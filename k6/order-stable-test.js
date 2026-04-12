import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const BASE_URL = 'http://localhost:8080';

// 안정적인 TPS를 찾기 위해 낮은 VU로 고정 부하 테스트
export const options = {
    scenarios: {
        low: { executor: 'constant-vus', vus: 30, duration: '30s', startTime: '0s' },
        mid: { executor: 'constant-vus', vus: 50, duration: '30s', startTime: '35s' },
        high: { executor: 'constant-vus', vus: 80, duration: '30s', startTime: '70s' },
    },
    thresholds: {
        http_req_duration: ['p(95)<2000'],
    },
};

// setup에서 기존 데이터의 productId를 조회
export function setup() {
    // 이전 테스트에서 생성한 상품 조회
    const res = http.get(`${BASE_URL}/api-admin/v1/products`, {
        headers: { 'X-Loopers-Ldap': 'test' },
    });
    const products = res.json('data.content');
    const productId = products[0].id;
    return { productId };
}

export default function (data) {
    const userIndex = __VU % 200;
    const loginId = `k6user${userIndex}`;

    const res = http.post(
        `${BASE_URL}/api/v1/orders`,
        JSON.stringify({
            orderItems: [{ productId: data.productId, quantity: 1 }],
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

    check(res, {
        '주문 성공 (200)': (r) => r.status === 200,
    });

    sleep(0.1);
}
