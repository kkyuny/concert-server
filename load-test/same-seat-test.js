import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    scenarios: {
        queue_reservation_test: {
            executor: 'constant-vus',
            vus: 20,
            duration: '10s',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<2000'],
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {

    const userId = __VU;
    const concertSeatId = 1;

    // 1️⃣ 먼저 큐 진입
    const queuePayload = JSON.stringify({
        userId: userId
    });

    const queueParams = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    http.post(
        `${BASE_URL}/api/queue/enter`,
        queuePayload,
        queueParams
    );

    // 약간 대기 (READY 전환 시간)
    sleep(0.2);

    // 2️⃣ 예약 요청
    const reservationPayload = JSON.stringify({
        userId: userId,
        concertSeatId: concertSeatId,
        token: `token-${userId}`,
    });

    const reservationParams = {
        headers: {
            'Content-Type': 'application/json',
        },
        timeout: '5s',
    };

    const response = http.post(
        `${BASE_URL}/api/reservations`,
        reservationPayload,
        reservationParams
    );

    check(response, {
        'status valid': (r) =>
            r.status === 200 ||
            r.status === 400 ||
            r.status === 500,
    });
}