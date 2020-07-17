/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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

package org.onap.policy.api.main.rest.provider;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * This class performs unit test of {@link PolicyProvider}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class TestPolicyProvider {

    private static PolicyProvider policyProvider;
    private static PolicyTypeProvider policyTypeProvider;
    private static PolicyModelsProviderParameters providerParams;
    private static ApiParameterGroup apiParamGroup;
    private static StandardCoder standardCoder;
    private static StandardYamlCoder standardYamlCoder;

    private static final String POLICY_RESOURCE = "policies/vCPE.policy.monitoring.input.tosca.json";
    private static final String POLICY_TYPE_RESOURCE = "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.yaml";
    private static final String POLICY_RESOURCE_WITH_BAD_POLICYTYPE_ID = "policies/vCPE.policy.bad.policytypeid.json";
    private static final String POLICY_RESOURCE_WITH_BAD_POLICYTYPE_VERSION =
        "policies/vCPE.policy.bad.policytypeversion.json";
    private static final String POLICY_RESOURCE_WITH_NO_POLICY_VERSION = "policies/vCPE.policy.no.policyversion.json";
    private static final String POLICY_RESOURCE_WITH_DIFFERENT_FIELDS =
        "policies/vCPE.policy.different.policy.fields.json";
    private static final String MULTIPLE_POLICIES_RESOURCE = "policies/vCPE.policies.optimization.input.tosca.json";

    public static final String POLICY_TYPE_RESOURCE_OPERATIONAL =
        "policytypes/onap.policies.controlloop.Operational.yaml";
    public static final String POLICY_TYPE_RESOURCE_OPERATIONAL_COMMON =
        "policytypes/onap.policies.controlloop.operational.Common.yaml";
    public static final String POLICY_TYPE_RESOURCE_OPERATIONAL_DROOLS =
        "policytypes/onap.policies.controlloop.operational.common.Drools.yaml";
    private static final String POLICY_RESOURCE_OPERATIONAL = "policies/vCPE.policy.operational.input.tosca.json";
    public static final String POLICY_TYPE_OPERATIONAL_DROOLS = "onap.policies.controlloop.operational.common.Drools";

    /**
     * Initializes parameters.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    @Before
    public void setupParameters() throws PfModelException {

        standardCoder = new StandardCoder();
        standardYamlCoder = new StandardYamlCoder();
        providerParams = new PolicyModelsProviderParameters();
        providerParams.setDatabaseDriver("org.h2.Driver");
        providerParams.setDatabaseUrl("jdbc:h2:mem:testdb");
        providerParams.setDatabaseUser("policy");
        providerParams.setDatabasePassword(Base64.getEncoder().encodeToString("P01icY".getBytes()));
        providerParams.setPersistenceUnit("ToscaConceptTest");
        apiParamGroup =
            new ApiParameterGroup("ApiGroup", null, providerParams, Collections.emptyList(), Collections.emptyList());
        ParameterService.register(apiParamGroup, true);
        policyTypeProvider = new PolicyTypeProvider();
        policyProvider = new PolicyProvider();
    }

    /**
     * Closes up DB connections and deregisters API parameter group.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    @After
    public void tearDown() throws PfModelException {

        policyTypeProvider.close();
        policyProvider.close();
        ParameterService.deregister(apiParamGroup);
    }

    @Test
    public void testFetchPolicies() {

        assertThatThrownBy(() -> {
            policyProvider.fetchPolicies("dummy", "1.0.0", null, null, null);
        }).hasMessage("service template not found in database");

        assertThatThrownBy(() -> {
            policyProvider.fetchPolicies("dummy", "1.0.0", "dummy", null, null);
        }).hasMessage("service template not found in database");

        assertThatThrownBy(() -> {
            policyProvider.fetchPolicies("dummy", "1.0.0", "dummy", "1.0.0", null);
        }).hasMessage("service template not found in database");

        assertThatThrownBy(() -> {
            policyProvider.fetchPolicies(null, null, "dummy", "1.0.0", null);
        }).hasMessage("service template not found in database");
    }

    @Test
    public void testFetchSpecificVersionOfPolicy() throws Exception {
        String policyTypeVersion = "1.0.0";
        String policyTypeId = "onap.policies.monitoring.cdap.tca.hi.lo.app";

        // Create Policy Type
        assertThatCode(() -> {
            ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder
                .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        }).doesNotThrowAnyException();

        // Create Policy
        String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
        ToscaServiceTemplate policyServiceTemplate =
            standardCoder.decode(policyString, ToscaServiceTemplate.class);
        ToscaServiceTemplate serviceTemplate =
            policyProvider.createPolicy(policyTypeId, policyTypeVersion, policyServiceTemplate);
        assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());

        // Test fetch specific policy
        assertThatCode(() -> {
            policyProvider.fetchPolicies(null,  null, "onap.restart.tca", "1.0.0", null);
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            policyProvider.fetchPolicies(null,  null, "onap.restart.tca", "2.0.0", null);
        }).hasMessageContaining("policies for onap.restart.tca:2.0.0 do not exist");
    }

    @Test
    public void testFetchLatestPolicies() {

        assertThatThrownBy(() -> {
            policyProvider.fetchLatestPolicies("dummy", "dummy", "dummy", null);
        }).hasMessage("service template not found in database");
    }

    @Test
    public void testFetchDeployedPolicies() {
        String policyId = "onap.restart.tca";
        String policyVersion = "1.0.0";
        String policyTypeVersion = "1.0.0";
        String policyTypeId = "onap.policies.monitoring.cdap.tca.hi.lo.app";

        try (PolicyModelsProvider databaseProvider =
            new PolicyModelsProviderFactory().createPolicyModelsProvider(providerParams)) {
            assertEquals(0, databaseProvider.getPdpGroups("name").size());
            assertEquals(0, databaseProvider.getFilteredPdpGroups(PdpGroupFilter.builder().build()).size());

            assertNotNull(databaseProvider.createPdpGroups(new ArrayList<>()));
            assertNotNull(databaseProvider.updatePdpGroups(new ArrayList<>()));

            PdpGroup pdpGroup = new PdpGroup();
            pdpGroup.setName("group");
            pdpGroup.setVersion("1.2.3");
            pdpGroup.setPdpGroupState(PdpState.ACTIVE);
            pdpGroup.setPdpSubgroups(new ArrayList<>());
            List<PdpGroup> groupList = new ArrayList<>();
            groupList.add(pdpGroup);

            PdpSubGroup pdpSubGroup = new PdpSubGroup();
            pdpSubGroup.setPdpType("type");
            pdpSubGroup.setDesiredInstanceCount(123);
            pdpSubGroup.setSupportedPolicyTypes(new ArrayList<>());
            pdpSubGroup.getSupportedPolicyTypes().add(new ToscaPolicyTypeIdentifier(policyTypeId, policyTypeVersion));
            pdpGroup.getPdpSubgroups().add(pdpSubGroup);

            Pdp pdp = new Pdp();
            pdp.setInstanceId("type-0");
            pdp.setMessage("Hello");
            pdp.setPdpState(PdpState.ACTIVE);
            pdp.setHealthy(PdpHealthStatus.UNKNOWN);
            pdpSubGroup.setPdpInstances(new ArrayList<>());
            pdpSubGroup.getPdpInstances().add(pdp);

            // Create Pdp Groups
            assertEquals(123,
                databaseProvider.createPdpGroups(groupList).get(0).getPdpSubgroups().get(0).getDesiredInstanceCount());
            assertEquals(1, databaseProvider.getPdpGroups("group").size());

            // Create Policy Type
            assertThatCode(() -> {
                ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder
                    .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
                policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
            }).doesNotThrowAnyException();

            // Create Policy
            assertThatCode(() -> {
                String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
                ToscaServiceTemplate policyServiceTemplate =
                    standardCoder.decode(policyString, ToscaServiceTemplate.class);
                ToscaServiceTemplate serviceTemplate =
                    policyProvider.createPolicy(policyTypeId, policyTypeVersion, policyServiceTemplate);
                assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
            }).doesNotThrowAnyException();

            // Update pdpSubGroup
            pdpSubGroup.setPolicies(new ArrayList<>());
            pdpSubGroup.getPolicies().add(new ToscaPolicyIdentifier(policyId, policyVersion));
            assertEquals(1,
                databaseProvider.createPdpGroups(groupList).get(0).getPdpSubgroups().get(0).getPolicies().size());

            // Test validateDeleteEligibility exception path(!pdpGroups.isEmpty())
            assertThatThrownBy(() -> {
                policyProvider.deletePolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", "onap.restart.tca",
                    "1.0.0");
            }).hasMessageContaining("policy is in use, it is deployed in PDP group group subgroup type");
        } catch (Exception exc) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testCreatePolicy() throws Exception {

        assertThatThrownBy(() -> {
            policyProvider.createPolicy("dummy", "1.0.0", new ToscaServiceTemplate());
        }).hasMessage("topology template not specified on service template");

        ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        assertThatCode(() -> policyTypeProvider.createPolicyType(policyTypeServiceTemplate)).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            String badPolicyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_WITH_BAD_POLICYTYPE_ID);
            ToscaServiceTemplate badPolicyServiceTemplate =
                standardCoder.decode(badPolicyString, ToscaServiceTemplate.class);
            policyProvider.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                badPolicyServiceTemplate);
        }).hasMessage(
            "PolicyType version not specified, the version of the PolicyType for this policy must be specified in the "
                + "type_version field");

        assertThatThrownBy(() -> {
            String badPolicyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_WITH_BAD_POLICYTYPE_VERSION);
            ToscaServiceTemplate badPolicyServiceTemplate =
                standardCoder.decode(badPolicyString, ToscaServiceTemplate.class);
            policyProvider.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                badPolicyServiceTemplate);
        }).hasMessageContaining(
            "policy type onap.policies.monitoring.cdap.tca.hi.lo.app:2.0.0 referenced in policy not found");

        assertThatThrownBy(() -> {
            String badPolicyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_WITH_NO_POLICY_VERSION);
            ToscaServiceTemplate badPolicyServiceTemplate =
                standardCoder.decode(badPolicyString, ToscaServiceTemplate.class);
            policyProvider.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                badPolicyServiceTemplate);
        }).hasMessageContaining("key version is a null version");

        String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
        ToscaServiceTemplate policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
        ToscaServiceTemplate serviceTemplate =
            policyProvider.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
        assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());

        assertThatThrownBy(() -> {
            String badPolicyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_WITH_DIFFERENT_FIELDS);
            ToscaServiceTemplate badPolicyServiceTemplate =
                standardCoder.decode(badPolicyString, ToscaServiceTemplate.class);
            policyProvider.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                badPolicyServiceTemplate);
        }).hasMessageContaining("entity in incoming fragment does not equal existing entity");
    }

    @Test
    public void testCreateOperationalDroolsPolicy() throws CoderException, PfModelException {
        ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder
            .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL), ToscaServiceTemplate.class);

        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_COMMON), ToscaServiceTemplate.class);

        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_DROOLS), ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_OPERATIONAL);
        ToscaServiceTemplate policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
        ToscaServiceTemplate serviceTemplate =
            policyProvider.createPolicy(POLICY_TYPE_OPERATIONAL_DROOLS, "1.0.0", policyServiceTemplate);
        assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
    }

    @Test
    public void testSimpleCreatePolicy() throws Exception {

        assertThatThrownBy(() -> {
            String multiPoliciesString = ResourceUtils.getResourceAsString(MULTIPLE_POLICIES_RESOURCE);
            ToscaServiceTemplate multiPoliciesServiceTemplate =
                standardCoder.decode(multiPoliciesString, ToscaServiceTemplate.class);
            policyProvider.createPolicies(multiPoliciesServiceTemplate);
        }).hasMessageContaining(
            "no policy types are defined on the service template for the policies in the topology template");

        // Create required policy types
        ToscaServiceTemplate policyTypeServiceTemplate =
            standardYamlCoder.decode(ResourceUtils.getResourceAsString("policytypes/onap.policies.Optimization.yaml"),
                ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.Resource.yaml"),
            ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.resource.AffinityPolicy.yaml"),
            ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.resource.DistancePolicy.yaml"),
            ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.resource.Vim_fit.yaml"),
            ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.resource.HpaPolicy.yaml"),
            ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.resource.VnfPolicy.yaml"),
            ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.Service.yaml"),
            ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.service.SubscriberPolicy.yaml"),
            ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.service.QueryPolicy.yaml"),
            ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        policyTypeServiceTemplate = standardYamlCoder.decode(
            ResourceUtils.getResourceAsString("policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.yaml"),
            ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        // Create multiple policies in one call
        String multiPoliciesString = ResourceUtils.getResourceAsString(MULTIPLE_POLICIES_RESOURCE);
        ToscaServiceTemplate multiPoliciesServiceTemplate =
            standardCoder.decode(multiPoliciesString, ToscaServiceTemplate.class);

        assertThatCode(() -> {
            policyProvider.createPolicies(multiPoliciesServiceTemplate);
            policyProvider.createPolicies(multiPoliciesServiceTemplate);
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeletePolicy() {

        assertThatThrownBy(() -> {
            policyProvider.deletePolicy("dummy", "1.0.0", "dummy", "1.0.0");
        }).hasMessage("service template not found in database");

        assertThatCode(() -> {
            ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder
                .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            ToscaServiceTemplate policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
            ToscaServiceTemplate serviceTemplate = policyProvider
                .createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
            assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            ToscaServiceTemplate serviceTemplate = policyProvider
                .deletePolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", "onap.restart.tca", "1.0.0");
            assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            policyProvider.deletePolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", "onap.restart.tca",
                "1.0.0");
        }).hasMessageContaining("no policies found");
    }
}
