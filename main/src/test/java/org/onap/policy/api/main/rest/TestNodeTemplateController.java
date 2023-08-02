/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2022-2023 Nordix Foundation. All rights reserved.
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

package org.onap.policy.api.main.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.policy.api.main.PolicyApiApplication;
import org.onap.policy.api.main.rest.utils.CommonTestRestController;
import org.onap.policy.api.main.service.ToscaServiceTemplateService;
import org.onap.policy.common.utils.security.SelfSignedKeyStore;
import org.onap.policy.models.errors.concepts.ErrorResponse;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Class to perform unit test of {@link NodeTemplateController}.
 *
 */
@SpringBootTest(classes = PolicyApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test", "default" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestNodeTemplateController extends CommonTestRestController {

    private static final String TOSCA_NODE_TEMPLATE_RESOURCE =
        "nodetemplates/nodetemplates.metadatasets.input.tosca.json";
    private static final String TOSCA_INVALID_NODE_TYPE =
        "nodetemplates/nodetemplates.metadatasets.invalid.nodetype.json";
    private static final String TOSCA_INVALID_TEMPLATE =
        "nodetemplates/nodetemplates.metadatasets.no.nodetemplate.json";
    private static final String TOSCA_UPDATE_NODE_TEMPLATES = "nodetemplates/nodetemplates.metadatasets.update.json";

    private static final String NODE_TEMPLATES = "nodetemplates";
    private static final String SPECIFIC_NODE_TEMPLATE = "nodetemplates/apexMetadata_adaptive/versions/1.0.0";
    private static final String INVALID_NODE_TEMPLATE_ID = "nodetemplates/invalid_template/versions/1.0.0";

    private static final List<String> nodeTemplateKeys =
        List.of("apexMetadata_grpc", "apexMetadata_adaptive", "apexMetadata_decisionMaker");

    protected static final String APP_JSON = "application/json";

    private static SelfSignedKeyStore keystore;

    @LocalServerPort
    private int apiPort;

    @Autowired
    private ToscaServiceTemplateService toscaServiceTemplateService;

    /**
     * Initializes parameters and set up test environment.
     *
     * @throws IOException on I/O exceptions
     * @throws InterruptedException if interrupted
     */
    @BeforeAll
    public static void setupParameters() throws IOException, InterruptedException {
        keystore = new SelfSignedKeyStore();
    }

    /**
     * Clean up the database.
     *
     */
    @AfterEach
    public void clearDb() {
        for (String name : nodeTemplateKeys) {
            try {
                toscaServiceTemplateService.deleteToscaNodeTemplate(name, "1.0.0");
            } catch (Exception e) {
                //do nothing
            }
        }
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
    void testCreateToscaNodeTemplates() throws Exception {
        Response rawResponse = createResource(NODE_TEMPLATES, TOSCA_NODE_TEMPLATE_RESOURCE, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertNotNull(response);
        assertFalse(response.getToscaTopologyTemplate().getNodeTemplates().isEmpty());

        // Send a node type with a invalid value to trigger an error
        rawResponse = createResource(NODE_TEMPLATES, TOSCA_INVALID_NODE_TYPE, apiPort);
        assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), rawResponse.getStatus());
        ErrorResponse response2 = rawResponse.readEntity(ErrorResponse.class);
        assertThat(response2.getErrorMessage())
            .containsPattern("^NODE_TYPE .* for toscaNodeTemplate .* does not exist$");

        // Send invalid tosca template with no node templates
        rawResponse = createResource(NODE_TEMPLATES, TOSCA_INVALID_TEMPLATE, apiPort);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        response2 = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("node templates not present on the service template", response2.getErrorMessage());
    }


    @Test
    void testReadNodeTemplates() throws Exception {
        Response rawResponse = readResource(NODE_TEMPLATES, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        List<?> nodeTemplates = rawResponse.readEntity(List.class);
        assertNotNull(nodeTemplates);
        assertEquals(0, nodeTemplates.size());

        createResource(NODE_TEMPLATES, TOSCA_NODE_TEMPLATE_RESOURCE, apiPort);
        rawResponse = readResource(NODE_TEMPLATES, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        nodeTemplates = rawResponse.readEntity(List.class);
        assertNotNull(nodeTemplates);
        assertEquals(3, nodeTemplates.size());

        rawResponse = readResource(SPECIFIC_NODE_TEMPLATE, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        List<ToscaNodeTemplate> retrievedTemplate =
            rawResponse.readEntity(new GenericType<List<ToscaNodeTemplate>>() {});
        assertNotNull(nodeTemplates);
        assertEquals(1, retrievedTemplate.size());
        String retrievedTemplateName = retrievedTemplate.get(0).getName();
        assertEquals("apexMetadata_adaptive", retrievedTemplateName);
    }

    @Test
    void testUpdateNodeTemplates() throws Exception {
        createResource(NODE_TEMPLATES, TOSCA_NODE_TEMPLATE_RESOURCE, apiPort);
        Response rawResponse = updateResource(NODE_TEMPLATES, TOSCA_UPDATE_NODE_TEMPLATES, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertNotNull(response);
        assertFalse(response.getToscaTopologyTemplate().getNodeTemplates().isEmpty());
        String updatedValue = "" + response.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_grpc")
            .getMetadata().get("state");
        assertEquals("passive", updatedValue);

        rawResponse = updateResource(NODE_TEMPLATES, TOSCA_INVALID_NODE_TYPE, APP_JSON, apiPort);
        assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), rawResponse.getStatus());
        ErrorResponse response2 = rawResponse.readEntity(ErrorResponse.class);
        assertThat(response2.getErrorMessage())
            .containsPattern("^NODE_TYPE .* for toscaNodeTemplate .* does not exist$");

        // Send invalid tosca template with no node templates
        rawResponse = updateResource(NODE_TEMPLATES, TOSCA_INVALID_TEMPLATE, APP_JSON, apiPort);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        ErrorResponse response3 = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("node templates not present on the service template", response3.getErrorMessage());
    }

    @Test
    void testDeleteNodeTemplates() throws Exception {
        createResource(NODE_TEMPLATES, TOSCA_NODE_TEMPLATE_RESOURCE, apiPort);
        Response rawResponse = deleteResource(SPECIFIC_NODE_TEMPLATE, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertNotNull(response);
        assertFalse(response.getToscaTopologyTemplate().getNodeTemplates().isEmpty());

        rawResponse = readResource(NODE_TEMPLATES, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        List<?> nodeTemplates = rawResponse.readEntity(List.class);
        assertNotNull(nodeTemplates);
        assertEquals(2, nodeTemplates.size());

        // Send invalid id
        rawResponse = deleteResource(INVALID_NODE_TEMPLATE_ID, APP_JSON, apiPort);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        ErrorResponse response3 = rawResponse.readEntity(ErrorResponse.class);
        assertThat(response3.getErrorMessage()).containsPattern("^node template .* not found$");
    }

}
