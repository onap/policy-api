package org.onap.policy.api.main.rest.aaf;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestAafApiFilter {
  private AafApiFilter aafApiFilter = new AafApiFilter();

  @Test
  public void testGetPermissionTypeRoot() {
    assertEquals(aafApiFilter.getPermissionTypeRoot(), "org.onap.policy.policy-api");
  }
}
