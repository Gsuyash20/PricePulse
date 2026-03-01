package org.pricepulse.auth.exception.generic;

import org.pricepulse.auth.exception.ServiceException;

public class DuplicateResourceException extends ServiceException {
  public DuplicateResourceException(String message) {
    super(message);
  }
}
