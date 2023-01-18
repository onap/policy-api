/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.api.main.rest.stub;

import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.onap.policy.api.main.rest.CommonRestController;
import org.onap.policy.api.main.rest.genapi.ToscaNodeTemplateDesignApi;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Profile("stub")
public class NodeTemplateControllerStub extends CommonRestController
    implements ToscaNodeTemplateDesignApi {

    private final StubUtils stubUtils;

    @Override
    public ResponseEntity<ToscaServiceTemplate> createToscaNodeTemplates(
        @Valid ToscaServiceTemplate body, UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> deleteToscaNodeTemplates(
        String name, String version, UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<List<ToscaNodeTemplate>> getAllNodeTemplates(
        UUID requestID) {
        return stubUtils.getStubbedResponseList(ToscaNodeTemplate.class);
    }

    @Override
    public ResponseEntity<List<ToscaNodeTemplate>> getSpecificVersionOfNodeTemplate(
        String name, String version, UUID requestID) {
        return stubUtils.getStubbedResponseList(ToscaNodeTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> updateToscaNodeTemplates(
        @Valid ToscaServiceTemplate body, UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }
}
