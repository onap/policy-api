/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

/**
 * Class to perform unit testing of CommonRestController.
 */
public class TestCommonRestController {
    private CommonRestController crc = new CommonRestController();

    @Test
    public void testAddLoggingHeaders() {
        UUID requestId = UUID.randomUUID();
        ResponseEntity<Void> rb = crc.makeOkResponse(requestId, null);
        assertEquals(requestId.toString(), rb.getHeaders().getFirst("X-ONAP-RequestID"));
    }

    /*
     * Tests null response for null object
     */
    @Test
    public void testToJsonNull() {
        assertNull(crc.toJson(null));
    }
}