package org.pricepulse.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.domain.entity.AuditLog;
import org.pricepulse.auth.domain.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuditService {
  private AuditLogRepository auditLogRepository;

  @Async
  public void log(String eventType, UUID userId, String email,
                  String ip, String userAgent, String metadata, String traceId) {

    AuditLog log = new AuditLog();
    log.setEventType(eventType);
    log.setUserId(userId);
    log.setEmail(email);
    log.setIpAddress(ip);
    log.setUserAgent(userAgent);
    log.setMetadata(metadata);
    log.setTraceId(traceId);
    log.setCreatedAt(Instant.now());

    auditLogRepository.save(log);
  }
}
