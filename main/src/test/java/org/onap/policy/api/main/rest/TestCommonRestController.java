/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2023-2024 Nordix Foundation.
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

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.policy.api.main.exception.PolicyApiRuntimeException;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Class to perform unit testing of CommonRestController.
 */
class TestCommonRestController {
    private final CommonRestController crc = new CommonRestController();

    @Test
    void testAddLoggingHeaders() {
        UUID requestId = UUID.randomUUID();
        ResponseEntity<Void> rb = crc.makeOkResponse(requestId, null);
        Assertions.assertEquals(requestId.toString(), rb.getHeaders().getFirst("X-ONAP-RequestID"));
    }

    /*
     * Tests null response for null object
     */
    @Test
    void testToJsonNull() throws CoderException {
        Assertions.assertNull(crc.toJson(null));

        var mockCoder = Mockito.mock(StandardCoder.class);
        Mockito.when(mockCoder.encode("fail")).thenThrow(new CoderException("fail"));
        ReflectionTestUtils.setField(crc, "coder", mockCoder);
        Assertions.assertNull(crc.toJson("fail"));
    }

    @Test
    void testLock() throws Exception {
        Class<?> mockControllerClass = Class.forName("org.onap.policy.api.main.rest.CommonRestController");
        CommonRestController mockController =
            (CommonRestController) mockControllerClass.getDeclaredConstructor().newInstance();
        var mockSemaphore = Mockito.mock(Semaphore.class);
        Mockito.doThrow(new InterruptedException("runtime error")).when(mockSemaphore).acquire();
        Field field = mockControllerClass.getDeclaredField("mutex");
        field.setAccessible(true);
        field.set(mockController, mockSemaphore);

        Assertions.assertThrows(PolicyApiRuntimeException.class, mockController::lock);
        ReflectionTestUtils.setField(crc, "mutex", new Semaphore(1));
    }
}