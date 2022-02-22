/*
 *  ============LICENSE_START=======================================================
 *   Copyright (C) 2022 Bell Canada. All rights reserved.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.api.main.repository.ToscaServiceTemplateRepository;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;

/**
 * This class performs unit test of {@link ToscaServiceTemplateService}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class TestToscaServiceTemplateServiceForPolicyTypeCrud {

    private static StandardYamlCoder coder = new StandardYamlCoder();
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
    @Mock
    private ToscaServiceTemplateRepository toscaServiceTemplateRepository;

    @InjectMocks
    private ToscaServiceTemplateService toscaServiceTemplateService;

    /**
     * Setup the DB TOSCA service template object post create, and delete request.
     * @param serviceTemplate ToscaServiceTemplate object
     */
    private void mockDbServiceTemplate(ToscaServiceTemplate serviceTemplate) {
        Mockito.when(toscaServiceTemplateRepository.findById(new PfConceptKey(JpaToscaServiceTemplate.DEFAULT_NAME,
                JpaToscaServiceTemplate.DEFAULT_VERSION)))
            .thenReturn(Optional.of(new JpaToscaServiceTemplate(serviceTemplate)));
    }

    /**
     * Test setup.
     */
    @Before
    public void setUp() {
        Mockito.when(toscaServiceTemplateRepository.findById(new PfConceptKey(JpaToscaServiceTemplate.DEFAULT_NAME,
                JpaToscaServiceTemplate.DEFAULT_VERSION)))
            .thenReturn(Optional.of(new JpaToscaServiceTemplate()));
    }

    @Test
    public void testFetchPolicyTypes() throws PfModelException {
        assertThatThrownBy(() -> {
            toscaServiceTemplateService.fetchPolicyTypes("dummy", null);
        }).hasMessage("policy types for filter ToscaEntityFilter(name=dummy, version=null) do not exist");

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.fetchPolicyTypes("dummy", "dummy");
        }).hasMessage("policy types for filter ToscaEntityFilter(name=dummy, version=dummy) do not exist");

        // FIXME
        // ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.fetchPolicyTypes(null, null);
        // assertFalse(serviceTemplate.getPolicyTypes().isEmpty());
    }

    @Test
    public void testFetchLatestPolicyTypes() {

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.fetchLatestPolicyTypes("dummy");
        }).hasMessage("policy types for filter ToscaEntityFilter(name=dummy, version=LATEST) do not exist");
    }

    @Test
    public void testCreatePolicyType() throws CoderException, PfModelException {
        var policyTypeServiceTemplate = coder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_MONITORING), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        assertFalse(serviceTemplate.getPolicyTypes().isEmpty());
        assertEquals(2, serviceTemplate.getPolicyTypes().size());

        assertThatCode(() -> {
            toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        }).doesNotThrowAnyException();

        policyTypeServiceTemplate.getPolicyTypes().get("onap.policies.monitoring.tcagen2")
            .setDescription("Some other description");

        mockDbServiceTemplate(serviceTemplate);
        assertThatThrownBy(() -> {
            toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        }).hasMessageContaining("item \"entity\" value \"onap.policies.monitoring.tcagen2:1.0.0\" INVALID, "
            + "does not equal existing entity");

        assertThatThrownBy(() -> {
            ToscaServiceTemplate badPolicyType =
                coder.decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_WITH_NO_VERSION),
                    ToscaServiceTemplate.class);
            toscaServiceTemplateService.createPolicyType(badPolicyType);
        }).hasMessageContaining("item \"version\" value \"0.0.0\" INVALID, is null");

        mockDbServiceTemplate(serviceTemplate);
        toscaServiceTemplateService.deletePolicyType(POLICY_TYPE_NAME_MONITORING, POLICY_TYPE_VERSION);
    }

    @Test
    public void testCreateOperationalPolicyTypes() throws CoderException {
        ToscaServiceTemplate policyTypeServiceTemplate = coder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_COMMON), ToscaServiceTemplate.class);
        ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);

        assertNotNull(serviceTemplate.getPolicyTypes().get(POLICY_TYPE_OPERATIONAL_COMMON));

        mockDbServiceTemplate(serviceTemplate);
        policyTypeServiceTemplate = coder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_DROOLS), ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        assertNotNull(serviceTemplate.getPolicyTypes().get(POLICY_TYPE_OPERATIONAL_DROOLS));

        mockDbServiceTemplate(serviceTemplate);
        toscaServiceTemplateService.deletePolicyType(POLICY_TYPE_OPERATIONAL_DROOLS, POLICY_TYPE_VERSION);
        var policyTypes = serviceTemplate.getPolicyTypes();
        policyTypes.remove(POLICY_TYPE_OPERATIONAL_DROOLS);
        mockDbServiceTemplate(serviceTemplate);
        toscaServiceTemplateService.deletePolicyType(POLICY_TYPE_OPERATIONAL_COMMON, POLICY_TYPE_VERSION);
    }

    @Test
    public void testCreateApexOperationalPolicyTypes() throws CoderException {
        var policyTypeServiceTemplate = coder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_COMMON), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);

        mockDbServiceTemplate(serviceTemplate);
        policyTypeServiceTemplate = coder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_APEX), ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        assertNotNull(serviceTemplate.getPolicyTypes().get(POLICY_TYPE_OPERATIONAL_APEX));

        mockDbServiceTemplate(serviceTemplate);
        toscaServiceTemplateService.deletePolicyType(POLICY_TYPE_OPERATIONAL_APEX, POLICY_TYPE_VERSION);
    }

    @Test
    public void testDeletePolicyType() throws Exception {
        var policyTypeServiceTemplate = coder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_MONITORING), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        assertFalse(serviceTemplate.getPolicyTypes().isEmpty());

        var policyServiceTemplate = coder
            .decode(ResourceUtils.getResourceAsString(POLICY_RESOURCE_MONITORING), ToscaServiceTemplate.class);
        mockDbServiceTemplate(serviceTemplate);
        serviceTemplate = toscaServiceTemplateService.createPolicy("onap.policies.monitoring.tcagen2",
            "1.0.0", policyServiceTemplate);

        mockDbServiceTemplate(serviceTemplate);
        var exceptionMessage = "policy type onap.policies.monitoring.tcagen2:1.0.0 is in use, "
            + "it is referenced in policy onap.restart.tca:1.0.0";
        assertThatThrownBy(() -> {
            toscaServiceTemplateService.deletePolicyType("onap.policies.monitoring.tcagen2", "1.0.0");
        }).hasMessage(exceptionMessage);

        toscaServiceTemplateService.deletePolicy("onap.policies.monitoring.tcagen2", "1.0.0",
            "onap.restart.tca", "1.0.0");
        assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());

        exceptionMessage = "policy type is in use, it is referenced in PDP group dummy subgroup dummy";
        Mockito.doThrow(new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, exceptionMessage))
            .when(pdpGroupService).assertPolicyTypeNotSupportedInPdpGroup("onap.policies.monitoring.tcagen2", "1.0.0");
        assertThatThrownBy(() -> {
            toscaServiceTemplateService.deletePolicyType("onap.policies.monitoring.tcagen2", "1.0.0");
        }).hasMessage(exceptionMessage);

        serviceTemplate.setToscaTopologyTemplate(new ToscaTopologyTemplate());
        mockDbServiceTemplate(serviceTemplate);
        Mockito.doNothing().when(pdpGroupService)
            .assertPolicyTypeNotSupportedInPdpGroup("onap.policies.monitoring.tcagen2", "1.0.0");
        toscaServiceTemplateService.deletePolicyType("onap.policies.monitoring.tcagen2", "1.0.0");
        assertFalse(serviceTemplate.getPolicyTypes().isEmpty());

        serviceTemplate.getPolicyTypes().remove("onap.policies.monitoring.tcagen2");
        mockDbServiceTemplate(serviceTemplate);
        assertThatThrownBy(() -> {
            toscaServiceTemplateService.deletePolicyType("onap.policies.monitoring.tcagen2", "1.0.0");
        }).hasMessage("policy type onap.policies.monitoring.tcagen2:1.0.0 not found");
    }
}