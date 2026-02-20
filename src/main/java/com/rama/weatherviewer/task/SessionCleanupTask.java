package com.rama.weatherviewer.task;

import com.rama.weatherviewer.repository.SessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionCleanupTask {
    private final SessionRepository sessionRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanUpExpiredSessions() {
        log.info("Starting scheduled session cleanup....");

        int deleteCount = sessionRepository.deleteExpiredSession();

        if (deleteCount < 0) {
            log.info("Cleanup finished. Removed {} expired sessions.", deleteCount);
        } else {
            log.info("Cleanup finished. No expired sessions found.");
        }
    }
}
