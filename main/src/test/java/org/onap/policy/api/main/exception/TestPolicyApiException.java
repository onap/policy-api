package org.onap.policy.api.main.exception;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TestPolicyApiException {

  @Test
  public void testPolicyApiExceptionString() {
    assertNotNull(new PolicyApiException("Policy API Exception Message"));
  }

  @Test
  public void testPolicyApiExceptionException() {
    assertNotNull(new PolicyApiException(new Exception()));

  }

  @Test
  public void testPolicyApiExceptionStringException() {
    assertNotNull(new PolicyApiException("Policy API Exception Message", new Exception()));
  }
}
