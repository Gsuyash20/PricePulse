package org.pricepulse.auth.context;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PulseContext {
  private final String ipAddress;
  private final String userAgent;
  private final String requestId;
  private final String traceId;
  private final UUID userId;

  public PulseContext(String ipAddress, String userAgent, String traceId, String requestId, UUID userId) {
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.traceId = traceId;
    this.requestId = requestId;
    this.userId = userId;
  }

  public static PulseContextBuilder builder() {
    return new PulseContextBuilder();
  }

  public static class PulseContextBuilder {
    private String ipAddress;
    private String userAgent;
    private String traceId;
    private String requestId;
    private UUID userId;

    public PulseContextBuilder ipAddress(final String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }
    public PulseContextBuilder userAgent(final String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public PulseContextBuilder traceId(final String traceId) {
      this.traceId = traceId;
      return this;
    }
    public PulseContextBuilder requestId(final String requestId) {
      this.requestId = requestId;
      return this;
    }
    public PulseContextBuilder userId(final UUID userId) {
      this.userId = userId;
      return this;
    }

    public PulseContext build() {
      return new PulseContext(ipAddress, userAgent, traceId, requestId, userId );
    }
  }
}
