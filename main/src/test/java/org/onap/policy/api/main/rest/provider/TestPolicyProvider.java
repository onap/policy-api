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
import static org.junit.Assert.assertFalse;

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
    private static final String POLICY_TYPE_RESOURCE = "policytypes/onap.policy.monitoring.cdap.tca.hi.lo.app.json";
    private static final String POLICY_RESOURCE_WITH_BAD_POLICYTYPE_ID = "policies/vCPE.policy.bad.policytypeid.json";
    private static final String POLICY_RESOURCE_WITH_BAD_POLICYTYPE_VERSION =
            "policies/vCPE.policy.bad.policytypeversion.json";

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
    public void testFetchPolicies() {

        assertThatThrownBy(() -> {
            policyProvider.fetchPolicies("dummy", "dummy", null, null);
        }).hasMessage("policy with ID null:null and type dummy:dummy does not exist");

        assertThatThrownBy(() -> {
            policyProvider.fetchPolicies("dummy", "dummy", "dummy", null);
        }).hasMessage("policy with ID dummy:null and type dummy:dummy does not exist");

        assertThatThrownBy(() -> {
            policyProvider.fetchPolicies("dummy", "dummy", "dummy", "dummy");
        }).hasMessage("policy with ID dummy:dummy and type dummy:dummy does not exist");
    }

    @Test
    public void testFetchLatestPolicies() {

        assertThatThrownBy(() -> {
            policyProvider.fetchLatestPolicies("dummy", "dummy", "dummy");
        }).hasMessage("policy with ID dummy:null and type dummy:dummy does not exist");
    }

    @Test
    public void testFetchDeployedPolicies() {

        assertThatThrownBy(() -> {
            policyProvider.fetchDeployedPolicies("dummy", "dummy", "dummy");
        }).hasMessage("could not find policy with ID dummy and type dummy:dummy deployed in any pdp group");
    }

    @Test
    public void testCreatePolicy() {

        assertThatThrownBy(() -> {
            policyProvider.createPolicy("dummy", "dummy", new ToscaServiceTemplate());
        }).hasMessage("policy type with ID dummy:dummy does not exist");

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
            policyProvider.deletePolicy("dummy", "dummy", "dummy", "dummy");
        }).hasMessage("policy with ID dummy:dummy and type dummy:dummy does not exist");

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
