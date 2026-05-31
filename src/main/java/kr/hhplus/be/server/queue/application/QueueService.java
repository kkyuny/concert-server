package kr.hhplus.be.server.queue.application;

import kr.hhplus.be.server.queue.domain.QueueStatus;
import kr.hhplus.be.server.queue.infrastructure.RedisQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {

    private static final int MAX_ACTIVE = 5;

    private final RedisQueueRepository repository;

    public void enterQueue(Long userId) {

        repository.addUser(userId.toString());
    }

    public QueueStatus getStatus(Long userId) {

        List<String> queue =
                repository.queue();

        if (queue == null) {
            return QueueStatus.WAITING;
        }

        int index =
                queue.indexOf(userId.toString());

        if (index == -1) {
            return QueueStatus.WAITING;
        }

        return index < MAX_ACTIVE
                ? QueueStatus.READY
                : QueueStatus.WAITING;
    }

    public boolean tryAcquire(Long userId) {
        if (getStatus(userId)
                != QueueStatus.READY) {
            return false;
        }

        if (repository.activeCount() >= MAX_ACTIVE) {
            return false;
        }

        repository.incrementActive();

        repository.removeUser(userId.toString());

        return true;
    }

    public void release() {

        repository.decrementActive();
    }
}