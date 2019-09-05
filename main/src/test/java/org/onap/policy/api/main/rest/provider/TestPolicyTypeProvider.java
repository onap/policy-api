/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertFalse;

import java.util.Base64;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.api.main.ApiTestSupportUtilities;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * This class performs unit test of {@link PolicyTypeProvider}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class TestPolicyTypeProvider {

    private static PolicyTypeProvider policyTypeProvider;
    private static PolicyProvider policyProvider;
    private static PolicyModelsProviderParameters providerParams;
    private static ApiParameterGroup apiParamGroup;
    private static StandardCoder standardCoder;

    private static final String POLICY_RESOURCE = "policies/vCPE.policy.monitoring.input.tosca.yaml";
    private static final String POLICY_TYPE_RESOURCE = "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.yaml";

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
        policyTypeProvider = new PolicyTypeProvider();
        policyProvider = new PolicyProvider();
    }

    /**
     * Closes up DB connections and deregisters API parameter group.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    @AfterClass
    public static void tearDown() throws PfModelException {

        policyTypeProvider.close();
        policyProvider.close();
        ParameterService.deregister(apiParamGroup);
    }

    @Test
    public void testFetchPolicyTypes() {

        assertThatCode(() -> {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.fetchPolicyTypes(null, null);
            assertFalse(serviceTemplate.getPolicyTypes().isEmpty());
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            policyTypeProvider.fetchPolicyTypes("dummy", null);
        }).hasMessage("policy type with ID dummy:null does not exist");

        assertThatThrownBy(() -> {
            policyTypeProvider.fetchPolicyTypes("dummy", "dummy");
        }).hasMessage("policy type with ID dummy:dummy does not exist");
    }

    @Test
    public void testFetchLatestPolicyTypes() {

        assertThatThrownBy(() -> {
            policyTypeProvider.fetchLatestPolicyTypes("dummy");
        }).hasMessage("policy type with ID dummy:null does not exist");
    }

    @Test
    public void testCreatePolicyType() {

        assertThatCode(() -> {
            String policyTypeString =
                    ApiTestSupportUtilities.yaml2Json(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE));
            ToscaServiceTemplate policyTypeServiceTemplate =
                    standardCoder.decode(policyTypeString, ToscaServiceTemplate.class);
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
            assertFalse(serviceTemplate.getPolicyTypes().isEmpty());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeletePolicyType() {

        assertThatCode(() -> {
            String policyTypeString =
                    ApiTestSupportUtilities.yaml2Json(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE));
            ToscaServiceTemplate policyTypeServiceTemplate =
                    standardCoder.decode(policyTypeString, ToscaServiceTemplate.class);
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
            assertFalse(serviceTemplate.getPolicyTypes().isEmpty());
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            String policyString = ApiTestSupportUtilities.yaml2Json(ResourceUtils.getResourceAsString(POLICY_RESOURCE));
            ToscaServiceTemplate policyServiceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);
            policyProvider.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);
        }).doesNotThrowAnyException();

        String exceptionMessage = "policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 "
                + "cannot be deleted as it is parameterized by policies onap.restart.tca:1.0.0";
        assertThatThrownBy(() -> {
            policyTypeProvider.deletePolicyType("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0");
        }).hasMessage(exceptionMessage);

        assertThatCode(() -> {
            ToscaServiceTemplate serviceTemplate = policyProvider
                    .deletePolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", "onap.restart.tca", "1.0.0");
            assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            ToscaServiceTemplate serviceTemplate =
                    policyTypeProvider.deletePolicyType("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0");
            assertFalse(serviceTemplate.getPolicyTypes().isEmpty());
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> {
            policyTypeProvider.deletePolicyType("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0");
        }).hasMessage("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist");
    }
}
