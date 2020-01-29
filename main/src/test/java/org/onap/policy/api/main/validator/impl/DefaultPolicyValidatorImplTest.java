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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.ws.rs.core.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.policy.api.main.validator.PolicyValidatorParameters;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientConfigException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the {@link DefaultPolicyValidator} class.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 *
 */
public class DefaultPolicyValidatorImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPolicyValidatorImplTest.class);

    private static final String VALID_NATIVE_DROOLS_POLICY =
            "policies/example.policy.native.drools.valid.input.tosca.json";
    private static final String INVALID_NATIVE_DROOLS_POLICY =
            "policies/example.policy.native.drools.invalid.input.tosca.json";
    private static final String GROUP_ID = "org.onap.policy.native.drools";
    private static final String VALID_ARTIFACT_ID = "example-valid-drools-policy";
    private static final String INVALID_ARTIFACT_ID = "example-invalid-drools-policy";
    private static final String VERSION = "1.0.0-SNAPSHOT";
    private static final String RELEASE = "releases";
    private static final String VALID_PATH =
            "resolve?r=" + RELEASE + "&g=" + GROUP_ID + "&a=" + VALID_ARTIFACT_ID + "&v=" + VERSION;
    private static final String INVALID_PATH =
            "resolve?r=" + RELEASE + "&g=" + GROUP_ID + "&a=" + INVALID_ARTIFACT_ID + "&v=" + VERSION;
    private static final String LOCALHOST = "localhost";

    private static StandardCoder standardCoder;
    private static PolicyValidatorParameters params;
    private static DefaultPolicyValidatorImpl validator;
    private static ServerSocket localSocket;
    private static int localPort;

    /**
     * Sets up the parameters for all the tests.
     *
     * @throws Exception on errors setting up initial parameters
     */
    @BeforeClass
    public static void initParameters() throws Exception {
        standardCoder = new StandardCoder();
        params = PolicyValidatorParameters.builder().nexusName(LOCALHOST).nexusPort("8081").build();
        validator = new DefaultPolicyValidatorImpl(params);

        localPort = NetworkUtil.allocPort(LOCALHOST);
        localSocket = new ServerSocket();
        localSocket.bind(new InetSocketAddress(LOCALHOST, localPort));
        new Accepter(localSocket).start();
    }

    /**
     * Closes the socket.
     *
     * @throws IOException on errors closing the socket
     */
    @AfterClass
    public static void close() throws IOException {
        localSocket.close();
    }

    @Test
    public void testConstructor() throws HttpClientConfigException {

        assertThatThrownBy(() -> {
            new DefaultPolicyValidatorImpl(null);
        }).hasMessage("parameters is marked @NonNull but is null");

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

        String errMsg = "errors on connecting to localhost:8081";
        assertThatThrownBy(() -> {
            validator.validateNativePolicies(serviceTemplate.getToscaTopologyTemplate().getPolicies());
        }).hasMessage(errMsg);

        params.setNexusPort(String.valueOf(localPort));
        validator.setParameters(params);
        assertThatThrownBy(() -> {
            validator.validateNativePolicies(serviceTemplate.getToscaTopologyTemplate().getPolicies());
        }).hasMessageContaining("Connection refused");
    }

    @Test
    public void testValidateDroolsArtifact() throws Exception {

        params.setNexusPort(String.valueOf(localPort));
        validator.setParameters(params);

        HttpClient mockedClient = mock(HttpClient.class);

        // Test 200 case
        when(mockedClient.get(VALID_PATH)).thenAnswer(
            new Answer<Response>() {
                @Override
                public Response answer(InvocationOnMock invocation) {
                    return Response.status(Response.Status.OK).build();
                }
            });

        ToscaServiceTemplate serviceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString(VALID_NATIVE_DROOLS_POLICY), ToscaServiceTemplate.class);
        ToscaPolicy validDroolsPolicy = serviceTemplate.getToscaTopologyTemplate().getPolicies().iterator().next()
                .entrySet().iterator().next().getValue();
        validator.validateDroolsArtifact(validDroolsPolicy, mockedClient);

        // Test 404 case
        when(mockedClient.get(INVALID_PATH)).thenAnswer(
                new Answer<Response>() {
                    @Override
                    public Response answer(InvocationOnMock invocation) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                });

        serviceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString(INVALID_NATIVE_DROOLS_POLICY), ToscaServiceTemplate.class);
        ToscaPolicy invalidDroolsPolicy = serviceTemplate.getToscaTopologyTemplate().getPolicies().iterator().next()
                .entrySet().iterator().next().getValue();

        String errMsg = "rule artifact org.onap.policy.native.drools|example-invalid-drools-policy|1.0.0-SNAPSHOT "
                + "not found";
        assertThatThrownBy(() -> {
            validator.validateDroolsArtifact(invalidDroolsPolicy, mockedClient);
        }).hasMessage(errMsg);
    }

    /**
     * Thread that accepts a connection on a socket.
     */
    private static class Accepter extends Thread {
        private ServerSocket socket;

        public Accepter(ServerSocket socket) {
            this.socket = socket;
            setDaemon(true);
        }

        @Override
        public void run() {
            try (Socket server = socket.accept()) {
                // do nothing

            } catch (IOException e) {
                LOGGER.error("socket not accepted", e);
            }
        }
    }
}