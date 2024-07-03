/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2024 Nordix Foundation. All rights reserved.
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

package org.onap.policy.api.main.rest.provider.healthcheck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.policy.api.main.exception.PolicyApiRuntimeException;
import org.onap.policy.api.main.service.ToscaServiceTemplateService;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;

class TestHealthCheckProvider {

    @InjectMocks
    private HealthCheckProvider healthCheckProvider;

    @Mock
    private ToscaServiceTemplateService toscaService;

    AutoCloseable closeable;

    @BeforeEach
    void before() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void after() throws Exception {
        closeable.close();
    }

    @Test
    void performHealthCheck() {
        Mockito.when(toscaService.getDefaultJpaToscaServiceTemplate())
            .thenReturn(new JpaToscaServiceTemplate());

        var result = healthCheckProvider.performHealthCheck();
        assertEquals(200, result.getCode());
        assertTrue(result.isHealthy());
    }

    @Test
    void performHealthCheck_NotHealthy() {
        Mockito.when(toscaService.getDefaultJpaToscaServiceTemplate())
            .thenThrow(new PolicyApiRuntimeException("Error"));

        var result = healthCheckProvider.performHealthCheck();
        assertEquals(503, result.getCode());
        assertFalse(result.isHealthy());
    }
}