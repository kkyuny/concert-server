import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {

    scenarios: {
        random_seat_test: {
            executor: 'constant-vus',
            vus: 300,
            duration: '30s',
        },
    },

    thresholds: {
        http_req_duration: ['p(95)<3000'],
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {

    // 랜덤 좌석 요청
    const seatId = Math.floor(Math.random() * 100) + 1;

    const payload = JSON.stringify({
        concertSeatId: seatId,
        userId: __VU,
        token: `token-${__VU}`,
        concertId: 1
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const response = http.post(
        `${BASE_URL}/api/reservations`,
        payload,
        params
    );

    check(response, {
        'status check': (r) =>
            r.status === 200 ||
            r.status === 400 ||
            r.status === 500,
    });

    sleep(1);
}