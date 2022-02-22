/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2021 Nordix Foundation.
 * Modifications Copyright (C) 2020,2022 Bell Canada.
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

package org.onap.policy.api.main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;

/**
 * This class performs unit test of Policy CRUS operations as implemented in {@link ToscaServiceTemplateService}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class TestToscaServiceTemplateServiceForPolicyCrud {

    private static StandardCoder standardCoder = new StandardCoder();
    private static StandardYamlCoder standardYamlCoder = new StandardYamlCoder();

    private static final String POLICY_RESOURCE = "policies/vCPE.policy.monitoring.input.tosca.json";
    private static final String POLICY_TYPE_RESOURCE = "policytypes/onap.policies.monitoring.tcagen2.yaml";
    private static final String POLICY_RESOURCE_WITH_BAD_POLICYTYPE_ID = "policies/vCPE.policy.bad.policytypeid.json";
    private static final String POLICY_RESOURCE_WITH_BAD_POLICYTYPE_VERSION =
        "policies/vCPE.policy.bad.policytypeversion.json";
    private static final String POLICY_RESOURCE_WITH_NO_POLICY_VERSION = "policies/vCPE.policy.no.policyversion.json";
    private static final String POLICY_RESOURCE_WITH_DIFFERENT_FIELDS =
        "policies/vCPE.policy.different.policy.fields.json";
    private static final String MULTIPLE_POLICIES_RESOURCE = "policies/vCPE.policies.optimization.input.tosca.json";

    private static final String POLICY_TYPE_RESOURCE_OPERATIONAL_COMMON =
        "policytypes/onap.policies.controlloop.operational.Common.yaml";
    private static final String POLICY_TYPE_RESOURCE_OPERATIONAL_DROOLS =
        "policytypes/onap.policies.controlloop.operational.common.Drools.yaml";
    private static final String POLICY_RESOURCE_OPERATIONAL = "policies/vCPE.policy.operational.input.tosca.json";
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

    @Before
    public void setUp() {
        Mockito.when(toscaServiceTemplateRepository.findById(new PfConceptKey(JpaToscaServiceTemplate.DEFAULT_NAME,
            JpaToscaServiceTemplate.DEFAULT_VERSION))).thenReturn(Optional.of(new JpaToscaServiceTemplate()));
    }

    @Test
    public void testFetchPolicies() {
        Mockito.when(toscaServiceTemplateRepository.findById(new PfConceptKey(JpaToscaServiceTemplate.DEFAULT_NAME,
            JpaToscaServiceTemplate.DEFAULT_VERSION))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.fetchPolicies("dummy", "1.0.0", null, null, null);
        }).hasMessage("service template not found in database");

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.fetchPolicies("dummy", "1.0.0", "dummy", null, null);
        }).hasMessage("service template not found in database");

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.fetchPolicies("dummy", "1.0.0", "dummy", "1.0.0", null);
        }).hasMessage("service template not found in database");

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.fetchPolicies(null, null, "dummy", "1.0.0", null);
        }).hasMessage("service template not found in database");
    }

    @Test
    public void testFetchLatestPolicies() {
        Mockito.when(toscaServiceTemplateRepository.findById(new PfConceptKey(JpaToscaServiceTemplate.DEFAULT_NAME,
            JpaToscaServiceTemplate.DEFAULT_VERSION))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.fetchLatestPolicies("dummy", "dummy", "dummy", null);
        }).hasMessage("service template not found in database");
    }

    @Test
    public void testCreatePolicy() throws Exception {
        assertThatThrownBy(() -> {
            toscaServiceTemplateService.createPolicy("dummy", "1.0.0", new ToscaServiceTemplate());
        }).hasMessage("topology template not specified on service template");

        var policyTypeServiceTemplate = standardYamlCoder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        assertThatCode(() -> toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate))
            .doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            var badPolicyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_WITH_BAD_POLICYTYPE_ID);
            var badPolicyServiceTemplate =
                standardCoder.decode(badPolicyString, ToscaServiceTemplate.class);
            toscaServiceTemplateService.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                badPolicyServiceTemplate);
        }).hasMessage(
            "Version not specified, the version of this TOSCA entity must be specified in "
                + "the type_version field");

        assertThatThrownBy(() -> {
            var badPolicyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_WITH_BAD_POLICYTYPE_VERSION);
            var badPolicyServiceTemplate =
                standardCoder.decode(badPolicyString, ToscaServiceTemplate.class);
            toscaServiceTemplateService.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                badPolicyServiceTemplate);
        }).hasMessageContaining(
            "item \"policy type\" value \"onap.policies.monitoring.cdap.tca.hi.lo.app:2.0.0\" INVALID, not found");

        assertThatThrownBy(() -> {
            var badPolicyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_WITH_NO_POLICY_VERSION);
            var badPolicyServiceTemplate =
                standardCoder.decode(badPolicyString, ToscaServiceTemplate.class);
            toscaServiceTemplateService.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                badPolicyServiceTemplate);
        }).hasMessageContaining("item \"version\" value \"0.0.0\" INVALID, is null");

        var policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
        var policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService
            .createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
        assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
        mockDbServiceTemplate(serviceTemplate);

        assertThatThrownBy(() -> {
            var badPolicyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_WITH_DIFFERENT_FIELDS);
            var badPolicyServiceTemplate =
                standardCoder.decode(badPolicyString, ToscaServiceTemplate.class);
            toscaServiceTemplateService.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                badPolicyServiceTemplate);
        }).hasMessageContaining(
            "item \"entity\" value \"onap.restart.tca:1.0.0\" INVALID, " + "does not equal existing entity");
    }

    @Test
    public void testCreateOperationalDroolsPolicy() throws CoderException {
        var policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_COMMON), ToscaServiceTemplate.class);

        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_DROOLS), ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        var policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_OPERATIONAL);
        var policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
        serviceTemplate =
            toscaServiceTemplateService.createPolicy(POLICY_TYPE_OPERATIONAL_DROOLS, "1.0.0", policyServiceTemplate);
        assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
    }

    @Test
    public void testSimpleCreatePolicy() throws Exception {

        assertThatThrownBy(() -> {
            String multiPoliciesString = ResourceUtils.getResourceAsString(MULTIPLE_POLICIES_RESOURCE);
            ToscaServiceTemplate multiPoliciesServiceTemplate =
                standardCoder.decode(multiPoliciesString, ToscaServiceTemplate.class);
            toscaServiceTemplateService.createPolicies(multiPoliciesServiceTemplate);
        }).hasMessageContaining(
            "no policy types are defined on the service template for the policies in the topology template");

        // Create required policy types
        var policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.Optimization.yaml"),
            ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.Resource.yaml"),
            ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils
                .getResourceAsString("policytypes/onap.policies.optimization.resource.AffinityPolicy.yaml"),
            ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils
                .getResourceAsString("policytypes/onap.policies.optimization.resource.DistancePolicy.yaml"),
            ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.resource.Vim_fit.yaml"),
            ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.resource.HpaPolicy.yaml"),
            ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.resource.VnfPolicy.yaml"),
            ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.Service.yaml"),
            ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils
                .getResourceAsString("policytypes/onap.policies.optimization.service.SubscriberPolicy.yaml"),
            ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.service.QueryPolicy.yaml"),
            ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.monitoring.tcagen2.yaml"),
            ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        // Create multiple policies in one call
        var multiPoliciesString = ResourceUtils.getResourceAsString(MULTIPLE_POLICIES_RESOURCE);
        var multiPoliciesServiceTemplate =
            standardCoder.decode(multiPoliciesString, ToscaServiceTemplate.class);

        assertThatCode(() -> {
            toscaServiceTemplateService.createPolicies(multiPoliciesServiceTemplate);
            toscaServiceTemplateService.createPolicies(multiPoliciesServiceTemplate);
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeletePolicy() throws CoderException, PfModelException {

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.deletePolicy("dummy", "1.0.0", "dummy", "1.0.0");
        }).hasMessage("no policies found");

        var policyTypeServiceTemplate = standardYamlCoder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        var policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
        var policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService
            .createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
        assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
        mockDbServiceTemplate(serviceTemplate);

        var exceptionMessage = "policy is in use, it is deployed in PDP group dummy subgroup dummy";
        Mockito.doThrow(new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, exceptionMessage))
            .when(pdpGroupService).assertPolicyNotDeployedInPdpGroup("onap.restart.tca", "1.0.0");
        assertThatThrownBy(() -> {
            toscaServiceTemplateService.deletePolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                "onap.restart.tca", "1.0.0");
        }).hasMessage(exceptionMessage);
        Mockito.doNothing().when(pdpGroupService).assertPolicyNotDeployedInPdpGroup("onap.restart.tca", "1.0.0");

        serviceTemplate = toscaServiceTemplateService
            .deletePolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", "onap.restart.tca", "1.0.0");
        assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());

        serviceTemplate.setToscaTopologyTemplate(new ToscaTopologyTemplate());
        mockDbServiceTemplate(serviceTemplate);
        assertThatThrownBy(() -> {
            toscaServiceTemplateService.deletePolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                "onap.restart.tca", "1.0.0");
        }).hasMessageContaining("no policies found");
    }

    @Test
    public void testFetchAllPolicies() throws Exception {
        // Create Policy Type
        var policyTypeServiceTemplate = standardYamlCoder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        // Create Policy
        var policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
        var policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService
            .createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        assertThat(serviceTemplate.getToscaTopologyTemplate().getPolicies()).hasSize(1);

        // Test fetch all policies
        serviceTemplate = toscaServiceTemplateService.fetchPolicies(null, null, null, null, null);

        assertThat(serviceTemplate.getToscaTopologyTemplate().getPolicies()).hasSize(1);
    }

    @Test
    public void testFetchSpecificPolicy_availablePolicy() throws Exception {
        // Create Policy Type
        var policyTypeServiceTemplate = standardYamlCoder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        // Create Policy
        var policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
        var policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService
            .createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        assertThat(serviceTemplate.getToscaTopologyTemplate().getPolicies()).hasSize(1);

        // Test fetch specific policy
        assertThat(toscaServiceTemplateService.fetchPolicies(null, null, "onap.restart.tca", "1.0.0", null)
            .getToscaTopologyTemplate().getPolicies()).hasSize(1);
    }

    @Test
    public void testFetchSpecificPolicy_unavailablePolicy() throws Exception {
        // Create Policy Type
        var policyTypeServiceTemplate = standardYamlCoder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        // Create Policy
        var policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
        var policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService
            .createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);
        assertNotNull(serviceTemplate.getToscaTopologyTemplate().getPolicies());
        assertThat(serviceTemplate.getToscaTopologyTemplate().getPolicies()).hasSize(1);

        // Test fetch specific policy
        assertThatThrownBy(() -> toscaServiceTemplateService.fetchPolicies(null, null, "onap.restart.tca",
            "2.0.0", null)).hasMessageContaining("policies for onap.restart.tca:2.0.0 do not exist");
    }

    @Test
    public void testDeleteSpecificPolicy_availablePolicy() throws Exception {
        var policyTypeServiceTemplate = standardYamlCoder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        var policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
        var policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService
            .createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
        assertThat(serviceTemplate.getToscaTopologyTemplate().getPolicies()).hasSize(1);
        mockDbServiceTemplate(serviceTemplate);

        serviceTemplate = toscaServiceTemplateService.deletePolicy(null, null, "onap.restart.tca", "1.0.0");
        assertThat(serviceTemplate.getToscaTopologyTemplate().getPolicies()).hasSize(1);
    }

    @Test
    public void testDeleteSpecificPolicy_unavailablePolicy() throws Exception {
        var policyTypeServiceTemplate = standardYamlCoder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
        var serviceTemplate = toscaServiceTemplateService.createPolicyType(policyTypeServiceTemplate);
        mockDbServiceTemplate(serviceTemplate);

        var policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
        var policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
        serviceTemplate = toscaServiceTemplateService
            .createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
        assertThat(serviceTemplate.getToscaTopologyTemplate().getPolicies()).hasSize(1);
        mockDbServiceTemplate(serviceTemplate);

        assertThatThrownBy(() -> toscaServiceTemplateService.deletePolicy(null, null, "onap.restart.tca", "2.0.0"))
            .hasMessageContaining("not found");

        assertThatThrownBy(() -> toscaServiceTemplateService.deletePolicy(null, null,
            "onap.restart.tca.unavailable", "1.0.0")).hasMessageContaining("not found");
    }
}