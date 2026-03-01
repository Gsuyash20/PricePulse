package org.pricepulse.auth.exception;

import lombok.Generated;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;


public abstract class ServiceException extends RuntimeException {

  public ServiceException(String message) {
    super(message);
  }
}
