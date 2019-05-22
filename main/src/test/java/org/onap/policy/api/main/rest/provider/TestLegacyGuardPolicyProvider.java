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
import static org.junit.Assert.assertTrue;

import java.util.Base64;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;

/**
 * This class performs unit test of {@link LegacyGuardPolicyProvider}
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class TestLegacyGuardPolicyProvider {

    private static LegacyGuardPolicyProvider guardPolicyProvider;
    private static PolicyTypeProvider policyTypeProvider;
    private static PolicyModelsProviderParameters providerParams;
    private static ApiParameterGroup apiParamGroup;
    private static StandardCoder standardCoder;

    private static final String POLICY_RESOURCE = "policies/vDNS.policy.guard.frequency.input.json";
    private static final String POLICY_RESOURCE_VER1 = "policies/vDNS.policy.guard.frequency.input.ver1.json";
    private static final String POLICY_RESOURCE_VER2 = "policies/vDNS.policy.guard.frequency.input.ver2.json";
    private static final String POLICY_TYPE_RESOURCE =
            "policytypes/onap.policies.controlloop.guard.FrequencyLimiter.json";
    private static final String POLICY_TYPE_ID = "onap.policies.controlloop.guard.FrequencyLimiter:1.0.0";
    private static final String POLICY_ID = "guard.frequency.scaleout:1.0.0";

    /**
     * Initializes parameters.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    @BeforeClass
    public static void setupParameters() throws PfModelException {

        standardCoder = new StandardCoder();
        providerParams = new PolicyModelsProviderParameters();
        providerParams.setDatabaseDriver("org.h2.Driver");
        providerParams.setDatabaseUrl("jdbc:h2:mem:testdb");
        providerParams.setDatabaseUser("policy");
        providerParams.setDatabasePassword(Base64.getEncoder().encodeToString("P01icY".getBytes()));
        providerParams.setPersistenceUnit("ToscaConceptTest");
        apiParamGroup = new ApiParameterGroup("ApiGroup", null, providerParams);
        ParameterService.register(apiParamGroup, true);
        guardPolicyProvider = new LegacyGuardPolicyProvider();
        policyTypeProvider = new PolicyTypeProvider();
    }

    /**
     * Closes up DB connections and deregisters API parameter group.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    @AfterClass
    public static void tearDown() throws PfModelException {

        guardPolicyProvider.close();
        policyTypeProvider.close();
        ParameterService.deregister(apiParamGroup);
    }


    @Test
    public void testFetchGuardPolicy() {

        assertThatThrownBy(() -> {
            guardPolicyProvider.fetchGuardPolicy("dummy", null);
        }).hasMessage("no policy found for policy: dummy:null");

        assertThatThrownBy(() -> {
            guardPolicyProvider.fetchGuardPolicy("dummy", "dummy");
        }).hasMessage("legacy policy version is not an integer");

        assertThatCode(() -> {
            String policyTypeString = ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE);
            ToscaServiceTemplate policyTypeServiceTemplate =
                    standardCoder.decode(policyTypeString, ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_VER1);
            LegacyGuardPolicyInput policyToCreate = standardCoder.decode(policyString, LegacyGuardPolicyInput.class);
            Map<String, LegacyGuardPolicyOutput> createdPolicy = guardPolicyProvider.createGuardPolicy(policyToCreate);
            assertNotNull(createdPolicy);
            assertFalse(createdPolicy.isEmpty());

            policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE_VER2);
            policyToCreate = standardCoder.decode(policyString, LegacyGuardPolicyInput.class);
            createdPolicy = guardPolicyProvider.createGuardPolicy(policyToCreate);
            assertNotNull(createdPolicy);
            assertFalse(createdPolicy.isEmpty());

            Map<String, LegacyGuardPolicyOutput> firstVersion =
                    guardPolicyProvider.fetchGuardPolicy("guard.frequency.scaleout", "1");
            assertNotNull(firstVersion);
            assertEquals("1",
                    firstVersion.get("guard.frequency.scaleout").getMetadata().get("policy-version").toString());

            Map<String, LegacyGuardPolicyOutput> latestVersion =
                    guardPolicyProvider.fetchGuardPolicy("guard.frequency.scaleout", null);
            assertNotNull(latestVersion);
            assertEquals("2",
                    latestVersion.get("guard.frequency.scaleout").getMetadata().get("policy-version").toString());
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            guardPolicyProvider.fetchGuardPolicy("guard.frequency.scaleout", "1.0.0");
        }).hasMessage("legacy policy version is not an integer");

        assertThatThrownBy(() -> {
            guardPolicyProvider.fetchGuardPolicy("guard.frequency.scaleout", "latest");
        }).hasMessage("legacy policy version is not an integer");

        assertThatCode(() -> {
            guardPolicyProvider.deleteGuardPolicy("guard.frequency.scaleout", "1");
            guardPolicyProvider.deleteGuardPolicy("guard.frequency.scaleout", "2");
            policyTypeProvider.deletePolicyType("onap.policies.controlloop.guard.FrequencyLimiter", "1.0.0");
        }).doesNotThrowAnyException();
    }

    @Test
    public void testCreateGuardPolicy() {

        assertThatThrownBy(() -> {
            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            LegacyGuardPolicyInput policyToCreate = standardCoder.decode(policyString, LegacyGuardPolicyInput.class);
            guardPolicyProvider.createGuardPolicy(policyToCreate);
        }).hasMessage("policy type " + POLICY_TYPE_ID + " for policy " + POLICY_ID + " does not exist");

        assertThatCode(() -> {
            String policyTypeString = ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE);
            ToscaServiceTemplate policyTypeServiceTemplate =
                    standardCoder.decode(policyTypeString, ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            LegacyGuardPolicyInput policyToCreate = standardCoder.decode(policyString, LegacyGuardPolicyInput.class);
            Map<String, LegacyGuardPolicyOutput> createdPolicy = guardPolicyProvider.createGuardPolicy(policyToCreate);
            assertNotNull(createdPolicy);
            assertFalse(createdPolicy.isEmpty());
            assertTrue(createdPolicy.containsKey("guard.frequency.scaleout"));
            assertEquals("onap.policies.controlloop.guard.FrequencyLimiter",
                    createdPolicy.get("guard.frequency.scaleout").getType());
            assertEquals("1.0.0", createdPolicy.get("guard.frequency.scaleout").getVersion());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeleteGuardPolicy() {

        assertThatThrownBy(() -> {
            guardPolicyProvider.deleteGuardPolicy("dummy", null);
        }).hasMessage("version is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            guardPolicyProvider.deleteGuardPolicy("dummy", "1.0.0");
        }).hasMessage("legacy policy version is not an integer");

        assertThatCode(() -> {
            String policyTypeString = ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE);
            ToscaServiceTemplate policyTypeServiceTemplate =
                    standardCoder.decode(policyTypeString, ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            LegacyGuardPolicyInput policyToCreate = standardCoder.decode(policyString, LegacyGuardPolicyInput.class);
            Map<String, LegacyGuardPolicyOutput> createdPolicy = guardPolicyProvider.createGuardPolicy(policyToCreate);
            assertNotNull(createdPolicy);
            assertFalse(createdPolicy.isEmpty());

            Map<String, LegacyGuardPolicyOutput> deletedPolicy = guardPolicyProvider
                    .deleteGuardPolicy("guard.frequency.scaleout", "1");
            assertNotNull(deletedPolicy);
            assertFalse(deletedPolicy.isEmpty());
            assertTrue(deletedPolicy.containsKey("guard.frequency.scaleout"));
            assertEquals("onap.policies.controlloop.guard.FrequencyLimiter",
                    deletedPolicy.get("guard.frequency.scaleout").getType());
            assertEquals("1",
                    deletedPolicy.get("guard.frequency.scaleout").getMetadata().get("policy-version").toString());
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            guardPolicyProvider.deleteGuardPolicy("guard.frequency.scaleout", "1");
        }).hasMessage("no policy found for policy: guard.frequency.scaleout:1");

        assertThatCode(() -> {
            policyTypeProvider.deletePolicyType("onap.policies.controlloop.guard.FrequencyLimiter", "1.0.0");
        }).doesNotThrowAnyException();
    }
}
