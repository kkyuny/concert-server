package kr.hhplus.be.server.queue.api;

import kr.hhplus.be.server.queue.api.dto.QueueEnterRequest;
import kr.hhplus.be.server.queue.api.dto.QueueStatusResponse;
import kr.hhplus.be.server.queue.application.QueueService;
import kr.hhplus.be.server.queue.domain.QueueStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/enter")
    public void enter(
            @RequestBody QueueEnterRequest request
    ) {
        queueService.enterQueue(request.userId());
    }

    @GetMapping("/status/{userId}")
    public QueueStatusResponse status(
            @PathVariable Long userId
    ) {

        QueueStatus status =
                queueService.getStatus(userId);

        return new QueueStatusResponse(status);
    }
}