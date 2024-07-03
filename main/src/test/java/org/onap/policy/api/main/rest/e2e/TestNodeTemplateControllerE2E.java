/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2024 Nordix Foundation. All rights reserved.
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

package org.onap.policy.api.main.rest.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.errors.concepts.ErrorResponse;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles({"default", "test"})
@AutoConfigureWebTestClient
class TestNodeTemplateControllerE2E {

    @Autowired
    WebTestClient webClient;

    protected static final MediaType APPLICATION_YAML = new MediaType("application", "yaml");
    protected static final MediaType APPLICATION_JSON = new MediaType("application", "json");

    private static final String TOSCA_NODE_TEMPLATE_RESOURCE =
        "nodetemplates/nodetemplates.metadatasets.input.tosca.json";
    private static final String TOSCA_INVALID_NODE_TYPE =
        "nodetemplates/nodetemplates.metadatasets.invalid.nodetype.json";
    private static final String TOSCA_INVALID_TEMPLATE =
        "nodetemplates/nodetemplates.metadatasets.no.nodetemplate.json";
    private static final String TOSCA_UPDATE_NODE_TEMPLATES = "nodetemplates/nodetemplates.metadatasets.update.json";

    private static final String NODE_TEMPLATES = "/nodetemplates";
    private static final String SPECIFIC_NODE_TEMPLATE = "/nodetemplates/apexMetadata_adaptive/versions/1.0.0";
    private static final String INVALID_NODE_TEMPLATE_ID = "/nodetemplates/invalid_template/versions/1.0.0";

    private final StandardCoder standardCoder = new StandardCoder();

    @BeforeEach
    void beforeEach() {
        var filter = ExchangeFilterFunctions.basicAuthentication("policyadmin", "zb!XztG34");
        webClient = webClient.mutate().filter(filter).build();
    }

    @Test
    void testCreateToscaNodeTemplates() throws CoderException {
        createNodeTemplate();

        // Send a node type with an invalid value to trigger an error
        var body = ResourceUtils.getResourceAsString(TOSCA_INVALID_NODE_TYPE);

        var responseBody = webClient.post().uri(NODE_TEMPLATES).contentType(APPLICATION_JSON)
            .bodyValue(body).exchange().expectStatus().is4xxClientError()
            .expectBody(String.class).returnResult().getResponseBody();

        var errorResp = standardCoder.decode(responseBody, ErrorResponse.class);
        assertNotNull(errorResp);
        assertEquals(Response.Status.NOT_ACCEPTABLE, errorResp.getResponseCode());
        assertThat(errorResp.getErrorMessage())
            .containsPattern("^NODE_TYPE .* for toscaNodeTemplate .* does not exist$");

        // Send invalid tosca template with no node templates
        body = ResourceUtils.getResourceAsString(TOSCA_INVALID_TEMPLATE);

        webClient.post().uri(NODE_TEMPLATES).contentType(APPLICATION_JSON)
            .bodyValue(body).exchange().expectStatus().isNotFound();
    }


    @Test
    void testReadNodeTemplates() {
        List<ToscaNodeTemplate> nodeTemplateList = getNodeTemplates(NODE_TEMPLATES);
        assertNotNull(nodeTemplateList);
        assertTrue(nodeTemplateList.isEmpty());

        createNodeTemplate();

        nodeTemplateList = getNodeTemplates(NODE_TEMPLATES);
        assertNotNull(nodeTemplateList);
        assertEquals(3, nodeTemplateList.size());

        nodeTemplateList = getNodeTemplates(SPECIFIC_NODE_TEMPLATE);
        assertNotNull(nodeTemplateList);
        assertEquals(1, nodeTemplateList.size());
        assertEquals("apexMetadata_adaptive", nodeTemplateList.get(0).getName());
    }

    @Test
    void testUpdateNodeTemplates() throws Exception {
        createNodeTemplate();

        var updateBody = ResourceUtils.getResourceAsString(TOSCA_UPDATE_NODE_TEMPLATES);
        var updateResp = webClient.put().uri(NODE_TEMPLATES).contentType(APPLICATION_JSON)
            .bodyValue(updateBody).exchange().expectStatus().isOk().expectBody(String.class);

        ToscaServiceTemplate response = standardCoder
            .decode(updateResp.returnResult().getResponseBody(), ToscaServiceTemplate.class);
        assertNotNull(response);
        assertFalse(response.getToscaTopologyTemplate().getNodeTemplates().isEmpty());
        String updatedValue = "" + response.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_grpc")
            .getMetadata().get("state");
        assertEquals("passive", updatedValue);

        var invalidUpdateBody = ResourceUtils.getResourceAsString(TOSCA_INVALID_NODE_TYPE);
        var responseBody = webClient.put().uri(NODE_TEMPLATES).contentType(APPLICATION_JSON)
            .bodyValue(invalidUpdateBody).exchange().expectStatus().is4xxClientError()
            .expectBody(String.class).returnResult().getResponseBody();
        var error = standardCoder.decode(responseBody, ErrorResponse.class);
        assertNotNull(error);
        assertThat(error.getErrorMessage())
            .containsPattern("^NODE_TYPE .* for toscaNodeTemplate .* does not exist$");

        // Send invalid tosca template with no node templates
        invalidUpdateBody = ResourceUtils.getResourceAsString(TOSCA_INVALID_TEMPLATE);
        responseBody = webClient.put().uri(NODE_TEMPLATES).contentType(APPLICATION_JSON)
            .bodyValue(invalidUpdateBody).exchange().expectStatus().isNotFound()
            .expectBody(String.class).returnResult().getResponseBody();
        error = standardCoder.decode(responseBody, ErrorResponse.class);
        assertNotNull(error);
        assertThat(error.getErrorMessage()).contains("node templates not present on the service template");
    }

    @Test
    void testDeleteNodeTemplates() {
        createNodeTemplate();

        webClient.delete().uri(SPECIFIC_NODE_TEMPLATE).exchange().expectStatus().isOk();

        var nodeTemplateList = getNodeTemplates(NODE_TEMPLATES);
        assertEquals(2, nodeTemplateList.size());

        // Send invalid id
        webClient.delete().uri(INVALID_NODE_TEMPLATE_ID).exchange().expectStatus().isNotFound();
    }

    private void createNodeTemplate() {
        var body = ResourceUtils.getResourceAsString(TOSCA_NODE_TEMPLATE_RESOURCE);
        webClient.post().uri(NODE_TEMPLATES).contentType(APPLICATION_JSON)
            .bodyValue(body).exchange().expectStatus().isCreated();
    }

    private List<ToscaNodeTemplate> getNodeTemplates(String uri) {
        List<ToscaNodeTemplate> toscaNodeTemplateList = new ArrayList<>();

        var response = webClient.get().uri(uri).accept(APPLICATION_JSON, APPLICATION_YAML)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class);
        assertNotNull(response.returnResult());
        var contentType = response.returnResult().getResponseHeaders().getContentType();
        assert contentType != null;
        var rawBody = response.returnResult().getResponseBody();
        try {
            for (Object node : standardCoder.convert(rawBody, List.class)) {
                toscaNodeTemplateList.add(standardCoder.convert(node, ToscaNodeTemplate.class));
            }
        } catch (CoderException e) {
            fail(e.getMessage());
        }
        return toscaNodeTemplateList;
    }
}
