/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Bell Canada. All rights reserved.
 *  Modifications Copyright (C) 2023 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================;
 */

package org.onap.policy.api.main.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.api.main.repository.PdpGroupRepository;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.pdp.concepts.PdpGroups;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdpGroup;

@RunWith(MockitoJUnitRunner.class)
class TestPdpGroupService {

    @Mock
    private PdpGroupRepository pdpGroupRepository;

    @InjectMocks
    private PdpGroupService pdpGroupService;

    AutoCloseable closeable;

    /**
     * Test setup.
     *
     * @throws CoderException decode errors
     */
    @BeforeEach
    public void setUp() throws CoderException {
        closeable = MockitoAnnotations.openMocks(this);
        var pdpGroups = new StandardCoder().decode(ResourceUtils.getResourceAsString("pdpgroups/PdpGroups.json"),
            PdpGroups.class).getGroups();
        List<JpaPdpGroup> jpaPdpGroupList = new ArrayList<>();
        pdpGroups.forEach(pdpGroup -> jpaPdpGroupList.add(new JpaPdpGroup(pdpGroup)));

        when(pdpGroupRepository.findAll()).thenReturn(jpaPdpGroupList);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testAssertPolicyTypeNotSupportedInPdpGroup() {
        assertThatCode(() -> pdpGroupService.assertPolicyTypeNotSupportedInPdpGroup("policy_type_not_supported",
            "1.0.0")).doesNotThrowAnyException();

        assertThatThrownBy(() -> pdpGroupService.assertPolicyTypeNotSupportedInPdpGroup(
            "onap.policies.controlloop.guard.common.FrequencyLimiter", "1.0.0"))
            .hasMessage("policy type is in use, it is referenced in PDP group defaultGroup subgroup xacml");
    }

    @Test
    void testAssertPolicyNotDeployedInPdpGroup() {
        assertThatCode(() -> pdpGroupService.assertPolicyNotDeployedInPdpGroup("policy_not_deployed", "1.0.0"))
            .doesNotThrowAnyException();

        assertThatThrownBy(() -> pdpGroupService.assertPolicyNotDeployedInPdpGroup(
            "onap.policies.controlloop.operational.common.apex.SampleDomain", "1.0.0"))
            .hasMessage("policy is in use, it is deployed in PDP group defaultGroup subgroup apex");
    }
}