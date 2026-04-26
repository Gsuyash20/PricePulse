package org.pricepulse.auth.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_user_time", columnList = "user_id, created_at"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_trace_id", columnList = "trace_id")
})
public class AuditLog {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  private String email;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "trace_id")
  private String traceId;

  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata; // JSON

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
