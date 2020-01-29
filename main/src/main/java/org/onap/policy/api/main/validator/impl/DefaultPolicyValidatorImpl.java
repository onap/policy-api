/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.api.main.validator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.NonNull;
import org.onap.policy.api.main.validator.PolicyValidator;
import org.onap.policy.api.main.validator.PolicyValidatorParameters;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientConfigException;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * Class to implement a default policy validator that can be used in policy API.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Getter
public class DefaultPolicyValidatorImpl implements PolicyValidator {

    private static final String NEXUS_BASEPATH = "nexus/service/local/artifact/maven";
    private static final String DOT_NEXUS = "." + "NEXUS";
    private static final String PROPERTY_RULE_ARTIFACT = "rule_artifact";
    private static final String GROUP_ID = "groupId";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String VERSION = "version";
    private static final String RELEASE = "releases";
    private static final String NATIVE_DROOLS_POLICYTYPE = "onap.policies.native.Drools";
    private static final String NATIVE_XACML_POLICYTYPE = "onap.policies.native.Xacml";
    private static final String NATIVE_APEX_POLICYTYPE = "onap.policies.native.Apex";

    private PolicyValidatorParameters parameters;
    private StandardCoder coder;
    private HttpClient client;

    /**
     * Constructor that takes the parameters.
     * @param parameters the parameters for the policy validator
     * @throws HttpClientConfigException on errors instantiating a http client
     */
    public DefaultPolicyValidatorImpl(@NonNull final PolicyValidatorParameters parameters)
            throws HttpClientConfigException {
        this.parameters = parameters;
        this.coder = new StandardCoder();
        this.client = getNoAuthHttpClient(false);
    }

    @Override
    public void validateNativePolicies(final List<Map<String, ToscaPolicy>> policies) throws PfModelException {

        List<Map<String, ToscaPolicy>> nativePolicies = filterNativePolicies(policies, NATIVE_DROOLS_POLICYTYPE);
        validateDroolsPolicies(nativePolicies);
        nativePolicies = filterNativePolicies(policies, NATIVE_XACML_POLICYTYPE);
        validateXacmlPolicies(nativePolicies);
        nativePolicies = filterNativePolicies(policies, NATIVE_APEX_POLICYTYPE);
        validateApexPolicies(nativePolicies);
    }

    @Override
    public void validatePoliciesAgainstPolicyTypes(final List<Map<String, ToscaPolicy>> policies)
            throws PfModelException {

        // TODO to implement here or in models repo or call Liam's validation function in models repo
    }

    @Override
    public void validateDroolsPolicies(final List<Map<String, ToscaPolicy>> droolsPolicies) throws PfModelException {

        if (droolsPolicies.isEmpty()) {
            return;
        }
        for (Map<String, ToscaPolicy> policy : droolsPolicies) {
            // Each map here should have only one policy entry
            validateDroolsPolicy(policy.entrySet().iterator().next().getValue(), client);
        }
    }

    /**
     * Validates individual TOSCA compliant native drools policy.
     * @param droolsPolicy the drools policy to validate
     * @throws PfModelException on errors validating the policy
     */
    public void validateDroolsPolicy(final ToscaPolicy droolsPolicy, HttpClient client) throws PfModelException {

        // Extract GAV information of the artifact
        // Assume the existence of required fields have been checked by validatePolicyAgainstPolicyType
        // method which should be called before this
        String groupId;
        String artifactId;
        String version;
        try {
            groupId = coder.toStandard(droolsPolicy.getProperties().get(PROPERTY_RULE_ARTIFACT)).getString(GROUP_ID);
            artifactId =
                    coder.toStandard(droolsPolicy.getProperties().get(PROPERTY_RULE_ARTIFACT)).getString(ARTIFACT_ID);
            version = coder.toStandard(droolsPolicy.getProperties().get(PROPERTY_RULE_ARTIFACT)).getString(VERSION);
        } catch (CoderException exc) {
            String errMsg = "errors on extracting GAV information from native drools policy";
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errMsg, exc);
        }

        String path = "resolve?r=" + RELEASE + "&g=" + groupId + "&a=" + artifactId + "&v=" + version;
        Response response = client.get(path);
        int code = response.getStatus();
        switch (code) {
            case 404:
                String errMsg = "rule artifact " + groupId + "|" + artifactId + "|" + version + " not found";
                throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errMsg);
            case 200:
                break;
            default:
                throw new PfModelException(Response.Status.fromStatusCode(code),
                        HttpClient.getBody(response, String.class));
        }
    }

    @Override
    public void validateXacmlPolicies(final List<Map<String, ToscaPolicy>> xacmlPolicies) throws PfModelException {

        if (xacmlPolicies.isEmpty()) {
            return;
        }
        // TODO
    }

    @Override
    public void validateApexPolicies(final List<Map<String, ToscaPolicy>> apexPolicies) throws PfModelException {

        if (apexPolicies.isEmpty()) {
            return;
        }
        // TODO
    }

    /**
     * Creates a http client without requiring authentication to access the nexus.
     * @param https the flag to indicate whether use https or not
     * @return the http client
     * @throws HttpClientConfigException on errors creating the http client
     */
    private HttpClient getNoAuthHttpClient(boolean https) throws HttpClientConfigException {

        final Properties httpProperties = new Properties();

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES, "NEXUS");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_NEXUS
                        + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, parameters.getNexusName());
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_NEXUS
                        + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX, parameters.getNexusPort());
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_NEXUS
                        + PolicyEndPointProperties.PROPERTY_HTTP_URL_SUFFIX, NEXUS_BASEPATH);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_NEXUS
                        + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX, Boolean.toString(https));
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_NEXUS
                        + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");

        List<HttpClient> clients = HttpClientFactoryInstance.getClientFactory().build(httpProperties);
        if (clients.size() != 1) {
            throw new HttpClientConfigException("we should allow one http client being instantiated for nexus access");
        }
        return clients.iterator().next();
    }

    /**
     * Constructs a map which only includes the native policies.
     * @param policies the entire TOSCA compliant policy set to filter
     * @param nativePolicyType the native policy type that can be used for filtering a particular native policy
     * @return the filtered native policy set
     */
    private List<Map<String, ToscaPolicy>> filterNativePolicies(
            final List<Map<String, ToscaPolicy>> policies, final String nativePolicyType) {

        List<Map<String, ToscaPolicy>> filteredPolicies = new ArrayList<>();
        for (Map<String, ToscaPolicy> policy : policies) {
            if (nativePolicyType.equals(policy.entrySet().iterator().next().getValue().getType())) {
                filteredPolicies.add(policy);
            }
        }
        return filteredPolicies;
    }
}