package org.pricepulse.auth.exception;

public abstract class ServiceException extends RuntimeException {

  public ServiceException(String message) {
    super(message);
  }
}
