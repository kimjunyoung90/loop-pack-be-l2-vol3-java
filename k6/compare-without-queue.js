import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const BASE_URL = 'http://localhost:8080';

const orderDuration = new Trend('order_duration');
const orderSuccess = new Counter('order_success');
const orderFail = new Counter('order_fail');

// 500명이 각 20회 주문 = 총 10,000건
export const options = {
    scenarios: {
        load: {
            executor: 'per-vu-iterations',
            vus: 500,
            iterations: 20,
            maxDuration: '10m',
        },
    },
};

export function setup() {
    const adminHeaders = { 'X-Loopers-Ldap': 'test', 'Content-Type': 'application/json' };

    const brandRes = http.post(
        `${BASE_URL}/api-admin/v1/brands`,
        JSON.stringify({ name: 'cmp-noq-brand' }),
        { headers: adminHeaders }
    );
    const brandId = brandRes.json('data.id');

    const productIds = [];
    for (let p = 0; p < 10; p++) {
        const productRes = http.post(
            `${BASE_URL}/api-admin/v1/products`,
            JSON.stringify({ brandId, name: `cmp-noq-product-${p}`, price: 10000 + p * 1000, stock: 1000000 }),
            { headers: adminHeaders }
        );
        productIds.push(productRes.json('data.id'));
    }

    for (let i = 0; i < 500; i++) {
        http.post(
            `${BASE_URL}/api/v1/users`,
            JSON.stringify({
                loginId: `noquser${i}`,
                password: 'Test1234!',
                name: `비교유저${i}`,
                birthDate: '1990-01-01',
                email: `noquser${i}@test.com`,
            }),
            { headers: { 'Content-Type': 'application/json' } }
        );
    }

    return { productIds };
}

export default function (data) {
    const loginId = `noquser${(__VU - 1) % 500}`;

    const res = http.post(
        `${BASE_URL}/api/v1/orders`,
        JSON.stringify({
            orderItems: [{ productId: data.productIds[__VU % data.productIds.length], quantity: 1 }],
            userCouponId: null,
        }),
        {
            headers: {
                'Content-Type': 'application/json',
                'X-Loopers-LoginId': loginId,
                'X-Loopers-LoginPw': 'Test1234!',
                'X-Idempotency-Key': uuidv4(),
                'X-Entry-Token': 'bypass',
            },
        }
    );

    orderDuration.add(res.timings.duration);
    if (res.status === 200) {
        orderSuccess.add(1);
    } else {
        orderFail.add(1);
    }
}

export function handleSummary(data) {
    return {
        'k6/results/without-queue.json': JSON.stringify(data, null, 2),
        stdout: formatSummary(data),
    };
}

function formatSummary(data) {
    const d = data.metrics.order_duration?.values || {};
    const success = data.metrics.order_success?.values?.count || 0;
    const fail = data.metrics.order_fail?.values?.count || 0;

    return `
=== 대기열 없이 (200명 동시 주문) ===
총 주문: ${success + fail}건 (성공: ${success}, 실패: ${fail})
에러율: ${((fail / (success + fail)) * 100).toFixed(2)}%

[주문 API 응답 시간]
  평균: ${(d.avg || 0).toFixed(0)}ms
  p90:  ${(d['p(90)'] || 0).toFixed(0)}ms
  p95:  ${(d['p(95)'] || 0).toFixed(0)}ms
  최대: ${(d.max || 0).toFixed(0)}ms
`;
}
