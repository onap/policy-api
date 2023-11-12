/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2023 Nordix Foundation.
 * Modifications Copyright (C) 2020-2023 Bell Canada. All rights reserved.
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

import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.onap.policy.api.main.exception.PolicyApiRuntimeException;
import org.onap.policy.api.main.rest.genapi.PolicyDesignApi;
import org.onap.policy.api.main.rest.provider.healthcheck.HealthCheckProvider;
import org.onap.policy.api.main.service.ToscaServiceTemplateService;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class to provide REST API services.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@RestController
@RequiredArgsConstructor
@Profile("default")
public class ApiRestController extends CommonRestController implements PolicyDesignApi {

    private final ToscaServiceTemplateService toscaServiceTemplateService;
    private final HealthCheckProvider healthCheckProvider;

    /**
     * Retrieves the healthcheck status of the API component.
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<HealthCheckReport> getHealthCheck(UUID requestId) {
        final var report = healthCheckProvider.performHealthCheck();
        return makeResponse(requestId, report, report.getCode());
    }

    /**
     * Retrieves all available policy types.
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getAllPolicyTypes(UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.fetchPolicyTypes(null, null);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = "GET /policytypes";
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves all versions of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getAllVersionsOfPolicyType(
        String policyTypeId,
        UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.fetchPolicyTypes(policyTypeId, null);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s", policyTypeId);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves specified version of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     * @param versionId    the version of specified policy type
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getSpecificVersionOfPolicyType(
        String policyTypeId,
        String versionId,
        UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.fetchPolicyTypes(policyTypeId, versionId);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s", policyTypeId, versionId);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves latest version of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getLatestVersionOfPolicyType(
        String policyTypeId,
        UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.fetchLatestPolicyTypes(policyTypeId);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/latest", policyTypeId);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Creates a new policy type.
     *
     * @param body the body of policy type following TOSCA definition
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> createPolicyType(
        ToscaServiceTemplate body,
        UUID requestId) {
        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, "/policytypes", toJson(body));
        }
        try {
            mutex.acquire();
            ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.createPolicyType(body);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            final var msg = "POST /policytypes";
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PolicyApiRuntimeException(e.getMessage(), null, null, requestId);
        } finally {
            mutex.release();
        }
    }

    /**
     * Deletes specified version of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     * @param versionId    the version of specified policy type
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> deleteSpecificVersionOfPolicyType(
        String policyTypeId,
        String versionId,
        UUID requestId) {
        try {
            mutex.acquire();
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.deletePolicyType(policyTypeId, versionId);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            var msg = String.format("DELETE /policytypes/%s/versions/%s", policyTypeId, versionId);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PolicyApiRuntimeException(e.getMessage(), null, null, requestId);
        } finally {
            mutex.release();
        }
    }

    /**
     * Retrieves all versions of a particular policy.
     *
     * @param policyTypeId      the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getAllPolicies(
        String policyTypeId,
        String policyTypeVersion,
        PolicyFetchMode mode,
        UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.fetchPolicies(policyTypeId, policyTypeVersion, null, null, mode);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s/policies", policyTypeId, policyTypeVersion);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves all versions of a particular policy.
     *
     * @param policyTypeId      the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId          the ID of specified policy
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getAllVersionsOfPolicy(
        String policyId,
        String policyTypeId,
        String policyTypeVersion,
        PolicyFetchMode mode,
        UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.fetchPolicies(policyTypeId, policyTypeVersion, policyId, null, mode);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("/policytypes/%s/versions/%s/policies/%s",
                policyTypeId, policyTypeVersion, policyId);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves the specified version of a particular policy.
     *
     * @param policyTypeId      the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId          the ID of specified policy
     * @param policyVersion     the version of specified policy
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getSpecificVersionOfPolicy(
        String policyId,
        String policyTypeId,
        String policyTypeVersion,
        String policyVersion,
        PolicyFetchMode mode,
        UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService
                .fetchPolicies(policyTypeId, policyTypeVersion, policyId, policyVersion, mode);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s/policies/%s/versions/%s",
                policyTypeId, policyTypeVersion, policyId, policyVersion);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves the latest version of a particular policy.
     *
     * @param policyTypeId      the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId          the ID of specified policy
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getLatestVersionOfPolicy(
        String policyId,
        String policyTypeId,
        String policyTypeVersion,
        PolicyFetchMode mode,
        UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.fetchLatestPolicies(policyTypeId, policyTypeVersion, policyId, mode);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s/policies/%s/versions/latest",
                policyTypeId, policyTypeVersion, policyId);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Creates a new policy for a particular policy type and version.
     *
     * @param policyTypeId      the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param body              the body of policy following TOSCA definition
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> createPolicy(
        String policyTypeId,
        String policyTypeVersion,
        ToscaServiceTemplate body,
        UUID requestId) {
        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST,
                "/policytypes/" + policyTypeId + "/versions/" + policyTypeVersion + "/policies", toJson(body));
        }
        try {
            mutex.acquire();
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.createPolicy(policyTypeId, policyTypeVersion, body);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            var msg = String.format("POST /policytypes/%s/versions/%s/policies", policyTypeId, policyTypeVersion);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PolicyApiRuntimeException(e.getMessage(), null, null, requestId);
        } finally {
            mutex.release();
        }
    }

    /**
     * Deletes the specified version of a particular policy.
     *
     * @param policyTypeId      the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId          the ID of specified policy
     * @param policyVersion     the version of specified policy
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> deleteSpecificVersionOfPolicy(
        String policyTypeId,
        String policyTypeVersion,
        String policyId,
        String policyVersion,
        UUID requestId) {
        try {
            mutex.acquire();
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.deletePolicy(policyTypeId, policyTypeVersion, policyId, policyVersion);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            var msg = String.format("DELETE /policytypes/%s/versions/%s/policies/%s/versions/%s",
                policyTypeId, policyTypeVersion, policyId, policyVersion);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PolicyApiRuntimeException(e.getMessage(), null, null, requestId);
        } finally {
            mutex.release();
        }
    }

    /**
     * Retrieves all the available policies.
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getPolicies(
        PolicyFetchMode mode,
        UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.fetchPolicies(null, null, null, null, mode);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = "GET /policies/ --";
            if (pfme.getErrorResponse().getResponseCode().equals(Status.NOT_FOUND)) {
                pfme.getErrorResponse().setErrorMessage(ERROR_MESSAGE_NO_POLICIES_FOUND);
                pfme.getErrorResponse().setErrorDetails(List.of(ERROR_MESSAGE_NO_POLICIES_FOUND));
            }
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves the specified version of a particular policy.
     *
     * @param policyId      the Name of specified policy
     * @param policyVersion the version of specified policy
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getSpecificPolicy(
        String policyId,
        String policyVersion,
        PolicyFetchMode mode,
        UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.fetchPolicies(null, null, policyId, policyVersion, mode);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policies/%s/versions/%s", policyId, policyVersion);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Creates one or more new policies in one call.
     *
     * @param body the body of policy following TOSCA definition
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> createPolicies(
        ToscaServiceTemplate body,
        UUID requestId) {
        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, "/policies", toJson(body));
        }
        try {
            mutex.acquire();
            ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.createPolicies(body);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            final var msg = "POST /policies";
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PolicyApiRuntimeException(e.getMessage(), null, null, requestId);
        } finally {
            mutex.release();
        }
    }

    /**
     * Deletes the specified version of a particular policy.
     *
     * @param policyId      the ID of specified policy
     * @param policyVersion the version of specified policy
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> deleteSpecificPolicy(
        String policyId,
        String policyVersion,
        UUID requestId) {
        try {
            mutex.acquire();
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.deletePolicy(null, null, policyId, policyVersion);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            var msg = String.format("DELETE /policies/%s/versions/%s", policyId, policyVersion);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PolicyApiRuntimeException(e.getMessage(), null, null, requestId);
        } finally {
            mutex.release();
        }
    }
}
