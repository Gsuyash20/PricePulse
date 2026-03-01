package org.pricepulse.auth.exception.generic;

import org.pricepulse.auth.exception.ServiceException;

public class InvalidInputException extends ServiceException {
  public InvalidInputException(String message) {
    super(message);
  }
}
