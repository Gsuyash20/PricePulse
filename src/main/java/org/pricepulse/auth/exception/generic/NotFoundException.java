package org.pricepulse.auth.exception.generic;

import org.pricepulse.auth.exception.ServiceException;

public class NotFoundException extends ServiceException {
  public NotFoundException(String message) {
    super(message);
  }
}
