import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const BASE_URL = 'http://localhost:8080';

export const options = {
    scenarios: {
        load: { executor: 'constant-vus', vus: 50, duration: '30s' },
    },
    thresholds: {
        http_req_duration: ['p(95)<3000'],
    },
};

export function setup() {
    const adminHeaders = { 'X-Loopers-Ldap': 'test', 'Content-Type': 'application/json' };
    const userCount = 50;
    const users = [];

    // 1. 브랜드 생성
    const brandRes = http.post(
        `${BASE_URL}/api-admin/v1/brands`,
        JSON.stringify({ name: 'k6-queue-brand' }),
        { headers: adminHeaders }
    );
    const brandId = brandRes.json('data.id');

    // 2. 상품 생성
    const productRes = http.post(
        `${BASE_URL}/api-admin/v1/products`,
        JSON.stringify({ brandId: brandId, name: 'k6-queue-product', price: 10000, stock: 1000000 }),
        { headers: adminHeaders }
    );
    const productId = productRes.json('data.id');

    // 3. 사용자 생성
    for (let i = 0; i < userCount; i++) {
        const loginId = `k6quser${i}`;
        http.post(
            `${BASE_URL}/api/v1/users`,
            JSON.stringify({
                loginId: loginId,
                password: 'Test1234!',
                name: `큐테스트유저${i}`,
                birthDate: '1990-01-01',
                email: `k6quser${i}@test.com`,
            }),
            { headers: { 'Content-Type': 'application/json' } }
        );
        users.push(loginId);
    }

    return { productId, users };
}

export default function (data) {
    const userIndex = __VU % data.users.length;
    const loginId = data.users[userIndex];
    const headers = {
        'Content-Type': 'application/json',
        'X-Loopers-LoginId': loginId,
        'X-Loopers-LoginPw': 'Test1234!',
    };

    // 1. 대기열 진입
    const enterRes = http.post(`${BASE_URL}/api/v1/queue/enter`, null, { headers });
    check(enterRes, { '대기열 진입 성공': (r) => r.status === 200 });

    // 2. 폴링: 토큰 발급될 때까지 대기
    let token = null;
    for (let i = 0; i < 30; i++) {
        const posRes = http.get(`${BASE_URL}/api/v1/queue/position`, { headers });
        if (posRes.status === 200) {
            const status = posRes.json('data.status');
            if (status === 'ALLOWED') {
                token = posRes.json('data.token');
                break;
            }
        }
        sleep(1);
    }

    if (!token) {
        return;
    }

    // 3. 토큰으로 주문
    const orderRes = http.post(
        `${BASE_URL}/api/v1/orders`,
        JSON.stringify({
            orderItems: [{ productId: data.productId, quantity: 1 }],
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

    check(orderRes, { '주문 성공': (r) => r.status === 200 });
    sleep(0.1);
}

// 결과를 JSON으로 저장
export function handleSummary(data) {
    return {
        'k6/results/with-queue.json': JSON.stringify(data, null, 2),
        stdout: textSummary(data),
    };
}

function textSummary(data) {
    const metrics = data.metrics;
    const duration = metrics.http_req_duration?.values || {};
    const reqs = metrics.http_reqs?.values || {};
    const failed = metrics.http_req_failed?.values || {};
    const checks = metrics.checks?.values || {};

    return `
=== 대기열 포함 부하 테스트 결과 ===
총 요청 수: ${reqs.count || 0}
초당 처리량: ${(reqs.rate || 0).toFixed(1)} req/s
평균 응답 시간: ${(duration.avg || 0).toFixed(0)}ms
p95 응답 시간: ${(duration['p(95)'] || 0).toFixed(0)}ms
최대 응답 시간: ${(duration.max || 0).toFixed(0)}ms
에러율: ${((failed.rate || 0) * 100).toFixed(2)}%
체크 성공률: ${((checks.rate || 0) * 100).toFixed(2)}%
`;
}
