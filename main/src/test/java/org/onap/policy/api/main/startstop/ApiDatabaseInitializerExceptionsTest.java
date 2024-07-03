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

package org.onap.policy.api.main.startstop;

import static org.mockito.ArgumentMatchers.any;

import io.netty.handler.codec.CodecException;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.onap.policy.api.main.config.PolicyPreloadConfig;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.service.ToscaServiceTemplateService;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

class ApiDatabaseInitializerExceptionsTest {

    @Test
    void testInitializeApiDatabase_CoderExceptions() throws CoderException, PfModelException {
        var list = List.of("policy", "policyType");
        var mockPolicyPreload = Mockito.mock(PolicyPreloadConfig.class);
        Mockito.when(mockPolicyPreload.getPolicies()).thenReturn(list);
        Mockito.when(mockPolicyPreload.getPolicyTypes()).thenReturn(list);

        var serviceTemplate = new ToscaServiceTemplate();
        serviceTemplate.setPolicyTypes(new HashMap<>());
        var mockServiceTemplate = Mockito.mock(ToscaServiceTemplateService.class);
        Mockito.when(mockServiceTemplate.getFilteredPolicyTypes(any())).thenReturn(serviceTemplate);

        var mockYamlCoder = Mockito.mock(StandardYamlCoder.class);
        Mockito.when(mockYamlCoder.decode((String) any(), any()))
            .thenThrow(new CodecException("fail"));

        var databaseService = new ApiDatabaseInitializer(mockServiceTemplate, mockPolicyPreload);
        Assertions.assertThrows(PolicyApiException.class, databaseService::loadData);
    }

    @Test
    void testInitializeApiDatabase_CantFindResourceExceptions() throws PfModelException {
        var list = List.of("policy", "policyType");
        var mockPolicyPreload = Mockito.mock(PolicyPreloadConfig.class);
        Mockito.when(mockPolicyPreload.getPolicies()).thenReturn(list);
        Mockito.when(mockPolicyPreload.getPolicyTypes()).thenReturn(list);

        var serviceTemplate = new ToscaServiceTemplate();
        serviceTemplate.setPolicyTypes(new HashMap<>());
        var mockServiceTemplate = Mockito.mock(ToscaServiceTemplateService.class);
        Mockito.when(mockServiceTemplate.getFilteredPolicyTypes(any())).thenReturn(serviceTemplate);

        try (MockedStatic<ResourceUtils> utilities = Mockito.mockStatic(ResourceUtils.class)) {
            utilities.when(() -> ResourceUtils.getResourceAsString(any())).thenReturn(null);
        }

        var databaseService = new ApiDatabaseInitializer(mockServiceTemplate, mockPolicyPreload);
        Assertions.assertThrows(PolicyApiException.class, databaseService::loadData);
    }
}