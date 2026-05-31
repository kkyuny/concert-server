package kr.hhplus.be.server.queue.api.dto;

import kr.hhplus.be.server.queue.domain.QueueStatus;

public record QueueStatusResponse(
        QueueStatus status
) {
}