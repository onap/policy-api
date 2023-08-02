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

package org.onap.policy.api.main.rest.stub;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.onap.policy.api.main.rest.CommonRestController;
import org.onap.policy.api.main.rest.PolicyFetchMode;
import org.onap.policy.api.main.rest.genapi.PolicyDesignApi;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Profile("stub")
public class ApiRestControllerStub extends CommonRestController implements PolicyDesignApi {

    private final StubUtils stubUtils;

    @Override
    public ResponseEntity<ToscaServiceTemplate> createPolicies(
            @Valid ToscaServiceTemplate body, UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> createPolicy(
        String policyTypeId,
        String policyTypeVersion,
        @Valid ToscaServiceTemplate body,
        UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> createPolicyType(
            @Valid ToscaServiceTemplate body, UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> deleteSpecificPolicy(
            String policyId,
            String policyVersion,
            UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> deleteSpecificVersionOfPolicy(
        String policyTypeId,
        String policyTypeVersion,
        String policyId,
        String policyVersion,
        UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> deleteSpecificVersionOfPolicyType(
        String policyTypeId,
        String versionId,
        UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> getAllPolicies(
        String policyTypeId,
        String policyTypeVersion,
        @Valid PolicyFetchMode mode,
        UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> getAllPolicyTypes(
        UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> getAllVersionsOfPolicy(
        String policyId,
        String policyTypeId,
        String policyTypeVersion,
        @Valid PolicyFetchMode mode,
        UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> getAllVersionsOfPolicyType(
        String policyTypeId, UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<HealthCheckReport> getHealthCheck(
        UUID requestID) {
        return stubUtils.getStubbedResponse(HealthCheckReport.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> getLatestVersionOfPolicy(
        String policyId,
        String policyTypeId,
        String policyTypeVersion,
        @Valid PolicyFetchMode mode,
        UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> getLatestVersionOfPolicyType(
        String policyTypeId, UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> getPolicies(
        @Valid PolicyFetchMode mode, UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> getSpecificPolicy(
        String policyId,
        String policyVersion,
        @Valid PolicyFetchMode mode,
        UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> getSpecificVersionOfPolicy(
        String policyId,
        String policyTypeId,
        String policyTypeVersion,
        String policyVersion,
        @Valid PolicyFetchMode mode,
        UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

    @Override
    public ResponseEntity<ToscaServiceTemplate> getSpecificVersionOfPolicyType(
        String policyTypeId,
        String versionId,
        UUID requestID) {
        return stubUtils.getStubbedResponse(ToscaServiceTemplate.class);
    }

}
