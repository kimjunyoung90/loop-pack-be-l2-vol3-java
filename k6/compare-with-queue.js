import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const BASE_URL = 'http://localhost:8080';

const orderDuration = new Trend('order_duration');
const queueWaitDuration = new Trend('queue_wait_duration');
const orderSuccess = new Counter('order_success');
const orderFail = new Counter('order_fail');

// 500명이 각 20회 대기열 → 주문 = 총 10,000건
export const options = {
    scenarios: {
        load: {
            executor: 'per-vu-iterations',
            vus: 500,
            iterations: 20,
            maxDuration: '30m',
        },
    },
};

export function setup() {
    const adminHeaders = { 'X-Loopers-Ldap': 'test', 'Content-Type': 'application/json' };

    // 앱 준비 대기
    for (let i = 0; i < 30; i++) {
        const healthCheck = http.get(`${BASE_URL}/api-admin/v1/brands?page=0&size=1`, { headers: adminHeaders });
        if (healthCheck.status === 200) break;
        sleep(1);
    }

    const brandRes = http.post(
        `${BASE_URL}/api-admin/v1/brands`,
        JSON.stringify({ name: 'cmp-q-brand' }),
        { headers: adminHeaders }
    );
    const brandId = brandRes.json('data.id');

    const productIds = [];
    for (let p = 0; p < 10; p++) {
        const productRes = http.post(
            `${BASE_URL}/api-admin/v1/products`,
            JSON.stringify({ brandId, name: `cmp-q-product-${p}`, price: 10000 + p * 1000, stock: 1000000 }),
            { headers: adminHeaders }
        );
        productIds.push(productRes.json('data.id'));
    }

    const users = [];
    for (let i = 0; i < 500; i++) {
        const loginId = `quser${i}`;
        http.post(
            `${BASE_URL}/api/v1/users`,
            JSON.stringify({
                loginId,
                password: 'Test1234!',
                name: `큐비교유저${i}`,
                birthDate: '1990-01-01',
                email: `quser${i}@test.com`,
            }),
            { headers: { 'Content-Type': 'application/json' } }
        );
        users.push(loginId);
    }

    return { productIds, users };
}

export default function (data) {
    const loginId = data.users[(__VU - 1) % data.users.length];
    const headers = {
        'Content-Type': 'application/json',
        'X-Loopers-LoginId': loginId,
        'X-Loopers-LoginPw': 'Test1234!',
    };

    // 1. 대기열 진입
    const queueStart = Date.now();
    http.post(`${BASE_URL}/api/v1/queue/enter`, null, { headers });

    // 2. 폴링: 서버가 반환하는 pollingIntervalSeconds에 따라 대기
    let token = null;
    while (!token) {
        const posRes = http.get(`${BASE_URL}/api/v1/queue/position`, { headers });
        if (posRes.status === 200) {
            const status = posRes.json('data.status');
            if (status === 'ALLOWED') {
                token = posRes.json('data.token');
                const orderableAfter = posRes.json('data.orderableAfterSeconds') || 0;
                if (orderableAfter > 0) {
                    sleep(orderableAfter);
                }
                break;
            }
            const pollingInterval = posRes.json('data.pollingIntervalSeconds') || 2;
            sleep(pollingInterval);
        } else {
            sleep(2);
        }
    }
    queueWaitDuration.add(Date.now() - queueStart);

    // 3. 주문
    const res = http.post(
        `${BASE_URL}/api/v1/orders`,
        JSON.stringify({
            orderItems: [{ productId: data.productIds[__VU % data.productIds.length], quantity: 1 }],
            userCouponId: null,
        }),
        {
            headers: {
                ...headers,
                'X-Idempotency-Key': uuidv4(),
                'X-Entry-Token': token,
            },
        }
    );

    orderDuration.add(res.timings.duration);
    if (res.status === 200) {
        orderSuccess.add(1);
    } else {
        console.error(`주문 실패: status=${res.status}, body=${res.body}, user=${loginId}`);
        orderFail.add(1);
    }
}

export function handleSummary(data) {
    return {
        'k6/results/with-queue.json': JSON.stringify(data, null, 2),
        stdout: formatSummary(data),
    };
}

function formatSummary(data) {
    const d = data.metrics.order_duration?.values || {};
    const q = data.metrics.queue_wait_duration?.values || {};
    const success = data.metrics.order_success?.values?.count || 0;
    const fail = data.metrics.order_fail?.values?.count || 0;
    const total = success + fail;

    return `
=== 대기열 포함 (200명 동시 주문) ===
총 주문: ${total}건 (성공: ${success}, 실패: ${fail})
에러율: ${total > 0 ? ((fail / total) * 100).toFixed(2) : 0}%

[주문 API 응답 시간]
  평균: ${(d.avg || 0).toFixed(0)}ms
  p90:  ${(d['p(90)'] || 0).toFixed(0)}ms
  p95:  ${(d['p(95)'] || 0).toFixed(0)}ms
  최대: ${(d.max || 0).toFixed(0)}ms

[대기열 대기 시간]
  평균: ${(q.avg || 0).toFixed(0)}ms
  p90:  ${(q['p(90)'] || 0).toFixed(0)}ms
  p95:  ${(q['p(95)'] || 0).toFixed(0)}ms
  최대: ${(q.max || 0).toFixed(0)}ms
`;
}
