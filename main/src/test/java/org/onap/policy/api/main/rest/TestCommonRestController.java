/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.api.main.rest;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.junit.Test;

/**
 * Class to perform unit testing of CommonRestController.
 */
public class TestCommonRestController {
    private CommonRestController crc = new CommonRestController();

    @Test
    public void testAddLoggingHeaders() {
        UUID requestId = UUID.randomUUID();
        ResponseBuilder rb =
            crc.addLoggingHeaders(
              crc.addVersionControlHeaders(Response.status(Response.Status.OK)), requestId);
        assertTrue(rb.equals(rb.header("X-ONAP-RequestID", requestId)));
    }

    /*
    * Tests null response for null object
    */
    @Test
    public void testToJsonNull() {
        assertNull(crc.toJson(null));
    }
}
