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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.policy.api.main.validator.PolicyValidatorParameters;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientConfigException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * Test the {@link DefaultPolicyValidator} class.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 *
 */
public class DefaultPolicyValidatorImplTest {

    private static final String VALID_NATIVE_DROOLS_POLICY =
            "policies/example.policy.native.drools.valid.input.tosca.json";
    private static final String INVALID_NATIVE_DROOLS_POLICY =
            "policies/example.policy.native.drools.invalid.input.tosca.json";
    private static final String GROUP_ID = "org.onap.policy.native.drools";
    private static final String ARTIFACT_ID = "example-valid-drools-policy";
    private static final String VERSION = "1.0.0-SNAPSHOT";
    private static final String RELEASE = "releases";
    private static final String PATH =
            "resolve?r=" + RELEASE + "&g=" + GROUP_ID + "&a=" + ARTIFACT_ID + "&v=" + VERSION;

    private static final StandardCoder standardCoder = new StandardCoder();

    private PolicyValidatorParameters params = new PolicyValidatorParameters();

    @Test
    public void testConstructor() throws HttpClientConfigException {

        assertThatThrownBy(() -> {
            new DefaultPolicyValidatorImpl(null);
        }).hasMessage("parameters is marked @NonNull but is null");

        DefaultPolicyValidatorImpl validator = new DefaultPolicyValidatorImpl(params);
        assertEquals(params.getName(), validator.getParameters().getName());
        assertEquals(params.getImplementation(), validator.getParameters().getImplementation());
        assertEquals(params.getNexusName(), validator.getParameters().getNexusName());
        assertEquals(params.getNexusPort(), validator.getParameters().getNexusPort());
        assertNotNull(validator.getCoder());
        assertNotNull(validator.getClient());
    }

    @Test
    public void testValidateNativePolicies() throws Exception {

        ToscaServiceTemplate serviceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString(INVALID_NATIVE_DROOLS_POLICY), ToscaServiceTemplate.class);
        DefaultPolicyValidatorImpl validator = new DefaultPolicyValidatorImpl(params);

        String errMsg = "rule artifact org.onap.policy.native.drools|example-invalid-drools-policy|1.0.0-SNAPSHOT "
                + "not found";
        assertThatThrownBy(() -> {
            validator.validateNativePolicies(serviceTemplate.getToscaTopologyTemplate().getPolicies());
        }).hasMessage(errMsg);
    }

    @Test
    public void testValidateDroolsPolicy() throws Exception {

        HttpClient mockedClient = mock(HttpClient.class);

        when(mockedClient.get(PATH)).thenAnswer(
            new Answer<Response>() {
                @Override
                public Response answer(InvocationOnMock invocation) {
                    return Response.status(Response.Status.OK).build();
                }
            });

        ToscaServiceTemplate serviceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString(VALID_NATIVE_DROOLS_POLICY), ToscaServiceTemplate.class);
        DefaultPolicyValidatorImpl validator = new DefaultPolicyValidatorImpl(params);
        ToscaPolicy droolsPolicy = serviceTemplate.getToscaTopologyTemplate().getPolicies().iterator().next()
                .entrySet().iterator().next().getValue();

        validator.validateDroolsPolicy(droolsPolicy, mockedClient);
    }
}