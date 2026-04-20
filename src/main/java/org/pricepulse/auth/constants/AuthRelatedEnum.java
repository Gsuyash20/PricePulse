package org.pricepulse.auth.constants;

import lombok.Getter;

@Getter
public enum AuthRelatedEnum {
  AUTHORIZATION("Authorization"),
  BEARER("Bearer"),
  ACCESS("Access");

  private final String value;

  AuthRelatedEnum(String value) {
    this.value = value;
  }
}
