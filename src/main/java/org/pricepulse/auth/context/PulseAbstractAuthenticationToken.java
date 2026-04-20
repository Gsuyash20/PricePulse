package org.pricepulse.auth.context;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public abstract class PulseAbstractAuthenticationToken extends AbstractAuthenticationToken {

  private final Object principal;
  private Object credentials;

  @Setter
  private PulseContext details;

  protected PulseAbstractAuthenticationToken(
      Object principal,
      Object credentials,
      Collection<? extends GrantedAuthority> authorities) {

    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(false); // important
  }

  @Override
  public Object getCredentials() {
    return credentials;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public Object getDetails() {
    return details;
  }

  @Override
  public void setAuthenticated(boolean authenticated) {
    if (authenticated) {
      throw new IllegalArgumentException("Cannot set this token to trusted directly");
    }
    super.setAuthenticated(false);
  }
}