/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2023 Bell Canada. All rights reserved.
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

package org.onap.policy.api.contract;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import javax.ws.rs.core.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.policy.api.main.PolicyApiApplication;
import org.onap.policy.api.main.rest.utils.CommonTestRestController;
import org.onap.policy.common.utils.security.SelfSignedKeyStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PolicyApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test", "stub" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ApiContractTest extends CommonTestRestController {
    protected static final String APP_JSON = "application/json";
    protected static final String APP_YAML = "application/yaml";
    private static final String TOSCA_NODE_TEMPLATE_RESOURCE =
            "nodetemplates/nodetemplates.metadatasets.input.tosca.json";

    @LocalServerPort
    private int apiPort;

    private static SelfSignedKeyStore keystore;


    @BeforeClass
    public static void setupParameters() throws IOException, InterruptedException {
        keystore = new SelfSignedKeyStore();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("server.ssl.enabled", () -> "true");
        registry.add("server.ssl.key-store", () -> keystore.getKeystoreName());
        registry.add("server.ssl.key-store-password", () -> SelfSignedKeyStore.KEYSTORE_PASSWORD);
        registry.add("server.ssl.key-store-type", () -> "PKCS12");
        registry.add("server.ssl.key-alias", () -> "policy@policy.onap.org");
        registry.add("server.ssl.key-password", () -> SelfSignedKeyStore.PRIVATE_KEY_PASSWORD);
    }

    @Test
    public void testStubPolicyDesign() throws Exception {
        checkStubJsonGet("policies");
        checkStubJsonGet("policies/policyname/versions/1.0.2");
        checkStubJsonGet("policytypes");
        checkStubJsonGet("policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16");
        checkStubJsonGet("policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/latest");
        checkStubJsonGet("policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0/");
        checkStubJsonGet("policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0/policies");
        checkStubJsonGet("policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0/policies/"
            + "9c65fa1f-2833-4076-a64d-5b62e35cd09b");
        checkStubJsonGet("policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0/policies/"
                + "9c65fa1f-2833-4076-a64d-5b62e35cd09b/versions/latest");
        checkStubJsonGet("policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0/policies/"
                + "9c65fa1f-2833-4076-a64d-5b62e35cd09b/versions/1.2.3");
        checkStubJsonGet("healthcheck");

        checkStubJsonPost("policies");
        checkStubJsonPost("policytypes");
        checkStubJsonPost("policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.2.3/policies");

        checkStubJsonDelete("policies/policyname/versions/1.0.2");
        checkStubJsonDelete("policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0");
        checkStubJsonDelete("policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0/policies/"
                + "9c65fa1f-2833-4076-a64d-5b62e35cd09b/versions/1.2.3");
    }

    @Test
    public void testStubNodeTemplateDesign() throws Exception {
        checkStubJsonGet("nodetemplates");
        checkStubJsonGet("nodetemplates/k8stemplate/versions/1.0.0");

        checkStubJsonPost("nodetemplates");

        checkStubJsonPut("nodetemplates");

        checkStubJsonDelete("nodetemplates/k8stemplate/versions/1.0.0");
    }

    @Test
    public void testErrors() throws Exception {
        var responseYaml = super.readResource("policies", APP_YAML, apiPort);
        assertEquals(Response.Status.NOT_IMPLEMENTED.getStatusCode(), responseYaml.getStatus());

        var responseListYaml = super.readResource("nodetemplates", APP_YAML, apiPort);
        assertEquals(Response.Status.NOT_IMPLEMENTED.getStatusCode(), responseListYaml.getStatus());

    }

    private void checkStubJsonGet(String url) throws Exception {
        var response = super.readResource(url, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private void checkStubJsonPost(String url) throws Exception {
        var response = super.createResource(url, TOSCA_NODE_TEMPLATE_RESOURCE, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private void checkStubJsonPut(String url) throws Exception {
        var response = super.updateResource(url, TOSCA_NODE_TEMPLATE_RESOURCE, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private void checkStubJsonDelete(String url) throws Exception {
        var response = super.deleteResource(url, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

}
