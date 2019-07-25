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

package org.onap.policy.api.main.rest.provider;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.common.utils.coder.StandardCoder;
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
 * This class performs unit test of {@link PolicyProvider}
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class TestPolicyProvider {

    private static PolicyProvider policyProvider;
    private static PolicyTypeProvider policyTypeProvider;
    private static PolicyModelsProviderParameters providerParams;
    private static ApiParameterGroup apiParamGroup;
    private static StandardCoder standardCoder;

    private static final String POLICY_RESOURCE = "policies/vCPE.policy.monitoring.input.tosca.json";
    private static final String POLICY_TYPE_RESOURCE = "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.json";
    private static final String POLICY_RESOURCE_WITH_BAD_POLICYTYPE_ID = "policies/vCPE.policy.bad.policytypeid.json";
    private static final String POLICY_RESOURCE_WITH_BAD_POLICYTYPE_VERSION =
            "policies/vCPE.policy.bad.policytypeversion.json";

    /**
     * Initializes parameters.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    @Before
    public void setupParameters() throws PfModelException {

        standardCoder = new StandardCoder();
        providerParams = new PolicyModelsProviderParameters();
        providerParams.setDatabaseDriver("org.h2.Driver");
        providerParams.setDatabaseUrl("jdbc:h2:mem:testdb");
        providerParams.setDatabaseUser("policy");
        providerParams.setDatabasePassword(Base64.getEncoder().encodeToString("P01icY".getBytes()));
        providerParams.setPersistenceUnit("ToscaConceptTest");
        apiParamGroup = new ApiParameterGroup("ApiGroup", null, providerParams);
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
            policyProvider.fetchPolicies("dummy", "1.0.0", null, null);
        }).hasMessage("policy with ID null:null and type dummy:1.0.0 does not exist");

        assertThatThrownBy(() -> {
            policyProvider.fetchPolicies("dummy", "1.0.0", "dummy", null);
        }).hasMessage("policy with ID dummy:null and type dummy:1.0.0 does not exist");

        assertThatThrownBy(() -> {
            policyProvider.fetchPolicies("dummy", "1.0.0", "dummy", "1.0.0");
        }).hasMessage("policy with ID dummy:1.0.0 and type dummy:1.0.0 does not exist");
    }

    @Test
    public void testFetchLatestPolicies() {

        assertThatThrownBy(() -> {
            policyProvider.fetchLatestPolicies("dummy", "dummy", "dummy");
        }).hasMessage("policy with ID dummy:null and type dummy:dummy does not exist");
    }


    @Test
    public void testFetchDeployedPolicies() {
        String policyId = "onap.restart.tca";
        String policyVersion = "1.0.0";
        String policyTypeVersion = "1.0.0";
        String policyTypeId = "onap.policies.monitoring.cdap.tca.hi.lo.app";

        //Basic Exception Throw
        assertThatThrownBy(() -> {
            policyProvider.fetchDeployedPolicies("dummy", "dummy", "dummy");
        }).hasMessage("could not find policy with ID dummy and type dummy:dummy deployed in any pdp group");

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
            pdpSubGroup.getSupportedPolicyTypes().add(new ToscaPolicyTypeIdentifier(
                    policyTypeId, policyTypeVersion));
            pdpGroup.getPdpSubgroups().add(pdpSubGroup);

            Pdp pdp = new Pdp();
            pdp.setInstanceId("type-0");
            pdp.setMessage("Hello");
            pdp.setPdpState(PdpState.ACTIVE);
            pdp.setHealthy(PdpHealthStatus.UNKNOWN);
            pdpSubGroup.setPdpInstances(new ArrayList<>());
            pdpSubGroup.getPdpInstances().add(pdp);

            // Create Pdp Groups
            assertEquals(123, databaseProvider.createPdpGroups(groupList).get(0).getPdpSubgroups().get(0)
                    .getDesiredInstanceCount());
            assertEquals(1, databaseProvider.getPdpGroups("group").size());

            // Create Policy Type
            assertThatCode(() -> {
                String policyTypeString = ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE);
                ToscaServiceTemplate policyTypeServiceTemplate =
                        standardCoder.decode(policyTypeString, ToscaServiceTemplate.class);
                policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
            }).doesNotThrowAnyException();

            // Create Policy
            assertThatCode(() -> {
                String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
                ToscaServiceTemplate policyServiceTemplate =
                        standardCoder.decode(policyString, ToscaServiceTemplate.class);
                ToscaServiceTemplate serviceTemplate = policyProvider
                        .createPolicy(policyTypeId, policyTypeVersion, policyServiceTemplate);
                assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
            }).doesNotThrowAnyException();

            // Test fetchDeployedPolicies (deployedPolicyMap.isEmpty())==true
            assertThatThrownBy(
                () -> {
                    policyProvider.fetchDeployedPolicies(
                        policyTypeId, policyTypeVersion, policyId);
                }).hasMessage("could not find policy with ID " + policyId + " and type "
                    + policyTypeId + ":" + policyTypeVersion + " deployed in any pdp group");


            // Update pdpSubGroup
            pdpSubGroup.setPolicies(new ArrayList<>());
            pdpSubGroup.getPolicies().add(new ToscaPolicyIdentifier(policyId, policyVersion));
            assertEquals(1, databaseProvider.createPdpGroups(groupList).get(0).getPdpSubgroups().get(0)
                    .getPolicies().size());

            // Test fetchDeployedPolicies
            assertThatCode(
                () -> {
                    policyProvider.fetchDeployedPolicies(
                            policyTypeId, policyTypeVersion, policyId);
                }).doesNotThrowAnyException();

            // Test validateDeleteEligibility exception path(!pdpGroups.isEmpty())
            assertThatThrownBy(
                () -> {
                    policyProvider.deletePolicy(
                            "onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                            "onap.restart.tca", "1.0.0");
                }).hasMessageContaining("policy with ID " + policyId + ":" + policyVersion
                    + " cannot be deleted as it is deployed in pdp groups");
        }
        catch (Exception exc) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testCreatePolicy() {

        assertThatThrownBy(() -> {
            policyProvider.createPolicy("dummy", "1.0.0", new ToscaServiceTemplate());
        }).hasMessage("policy type with ID dummy:1.0.0 does not exist");

        assertThatCode(() -> {
            String policyTypeString = ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE);
            ToscaServiceTemplate policyTypeServiceTemplate =
                    standardCoder.decode(policyTypeString, ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            String badPolicyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_WITH_BAD_POLICYTYPE_ID);
            ToscaServiceTemplate badPolicyServiceTemplate =
                    standardCoder.decode(badPolicyString, ToscaServiceTemplate.class);
            policyProvider.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                    badPolicyServiceTemplate);
        }).hasMessage("policy type id does not match");

        assertThatThrownBy(() -> {
            String badPolicyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_WITH_BAD_POLICYTYPE_VERSION);
            ToscaServiceTemplate badPolicyServiceTemplate =
                    standardCoder.decode(badPolicyString, ToscaServiceTemplate.class);
            policyProvider.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                    badPolicyServiceTemplate);
        }).hasMessage("policy type version does not match");

        assertThatCode(() -> {
            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            ToscaServiceTemplate policyServiceTemplate =
                    standardCoder.decode(policyString, ToscaServiceTemplate.class);
            ToscaServiceTemplate serviceTemplate = policyProvider
                    .createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
            assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeletePolicy() {

        assertThatThrownBy(() -> {
            policyProvider.deletePolicy("dummy", "1.0.0", "dummy", "1.0.0");
        }).hasMessage("policy with ID dummy:1.0.0 and type dummy:1.0.0 does not exist");

        assertThatCode(() -> {
            String policyTypeString = ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE);
            ToscaServiceTemplate policyTypeServiceTemplate =
                    standardCoder.decode(policyTypeString, ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            ToscaServiceTemplate policyServiceTemplate =
                    standardCoder.decode(policyString, ToscaServiceTemplate.class);
            ToscaServiceTemplate serviceTemplate = policyProvider
                    .createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
            assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            ToscaServiceTemplate serviceTemplate = policyProvider.deletePolicy(
                    "onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", "onap.restart.tca", "1.0.0");
            assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
        }).doesNotThrowAnyException();

        String exceptionMessage = "policy with ID onap.restart.tca:1.0.0 and type "
            + "onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist";
        assertThatThrownBy(() -> {
            policyProvider.deletePolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0",
                    "onap.restart.tca", "1.0.0");
        }).hasMessage(exceptionMessage);
    }

}
