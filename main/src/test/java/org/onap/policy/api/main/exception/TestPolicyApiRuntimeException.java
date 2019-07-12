package org.onap.policy.api.main.exception;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class TestPolicyApiRuntimeException {

  @Test
  public void testPolicyApiRuntimeExceptionString() {
      assertNotNull(new PolicyApiRuntimeException("Policy API Runtime Exception Message"));
  }

  @Test
  public void testPolicyApiRuntimeExceptionStringException() {
      assertNotNull(new PolicyApiRuntimeException("Policy API Runtime Exception Message", new Exception()));

  }
}
