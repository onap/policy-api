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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Base64;
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
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

/**
 * This class performs unit test of {@link LegacyOperationalPolicyProvider}
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class TestLegacyOperationalPolicyProvider {

    private static LegacyOperationalPolicyProvider operationalPolicyProvider;
    private static PolicyTypeProvider policyTypeProvider;
    private static PolicyModelsProviderParameters providerParams;
    private static ApiParameterGroup apiParamGroup;
    private static StandardCoder standardCoder;

    private static final String POLICY_RESOURCE = "policies/vCPE.policy.operational.input.json";
    private static final String POLICY_TYPE_RESOURCE = "policytypes/onap.policies.controlloop.Operational.json";

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
        operationalPolicyProvider = new LegacyOperationalPolicyProvider();
        policyTypeProvider = new PolicyTypeProvider();
    }

    /**
     * Closes up DB connections and deregisters API parameter group.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    @AfterClass
    public static void tearDown() throws PfModelException {

        operationalPolicyProvider.close();
        ParameterService.deregister(apiParamGroup);
    }

    @Test
    public void testFetchOperationalPolicy() {

        assertThatThrownBy(() -> {
            operationalPolicyProvider.fetchOperationalPolicy("dummy", null);
        }).hasMessage("no policy found for policy ID: dummy");

        assertThatThrownBy(() -> {
            operationalPolicyProvider.fetchOperationalPolicy("dummy", "dummy");
        }).hasMessage("no policy found for policy ID: dummy");
    }

    @Test
    public void testCreateOperationalPolicy() {

        assertThatThrownBy(() -> {
            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            LegacyOperationalPolicy policyToCreate = standardCoder.decode(policyString, LegacyOperationalPolicy.class);
            operationalPolicyProvider.createOperationalPolicy(policyToCreate);
        }).hasMessage("policy type onap.policies.controlloop.Operational:1.0.0 for "
            + "policy operational.restart:1.0.0 does not exist");

        assertThatCode(() -> {
            String policyTypeString = ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE);
            ToscaServiceTemplate policyTypeServiceTemplate =
                    standardCoder.decode(policyTypeString, ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            LegacyOperationalPolicy policyToCreate = standardCoder.decode(policyString, LegacyOperationalPolicy.class);
            LegacyOperationalPolicy createdPolicy = operationalPolicyProvider.createOperationalPolicy(policyToCreate);
            assertNotNull(createdPolicy);
            assertEquals("operational.restart", createdPolicy.getPolicyId());
            assertTrue(createdPolicy.getContent()
                    .startsWith("controlLoop%3A%0A%20%20version%3A%202.0.0%0A%20%20"));
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeleteOperationalPolicy() {

        assertThatThrownBy(() -> {
            operationalPolicyProvider.deleteOperationalPolicy("dummy", null);
        }).hasMessage("version is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            operationalPolicyProvider.deleteOperationalPolicy("dummy", "dummy");
        }).hasMessage("no policy found for policy ID: dummy");

        assertThatCode(() -> {
            String policyTypeString = ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE);
            ToscaServiceTemplate policyTypeServiceTemplate =
                    standardCoder.decode(policyTypeString, ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            String policyString = ResourceUtils.getResourceAsString(POLICY_RESOURCE);
            LegacyOperationalPolicy policyToCreate = standardCoder.decode(policyString, LegacyOperationalPolicy.class);
            LegacyOperationalPolicy createdPolicy = operationalPolicyProvider.createOperationalPolicy(policyToCreate);
            assertNotNull(createdPolicy);
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            LegacyOperationalPolicy deletedPolicy = operationalPolicyProvider
                    .deleteOperationalPolicy("operational.restart", "1.0.0");
            assertNotNull(deletedPolicy);
            assertEquals("operational.restart", deletedPolicy.getPolicyId());
            assertTrue(deletedPolicy.getContent()
                    .startsWith("controlLoop%3A%0A%20%20version%3A%202.0.0%0A%20%20"));
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            operationalPolicyProvider.deleteOperationalPolicy("operational.restart", "1.0.0");
        }).hasMessage("no policy found for policy ID: operational.restart");
    }
}
