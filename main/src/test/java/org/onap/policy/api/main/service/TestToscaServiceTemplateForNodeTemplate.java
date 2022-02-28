/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

package org.onap.policy.api.main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaNodeTemplate;

@RunWith(MockitoJUnitRunner.class)
public class TestToscaServiceTemplateForNodeTemplate extends TestCommonToscaServiceTemplateService {

    private static final String NODE_TEMPLATES_JSON = "nodetemplates/nodetemplates.metadatasets.input.tosca.json";
    private static final String UPDATED_NODE_TEMPLATE_JSON = "nodetemplates/nodetemplates.metadatasets.update.json";
    private static ToscaServiceTemplate toscaServiceTemplate;
    private static ToscaServiceTemplate updatedToscaServiceTemplate;
    private StandardCoder standardCoder;

    @InjectMocks
    private ToscaServiceTemplateService toscaServiceTemplateService;


    @Before
    public void setUp() {
        super.setUp();
    }

    /**
     * Fetch json files required for the tests.
     *
     * @throws CoderException when error parsing the json
     */
    @Before
    public void fetchToscaNodeTemplateJson() throws CoderException {
        standardCoder = new StandardCoder();
        toscaServiceTemplate =
            standardCoder.decode(ResourceUtils.getResourceAsString(NODE_TEMPLATES_JSON), ToscaServiceTemplate.class);
        updatedToscaServiceTemplate =
            standardCoder.decode(ResourceUtils.getResourceAsString(UPDATED_NODE_TEMPLATE_JSON),
                ToscaServiceTemplate.class);
    }

    @Test
    public void testToscaNodeTemplatesGet() throws Exception {

        assertNotNull(toscaServiceTemplate);
        var createdTemplate = toscaServiceTemplateService.createToscaNodeTemplates(toscaServiceTemplate);
        mockDbServiceTemplate(createdTemplate, null, null);

        //Fetch all node templates if id is null
        List<ToscaNodeTemplate> gotToscaNodeTemplates = toscaServiceTemplateService
            .fetchToscaNodeTemplates(null, null);
        assertEquals(3, gotToscaNodeTemplates.size());

        // Get filtered node templates
        List<ToscaNodeTemplate> filteredNodeTemplates = toscaServiceTemplateService
            .fetchToscaNodeTemplates("apexMetadata_adaptive", "1.0.0");
        assertEquals(1, filteredNodeTemplates.size());

        //Get invalid node template
        List<ToscaNodeTemplate> filteredNodeTemplatesInvalid = toscaServiceTemplateService
            .fetchToscaNodeTemplates("invalidname", "1.0.0");
        assertThat(filteredNodeTemplatesInvalid).isEmpty();
    }

    @Test
    public void testToscaNodeTemplatesCreate() throws Exception {

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.createToscaNodeTemplates(null);
        }).hasMessageMatching("^serviceTemplate is marked .*on.*ull but is null$");

        ToscaServiceTemplate createdNodeTemplates =
            toscaServiceTemplateService.createToscaNodeTemplates(toscaServiceTemplate);
        assertThat(createdNodeTemplates.getToscaTopologyTemplate().getNodeTemplates()).hasSize(3);
        assertThat(createdNodeTemplates.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_grpc")
            .getMetadata()).containsKey("threshold");
    }

    @Test
    public void testToscaNodeTemplateUpdate() throws Exception {

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.updateToscaNodeTemplates(null);
        }).hasMessageMatching("^serviceTemplate is marked non-null but is null$");

        JpaToscaNodeTemplate jpaNodeTemplate = new JpaToscaNodeTemplate();
        PfConceptKey key = new PfConceptKey("apexMetadata_grpc", "1.0.0");
        jpaNodeTemplate.setKey(key);
        jpaNodeTemplate.setDescription("Updated Metadata set for GRPC");
        ToscaServiceTemplate updatedTemplate =
            toscaServiceTemplateService.updateToscaNodeTemplates(updatedToscaServiceTemplate);
        assertEquals("Updated Metadata set for GRPC",
            updatedTemplate.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_grpc")
                .getDescription());
    }

    @Test
    public void testToscaNodeTemplateDelete() throws Exception {

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.deleteToscaNodeTemplate(null, null);
        }).hasMessageMatching("^name is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            toscaServiceTemplateService.deleteToscaNodeTemplate("name", null);
        }).hasMessageMatching("^version is marked .*on.*ull but is null$");

        var createdTemplate = toscaServiceTemplateService.createToscaNodeTemplates(toscaServiceTemplate);
        mockDbServiceTemplate(createdTemplate, null, null);
        assertThatThrownBy(() -> {
            toscaServiceTemplateService.deleteToscaNodeTemplate("dummyname", "1.0.1");
        }).hasMessage("node template dummyname:1.0.1 not found");

        ToscaServiceTemplate responseTemplate =
            toscaServiceTemplateService.deleteToscaNodeTemplate("apexMetadata_decisionMaker", "1.0.0");

        assertTrue(responseTemplate.getToscaTopologyTemplate().getNodeTemplates()
            .containsKey("apexMetadata_decisionMaker"));
        assertThat(responseTemplate.getToscaTopologyTemplate().getNodeTemplates()).hasSize(1);

        assertThat(toscaServiceTemplateService.fetchToscaNodeTemplates(null, null)).hasSize(2);
    }

}
