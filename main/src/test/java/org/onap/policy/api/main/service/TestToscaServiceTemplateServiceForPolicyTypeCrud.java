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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * This class performs unit test of Policy Type CRUD operations as implemented in {@link ToscaServiceTemplateService}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@RunWith(MockitoJUnitRunner.class)
class TestToscaServiceTemplateServiceForPolicyTypeCrud extends TestCommonToscaServiceTemplateService {

    private static final StandardYamlCoder coder = new StandardYamlCoder();
    private static final String POLICY_TYPE_VERSION = "1.0.0";

    private static final String POLICY_RESOURCE_MONITORING = "policies/vCPE.policy.monitoring.input.tosca.yaml";
    private static final String POLICY_TYPE_RESOURCE_MONITORING = "policytypes/onap.policies.monitoring.tcagen2.yaml";
    private static final String POLICY_TYPE_RESOURCE_WITH_NO_VERSION =
        "policytypes/onap.policies.optimization.Resource.no.version.yaml";
    private static final String POLICY_TYPE_NAME_MONITORING = "onap.policies.monitoring.tcagen2";

    private static final String POLICY_TYPE_RESOURCE_OPERATIONAL_COMMON =
        "policytypes/onap.policies.controlloop.operational.Common.yaml";
    private static final String POLICY_TYPE_RESOURCE_OPERATIONAL_DROOLS =
        "policytypes/onap.policies.controlloop.operational.common.Drools.yaml";
    private static final String POLICY_TYPE_RESOURCE_OPERATIONAL_APEX =
        "policytypes/onap.policies.controlloop.operational.common.Apex.yaml";
    private static final String POLICY_TYPE_OPERATIONAL_COMMON = "onap.policies.controlloop.operational.Common";
    private static final String POLICY_TYPE_OPERATIONAL_APEX = "onap.policies.controlloop.operational.common.Apex";
    private static final String POLICY_TYPE_OPERATIONAL_DROOLS = "onap.policies.controlloop.operational.common.Drools";

    @Mock
    private PdpGroupService pdpGroupService;

    @InjectMocks
    private ToscaServiceTemplateService toscaServiceTemplateService;

    /**
     * Test setup.
     */
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void testFetchPolicyTypes() {
        assertThatThrownBy(() -> toscaServiceTemplateService.fetchPolicyTypes("dummy", null))
            .hasMessage("policy types for filter ToscaEntityFilter(name=dummy, version=null) do not exist");

        assertThatThrownBy(() -> toscaServiceTemplateService.fetchPolicyTypes("dummy", "dummy"))
            .hasMessage("policy types for filter ToscaEntityFilter(name=dummy, version=dummy) do not exist");

        // FIXME
        // ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.fetchPolicyTypes(null, null);
        // assertFalse(serviceTemplate.getPolicyTypes().isEmpty());
    }

    @Test
    void testFetchLatestPolicyTypes() {
        assertThatThrownBy(() -> toscaServiceTemplateService.fetchLatestPolicyTypes("dummy"))
            .hasMessage("policy types for filter ToscaEntityFilter(name=dummy, version=LATEST) do not exist");
    }

    @Test
    void testCreatePolicyType() throws CoderException {
        var policyTypeServiceTemplate = coder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_MONITORING), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        assertFalse(serviceTemplate.getPolicyTypes().isEmpty());
        assertEquals(2, serviceTemplate.getPolicyTypes().size());
        mockDbServiceTemplate(serviceTemplate, null, null);

        policyTypeServiceTemplate.getPolicyTypes().get("onap.policies.monitoring.tcagen2")
            .setDescription("Some other description");

        assertThatThrownBy(() -> toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate))
            .hasMessageContaining("item \"entity\" value \"onap.policies.monitoring.tcagen2:1.0.0\" INVALID, "
                + "does not equal existing entity");

        assertThatThrownBy(() -> {
            ToscaServiceTemplate badPolicyType =
                coder.decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_WITH_NO_VERSION),
                    ToscaServiceTemplate.class);
            toscaServiceTemplateService.createPolicyType(badPolicyType);
        }).hasMessageContaining("item \"version\" value \"0.0.0\" INVALID, is null");

        toscaServiceTemplateService.deletePolicyType(POLICY_TYPE_NAME_MONITORING, POLICY_TYPE_VERSION);
    }

    @Test
    void testCreateOperationalPolicyTypes() throws CoderException {
        ToscaServiceTemplate policyTypeServiceTemplate = coder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_COMMON), ToscaServiceTemplate.class);
        ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        assertNotNull(serviceTemplate.getPolicyTypes().get(POLICY_TYPE_OPERATIONAL_COMMON));
        mockDbServiceTemplate(serviceTemplate, null, null);

        policyTypeServiceTemplate = coder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_DROOLS), ToscaServiceTemplate.class);
        var createPolicyTypeResponseFragment = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        assertNotNull(createPolicyTypeResponseFragment.getPolicyTypes().get(POLICY_TYPE_OPERATIONAL_DROOLS));
        mockDbServiceTemplate(serviceTemplate, createPolicyTypeResponseFragment, Operation.CREATE_POLICY_TYPE);

        var deletePolicyTypeResponseFragment = toscaServiceTemplateService
            .deletePolicyType(POLICY_TYPE_OPERATIONAL_DROOLS, POLICY_TYPE_VERSION);
        mockDbServiceTemplate(serviceTemplate, deletePolicyTypeResponseFragment, Operation.DELETE_POLICY_TYPE);
        toscaServiceTemplateService.deletePolicyType(POLICY_TYPE_OPERATIONAL_COMMON, POLICY_TYPE_VERSION);
    }

    @Test
    void testCreateApexOperationalPolicyTypes() throws CoderException {
        var policyTypeServiceTemplate = coder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_COMMON), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);

        mockDbServiceTemplate(serviceTemplate, null, null);
        policyTypeServiceTemplate = coder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_APEX), ToscaServiceTemplate.class);
        var createPolicyTypeResponseFragment = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        assertNotNull(createPolicyTypeResponseFragment.getPolicyTypes().get(POLICY_TYPE_OPERATIONAL_APEX));

        mockDbServiceTemplate(serviceTemplate, createPolicyTypeResponseFragment, Operation.CREATE_POLICY_TYPE);
        toscaServiceTemplateService.deletePolicyType(POLICY_TYPE_OPERATIONAL_APEX, POLICY_TYPE_VERSION);
    }

    @Test
    void testDeletePolicyType() throws CoderException {
        var policyTypeServiceTemplate = coder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_MONITORING), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        assertFalse(serviceTemplate.getPolicyTypes().isEmpty());

        var policyServiceTemplate = coder
            .decode(ResourceUtils.getResourceAsString(POLICY_RESOURCE_MONITORING), ToscaServiceTemplate.class);
        mockDbServiceTemplate(serviceTemplate, null, null);
        var createPolicyResponseFragment = toscaServiceTemplateService.createPolicy("onap.policies.monitoring.tcagen2",
            "1.0.0", policyServiceTemplate);

        mockDbServiceTemplate(serviceTemplate, createPolicyResponseFragment, Operation.CREATE_POLICY);
        var exceptionMessage = "policy type onap.policies.monitoring.tcagen2:1.0.0 is in use, "
            + "it is referenced in policy onap.restart.tca:1.0.0";
        assertThatThrownBy(() -> toscaServiceTemplateService.deletePolicyType("onap.policies.monitoring.tcagen2",
            "1.0.0")).hasMessage(exceptionMessage);

        var deletePolicyResponseFragment = toscaServiceTemplateService
            .deletePolicy("onap.policies.monitoring.tcagen2", "1.0.0", "onap.restart.tca", "1.0.0");
        assertFalse(deletePolicyResponseFragment.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
        mockDbServiceTemplate(serviceTemplate, deletePolicyResponseFragment, Operation.DELETE_POLICY);

        exceptionMessage = "policy type is in use, it is referenced in PDP group dummy subgroup dummy";
        Mockito.doThrow(new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, exceptionMessage))
            .when(pdpGroupService).assertPolicyTypeNotSupportedInPdpGroup("onap.policies.monitoring.tcagen2", "1.0.0");
        assertThatThrownBy(() -> toscaServiceTemplateService.deletePolicyType("onap.policies.monitoring.tcagen2",
            "1.0.0")).hasMessage(exceptionMessage);

        Mockito.doNothing().when(pdpGroupService)
            .assertPolicyTypeNotSupportedInPdpGroup("onap.policies.monitoring.tcagen2", "1.0.0");
        var deletePolicyTypeResponseFragment = toscaServiceTemplateService
            .deletePolicyType("onap.policies.monitoring.tcagen2", "1.0.0");
        assertFalse(deletePolicyTypeResponseFragment.getPolicyTypes().isEmpty());

        mockDbServiceTemplate(serviceTemplate, deletePolicyTypeResponseFragment, Operation.DELETE_POLICY_TYPE);
        assertThatThrownBy(() -> toscaServiceTemplateService.deletePolicyType("onap.policies.monitoring.tcagen2",
            "1.0.0")).hasMessage("policy type onap.policies.monitoring.tcagen2:1.0.0 not found");
    }
}