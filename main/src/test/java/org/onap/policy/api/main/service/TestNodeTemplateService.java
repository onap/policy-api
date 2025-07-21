/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2022-2025 OpenInfra Foundation Europe. All rights reserved.
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


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.policy.api.main.repository.NodeTemplateRepository;
import org.onap.policy.api.main.repository.NodeTypeRepository;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaNodeTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaNodeType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;

class TestNodeTemplateService {

    @Mock
    private NodeTemplateRepository nodeTemplateRepository;

    @Mock
    private NodeTypeRepository nodeTypeRepository;

    @InjectMocks
    private NodeTemplateService nodeTemplateService;

    private static final String POLICY_WITH_METADATA_SET_REF = "nodetemplates/dummy.apex.decisionmaker.policy.yaml";
    private static final String UPDATED_NODE_TEMPLATE_JSON = "nodetemplates/nodetemplates.metadatasets.update.json";

    private static ToscaServiceTemplate updatedToscaServiceTemplate;
    private final YamlJsonTranslator yamlJsonTranslator = new YamlJsonTranslator();
    ToscaServiceTemplate policyServiceTemplate;

    AutoCloseable closeable;

    /**
     * Set up for tests.
     *
     * @throws CoderException if error in json parsing
     */
    @BeforeEach
    void setUp() throws CoderException {
        closeable = MockitoAnnotations.openMocks(this);
        StandardCoder standardCoder = new StandardCoder();
        policyServiceTemplate =
            yamlJsonTranslator.fromYaml(ResourceUtils.getResourceAsString(POLICY_WITH_METADATA_SET_REF),
                ToscaServiceTemplate.class);
        updatedToscaServiceTemplate =
            standardCoder.decode(ResourceUtils.getResourceAsString(UPDATED_NODE_TEMPLATE_JSON),
                ToscaServiceTemplate.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testVerifyNodeType() {
        assertThatThrownBy(() -> nodeTemplateService.verifyNodeTypeInDbTemplate(new JpaToscaNodeTemplate()))
            .hasMessageMatching("^NODE_TYPE .* for toscaNodeTemplate .* does not exist$");

        JpaToscaNodeTemplate jpaToscaNodeTemplate = new JpaToscaNodeTemplate();
        PfConceptKey nodeType = new PfConceptKey("dummyType", "1.0.0");
        jpaToscaNodeTemplate.setType(nodeType);
        jpaToscaNodeTemplate.setKey(new PfConceptKey("dummyName", "1.0.0"));
        Mockito.when(nodeTypeRepository.findById(nodeType)).thenReturn(Optional.of(new JpaToscaNodeType()));
        assertDoesNotThrow(() -> nodeTemplateService.verifyNodeTypeInDbTemplate(jpaToscaNodeTemplate));
    }

    @Test
    void testNodeTemplateUsedInPolicy() {
        assertDoesNotThrow(() -> nodeTemplateService.assertNodeTemplateNotUsedInPolicy("dummyName", "1.0.0",
            new JpaToscaServiceTemplate(policyServiceTemplate)));

        assertThatThrownBy(() -> nodeTemplateService
            .assertNodeTemplateNotUsedInPolicy("apexMetadata_decisionMaker", "1.0.0",
                new JpaToscaServiceTemplate(policyServiceTemplate)))
            .hasMessage("Node template is in use, it is referenced in Tosca Policy operational.apex.decisionMaker "
                + "version 1.0.0");
    }

    @Test
    void testNodeTemplateUpdate() {
        Mockito.when(nodeTypeRepository.findById(Mockito.any())).thenReturn(Optional.of(new JpaToscaNodeType()));
        Mockito.when(nodeTemplateRepository.findById(Mockito.any())).thenReturn(Optional.of(
            new JpaToscaNodeTemplate()));
        assertDoesNotThrow(() -> nodeTemplateService.updateToscaNodeTemplates(
            new JpaToscaServiceTemplate(updatedToscaServiceTemplate)));
    }

    @Test
    void testNodeTemplateUpdate_Exception() {
        Mockito.when(nodeTypeRepository.findById(Mockito.any())).thenReturn(Optional.of(new JpaToscaNodeType()));
        Mockito.when(nodeTemplateRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        var updatedObj = new JpaToscaServiceTemplate(updatedToscaServiceTemplate);
        assertThrows(PfModelRuntimeException.class,
            () -> nodeTemplateService.updateToscaNodeTemplates(updatedObj));

        assertThrows(NullPointerException.class, () -> nodeTemplateService.updateToscaNodeTemplates(null));
    }
}
