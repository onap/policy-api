/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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
import static org.junit.Assert.assertTrue;
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
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

/**
 * This class performs unit test of {@link LegacyOperationalPolicyProvider}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class TestLegacyOperationalPolicyProvider {

    private static LegacyOperationalPolicyProvider operationalPolicyProvider;
    private static PolicyTypeProvider policyTypeProvider;
    private static PolicyModelsProviderParameters providerParams;
    private static ApiParameterGroup apiParamGroup;
    private static StandardCoder standardCoder;
    private static StandardYamlCoder standardYamlCoder;

    private static final String POLICY_RESOURCE = "policies/vCPE.policy.operational.input.json";
    private static final String POLICY_TYPE_RESOURCE = "policytypes/onap.policies.controlloop.Operational.yaml";
    private static final String POLICY_TYPE_ID = "onap.policies.controlloop.Operational:1.0.0";
    private static final String POLICY_ID = "operational.restart:1.0.0";
    private static final String POLICY_NAME = "operational.restart";
    private static final String POLICY_VERSION = "1";
    private static final String POLICY_TYPE_NAME = "onap.policies.controlloop.Operational";
    private static final String POLICY_TYPE_VERSION = "1.0.0";
    private static final String LEGACY_MINOR_PATCH_SUFFIX = ".0.0";

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
        apiParamGroup = new ApiParameterGroup("ApiGroup", null, providerParams, Collections.emptyList());
        ParameterService.register(apiParamGroup, true);
        operationalPolicyProvider = new LegacyOperationalPolicyProvider();
        policyTypeProvider = new PolicyTypeProvider();
    }

    /**
     * Closes up DB connections and deregisters API parameter group.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    @After
    public void tearDown() throws PfModelException {

        operationalPolicyProvider.close();
        policyTypeProvider.close();
        ParameterService.deregister(apiParamGroup);
    }

    @Test
    public void testFetchOperationalPolicy() throws Exception {

        assertThatThrownBy(() -> {
            operationalPolicyProvider.fetchOperationalPolicy("dummy", null);
        }).hasMessage("no policy found for policy: dummy:null");

        assertThatThrownBy(() -> {
            operationalPolicyProvider.fetchOperationalPolicy("dummy", "dummy");
        }).hasMessage("legacy policy version is not an integer");

        ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder.decode(
                ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
        policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
        LegacyOperationalPolicy policyToCreate = standardCoder.decode(policyString, LegacyOperationalPolicy.class);
        LegacyOperationalPolicy createdPolicy = operationalPolicyProvider.createOperationalPolicy(policyToCreate);
        assertNotNull(createdPolicy);

        LegacyOperationalPolicy firstVersion =
                operationalPolicyProvider.fetchOperationalPolicy("operational.restart", "1");
        assertNotNull(firstVersion);
        assertEquals("1", firstVersion.getPolicyVersion());

        LegacyOperationalPolicy latestVersion =
                operationalPolicyProvider.fetchOperationalPolicy("operational.restart", null);
        assertNotNull(latestVersion);
        assertEquals("1", latestVersion.getPolicyVersion());

        assertThatThrownBy(() -> {
            operationalPolicyProvider.fetchOperationalPolicy("operational.restart", "1.0.0");
        }).hasMessage("legacy policy version is not an integer");

        assertThatThrownBy(() -> {
            operationalPolicyProvider.fetchOperationalPolicy("operational.restart", "latest");;
        }).hasMessage("legacy policy version is not an integer");

        operationalPolicyProvider.deleteOperationalPolicy("operational.restart", "1");
        policyTypeProvider.deletePolicyType("onap.policies.controlloop.Operational", "1.0.0");
    }

    @Test
    public void testFetchDeployedOperationalPolicies() {

        assertThatThrownBy(() -> {
            operationalPolicyProvider.fetchDeployedOperationalPolicies("dummy");
        }).hasMessage("could not find policy with ID dummy and type " + POLICY_TYPE_ID + " deployed in any pdp group");

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
            pdpSubGroup.getSupportedPolicyTypes()
                    .add(new ToscaPolicyTypeIdentifier(POLICY_TYPE_NAME, POLICY_TYPE_VERSION));
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
                ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder.decode(
                        ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
                policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
            }).doesNotThrowAnyException();

            // Create Policy
            assertThatCode(() -> {
                String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
                LegacyOperationalPolicy policyToCreate =
                        standardCoder.decode(policyString, LegacyOperationalPolicy.class);
                LegacyOperationalPolicy policyCreated =
                        operationalPolicyProvider.createOperationalPolicy(policyToCreate);
                assertEquals("operational.restart", policyCreated.getPolicyId());
                assertEquals("1", policyCreated.getPolicyVersion());
                assertFalse(policyCreated.getContent() == null);
            }).doesNotThrowAnyException();

            // Test fetchDeployedPolicies (deployedPolicyMap.isEmpty())==true
            assertThatThrownBy(() -> {
                operationalPolicyProvider.fetchDeployedOperationalPolicies(POLICY_NAME);
            })  .hasMessage("could not find policy with ID " + POLICY_NAME + " and type " + POLICY_TYPE_ID
                    + " deployed in any pdp group");


            // Update pdpSubGroup
            pdpSubGroup.setPolicies(new ArrayList<>());
            pdpSubGroup.getPolicies()
                    .add(new ToscaPolicyIdentifier(POLICY_NAME, POLICY_VERSION + LEGACY_MINOR_PATCH_SUFFIX));
            assertEquals(1,
                    databaseProvider.createPdpGroups(groupList).get(0).getPdpSubgroups().get(0).getPolicies().size());

            // Test fetchDeployedPolicies
            assertThatCode(() -> {
                operationalPolicyProvider.fetchDeployedOperationalPolicies(POLICY_NAME);
            }).doesNotThrowAnyException();

            // Test validateDeleteEligibility exception path(!pdpGroups.isEmpty())
            assertThatThrownBy(() -> {
                operationalPolicyProvider.deleteOperationalPolicy(POLICY_NAME, POLICY_VERSION);
            })  .hasMessageContaining("policy with ID " + POLICY_NAME + ":" + POLICY_VERSION
                    + " cannot be deleted as it is deployed in pdp groups");
        } catch (Exception exc) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testCreateOperationalPolicy() {

        assertThatThrownBy(() -> {
            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            LegacyOperationalPolicy policyToCreate = standardCoder.decode(policyString, LegacyOperationalPolicy.class);
            operationalPolicyProvider.createOperationalPolicy(policyToCreate);
        }).hasMessage("policy type " + POLICY_TYPE_ID + " for policy " + POLICY_ID + " does not exist");

        assertThatCode(() -> {
            ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder.decode(
                    ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            LegacyOperationalPolicy policyToCreate = standardCoder.decode(policyString, LegacyOperationalPolicy.class);
            LegacyOperationalPolicy createdPolicy = operationalPolicyProvider.createOperationalPolicy(policyToCreate);
            assertNotNull(createdPolicy);
            assertEquals("operational.restart", createdPolicy.getPolicyId());
            assertTrue(createdPolicy.getContent().startsWith("controlLoop%3A%0A%20%20version%3A%202.0.0%0A%20%20"));
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeleteOperationalPolicyException() {
        String policyId = "operational.restart";
        String policyVersion = "1";
        String policyTypeVersion = "1.0.0";
        String policyTypeId = "onap.policies.controlloop.Operational";
        String legacyMinorPatchSuffix = ".0.0";

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
            assertEquals(123, databaseProvider.createPdpGroups(groupList).get(0).getPdpSubgroups().get(0)
                    .getDesiredInstanceCount());
            assertEquals(1, databaseProvider.getPdpGroups("group").size());

            // Create Policy Type
            assertThatCode(() -> {
                ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder.decode(
                        ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
                policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
            }).doesNotThrowAnyException();

            // Create Policy
            assertThatCode(() -> {
                String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
                LegacyOperationalPolicy policyToCreate =
                        standardCoder.decode(policyString, LegacyOperationalPolicy.class);
                LegacyOperationalPolicy createdPolicy =
                        operationalPolicyProvider.createOperationalPolicy(policyToCreate);
                assertNotNull(createdPolicy);
            }).doesNotThrowAnyException();

            // Update pdpSubGroup
            pdpSubGroup.setPolicies(new ArrayList<>());
            pdpSubGroup.getPolicies().add(new ToscaPolicyIdentifier(policyId, policyVersion + legacyMinorPatchSuffix));
            assertEquals(1,
                    databaseProvider.createPdpGroups(groupList).get(0).getPdpSubgroups().get(0).getPolicies().size());
            assertThatThrownBy(() -> {
                operationalPolicyProvider.deleteOperationalPolicy(policyId, policyVersion);
            }).hasMessageContaining("cannot be deleted as it is deployed in pdp groups");
        } catch (Exception exc) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testDeleteOperationalPolicy() {

        assertThatThrownBy(() -> {
            operationalPolicyProvider.deleteOperationalPolicy("dummy", null);
        }).hasMessage("legacy policy version is not an integer");

        assertThatThrownBy(() -> {
            operationalPolicyProvider.deleteOperationalPolicy("dummy", "dummy");
        }).hasMessage("legacy policy version is not an integer");

        assertThatCode(() -> {
            ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder.decode(
                    ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE), ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            LegacyOperationalPolicy policyToCreate = standardCoder.decode(policyString, LegacyOperationalPolicy.class);
            LegacyOperationalPolicy createdPolicy = operationalPolicyProvider.createOperationalPolicy(policyToCreate);
            assertNotNull(createdPolicy);

            LegacyOperationalPolicy deletedPolicy =
                    operationalPolicyProvider.deleteOperationalPolicy("operational.restart", "1");
            assertNotNull(deletedPolicy);
            assertEquals("operational.restart", deletedPolicy.getPolicyId());
            assertTrue(deletedPolicy.getContent().startsWith("controlLoop%3A%0A%20%20version%3A%202.0.0%0A%20%20"));
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            operationalPolicyProvider.deleteOperationalPolicy("operational.restart", "1");
        }).hasMessage("no policy found for policy: operational.restart:1");

        assertThatCode(() -> {
            policyTypeProvider.deletePolicyType("onap.policies.controlloop.Operational", "1.0.0");
        }).doesNotThrowAnyException();
    }
}
