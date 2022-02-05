/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2021 Nordix Foundation.
 * Modifications Copyright (C) 2020-2022 Bell Canada. All rights reserved.
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
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.policy.api.main.PolicyApiApplication;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This class performs unit test of {@link PolicyTypeProvider}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PolicyApiApplication.class, properties = {"database.initialize=false"})
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TestPolicyTypeProvider {

    private static StandardYamlCoder standardYamlCoder = new StandardYamlCoder();

    @Autowired
    private PolicyProvider policyProvider;

    @Autowired
    private PolicyTypeProvider policyTypeProvider;

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

    @Test
    public void testFetchPolicyTypes() throws Exception {

        ToscaServiceTemplate serviceTemplate = policyTypeProvider.fetchPolicyTypes(null, null);
        assertFalse(serviceTemplate.getPolicyTypes().isEmpty());

        assertThatThrownBy(() -> {
            policyTypeProvider.fetchPolicyTypes("dummy", null);
        }).hasMessage("policy types for filter ToscaEntityFilter(name=dummy, version=null) do not exist");

        assertThatThrownBy(() -> {
            policyTypeProvider.fetchPolicyTypes("dummy", "dummy");
        }).hasMessage("policy types for filter ToscaEntityFilter(name=dummy, version=dummy) do not exist");
    }

    @Test
    public void testFetchLatestPolicyTypes() {

        assertThatThrownBy(() -> {
            policyTypeProvider.fetchLatestPolicyTypes("dummy");
        }).hasMessage("policy types for filter ToscaEntityFilter(name=dummy, version=LATEST) do not exist");
    }

    @Test
    public void testCreatePolicyType() throws Exception {

        ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder
                .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_MONITORING), ToscaServiceTemplate.class);
        ToscaServiceTemplate serviceTemplate = policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        assertFalse(serviceTemplate.getPolicyTypes().isEmpty());

        assertThatCode(() -> {
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        }).doesNotThrowAnyException();

        ToscaPolicyType policyType = policyTypeServiceTemplate.getPolicyTypes().get("onap.policies.monitoring.tcagen2");
        policyType.setDescription("Some other description");

        assertThatThrownBy(() -> {
            policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        }).hasMessageContaining("item \"entity\" value \"onap.policies.monitoring.tcagen2:1.0.0\" INVALID, "
                + "does not equal existing entity");

        assertThatThrownBy(() -> {
            ToscaServiceTemplate badPolicyType =
                    standardYamlCoder.decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_WITH_NO_VERSION),
                            ToscaServiceTemplate.class);
            policyTypeProvider.createPolicyType(badPolicyType);
        }).hasMessageContaining("item \"version\" value \"0.0.0\" INVALID, is null");

        policyTypeProvider.deletePolicyType(POLICY_TYPE_NAME_MONITORING, POLICY_TYPE_VERSION);
    }

    @Test
    public void testCreateOperationalPolicyTypes() throws CoderException, PfModelException {
        ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder.decode(
                ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_COMMON), ToscaServiceTemplate.class);
        ToscaServiceTemplate serviceTemplate = policyTypeProvider.createPolicyType(policyTypeServiceTemplate);

        assertNotNull(serviceTemplate.getPolicyTypes().get(POLICY_TYPE_OPERATIONAL_COMMON));

        policyTypeServiceTemplate = standardYamlCoder.decode(
                ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_DROOLS), ToscaServiceTemplate.class);
        serviceTemplate = policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        assertNotNull(serviceTemplate.getPolicyTypes().get(POLICY_TYPE_OPERATIONAL_DROOLS));

        policyTypeProvider.deletePolicyType(POLICY_TYPE_OPERATIONAL_DROOLS, POLICY_TYPE_VERSION);
        policyTypeProvider.deletePolicyType(POLICY_TYPE_OPERATIONAL_COMMON, POLICY_TYPE_VERSION);
    }

    @Test
    public void testCreateApexOperationalPolicyTypes() throws CoderException, PfModelException {
        ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder.decode(
                ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_COMMON), ToscaServiceTemplate.class);
        ToscaServiceTemplate serviceTemplate = policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        policyTypeServiceTemplate = standardYamlCoder.decode(
                ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_OPERATIONAL_APEX), ToscaServiceTemplate.class);
        serviceTemplate = policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        assertNotNull(serviceTemplate.getPolicyTypes().get(POLICY_TYPE_OPERATIONAL_APEX));
        policyTypeProvider.deletePolicyType(POLICY_TYPE_OPERATIONAL_APEX, POLICY_TYPE_VERSION);
    }

    @Test
    public void testDeletePolicyType() throws Exception {

        ToscaServiceTemplate policyTypeServiceTemplate = standardYamlCoder
                .decode(ResourceUtils.getResourceAsString(POLICY_TYPE_RESOURCE_MONITORING), ToscaServiceTemplate.class);
        ToscaServiceTemplate serviceTemplate = policyTypeProvider.createPolicyType(policyTypeServiceTemplate);
        assertFalse(serviceTemplate.getPolicyTypes().isEmpty());

        ToscaServiceTemplate policyServiceTemplate = standardYamlCoder
                .decode(ResourceUtils.getResourceAsString(POLICY_RESOURCE_MONITORING), ToscaServiceTemplate.class);
        policyProvider.createPolicy("onap.policies.monitoring.cdap.tca.hi.lo.app", "1.0.0", policyServiceTemplate);

        String exceptionMessage = "policy type onap.policies.monitoring.tcagen2:1.0.0 is in use, "
                + "it is referenced in policy onap.restart.tca:1.0.0";
        assertThatThrownBy(() -> {
            policyTypeProvider.deletePolicyType("onap.policies.monitoring.tcagen2", "1.0.0");
        }).hasMessage(exceptionMessage);

        serviceTemplate =
                policyProvider.deletePolicy("onap.policies.monitoring.tcagen2", "1.0.0", "onap.restart.tca", "1.0.0");
        assertFalse(serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty());

        serviceTemplate = policyTypeProvider.deletePolicyType("onap.policies.monitoring.tcagen2", "1.0.0");
        assertFalse(serviceTemplate.getPolicyTypes().isEmpty());

        assertThatThrownBy(() -> {
            policyTypeProvider.deletePolicyType("onap.policies.monitoring.tcagen2", "1.0.0");
        }).hasMessage("policy type onap.policies.monitoring.tcagen2:1.0.0 not found");
    }
}