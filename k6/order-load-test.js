import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const BASE_URL = 'http://localhost:8080';
const ADMIN_HEADER = { 'X-Loopers-Ldap': 'test' };

// 부하 단계: 점진적으로 VU(가상 사용자)를 증가시켜 한계점을 찾는다
export const options = {
    stages: [
        { duration: '10s', target: 10 },   // 워밍업
        { duration: '20s', target: 50 },   // 점진 증가
        { duration: '20s', target: 100 },  // 중간 부하
        { duration: '20s', target: 200 },  // 고부하
        { duration: '10s', target: 0 },    // 정리
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],  // 95% 요청이 2초 이내
        http_req_failed: ['rate<0.1'],      // 에러율 10% 미만
    },
};

// setup: 테스트용 사용자와 상품을 생성한다
export function setup() {
    const userCount = 200;
    const users = [];

    // 1. 브랜드 생성
    const brandRes = http.post(
        `${BASE_URL}/api-admin/v1/brands`,
        JSON.stringify({ name: 'k6-test-brand' }),
        { headers: { ...ADMIN_HEADER, 'Content-Type': 'application/json' } }
    );
    check(brandRes, { '브랜드 생성 성공': (r) => r.status === 200 });
    const brandId = brandRes.json('data.id');

    // 2. 상품 생성 (재고 충분히)
    const productRes = http.post(
        `${BASE_URL}/api-admin/v1/products`,
        JSON.stringify({ brandId: brandId, name: 'k6-test-product', price: 10000, stock: 1000000 }),
        { headers: { ...ADMIN_HEADER, 'Content-Type': 'application/json' } }
    );
    check(productRes, { '상품 생성 성공': (r) => r.status === 200 });
    const productId = productRes.json('data.id');

    // 3. 사용자 생성
    for (let i = 0; i < userCount; i++) {
        const loginId = `k6user${i}`;
        const password = 'Test1234!';

        const signUpRes = http.post(
            `${BASE_URL}/api/v1/users`,
            JSON.stringify({
                loginId: loginId,
                password: password,
                name: `테스트유저${i}`,
                birthDate: '1990-01-01',
                email: `k6user${i}@test.com`,
            }),
            { headers: { 'Content-Type': 'application/json' } }
        );
        check(signUpRes, { [`유저 ${i} 생성`]: (r) => r.status === 200 });

        users.push({ loginId, password });
    }

    return { users, productId };
}

// 각 VU가 실행하는 시나리오: 주문 생성
export default function (data) {
    const userIndex = __VU % data.users.length;
    const user = data.users[userIndex];

    const res = http.post(
        `${BASE_URL}/api/v1/orders`,
        JSON.stringify({
            orderItems: [{ productId: data.productId, quantity: 1 }],
            userCouponId: null,
        }),
        {
            headers: {
                'Content-Type': 'application/json',
                'X-Loopers-LoginId': user.loginId,
                'X-Loopers-LoginPw': user.password,
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
