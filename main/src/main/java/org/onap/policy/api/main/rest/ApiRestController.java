/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2022 Nordix Foundation.
 * Modifications Copyright (C) 2020-2022 Bell Canada. All rights reserved.
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

import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.onap.policy.api.main.exception.PolicyApiRuntimeException;
import org.onap.policy.api.main.rest.genapi.PolicyDesignApi;
import org.onap.policy.api.main.rest.provider.healthcheck.HealthCheckProvider;
import org.onap.policy.api.main.rest.provider.statistics.ApiStatisticsManager;
import org.onap.policy.api.main.rest.provider.statistics.StatisticsProvider;
import org.onap.policy.api.main.rest.provider.statistics.StatisticsReport;
import org.onap.policy.api.main.service.ToscaServiceTemplateService;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class to provide REST API services.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@RestController
@RequiredArgsConstructor
public class ApiRestController extends CommonRestController implements PolicyDesignApi {

    private enum Target {
        POLICY,
        POLICY_TYPE,
        OTHER
    }

    private final ToscaServiceTemplateService toscaServiceTemplateService;
    private final HealthCheckProvider healthCheckProvider;
    private final ApiStatisticsManager mgr;
    private final StatisticsProvider statisticsProvider;

    /**
     * Retrieves the healthcheck status of the API component.
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<HealthCheckReport> getHealthCheck(UUID requestId) {
        final var report = healthCheckProvider.performHealthCheck();
        updateApiStatisticsCounter(Target.OTHER, HttpStatus.resolve(report.getCode()), HttpMethod.GET);
        return makeResponse(requestId, report, report.getCode());
    }

    /**
     * Retrieves the statistics report of the API component.
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<StatisticsReport> getStatistics(UUID requestId) {
        updateApiStatisticsCounter(Target.OTHER, HttpStatus.OK, HttpMethod.GET);
        return makeOkResponse(requestId, statisticsProvider.fetchCurrentStatistics());
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
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = "GET /policytypes";
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves all versions of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getAllVersionsOfPolicyType(
            String policyTypeId,
            UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.fetchPolicyTypes(policyTypeId, null);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s", policyTypeId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves specified version of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     * @param versionId the version of specified policy type
     *
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
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s", policyTypeId, versionId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves latest version of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> getLatestVersionOfPolicyType(
            String policyTypeId,
            UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.fetchLatestPolicyTypes(policyTypeId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/latest", policyTypeId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Creates a new policy type.
     *
     * @param body the body of policy type following TOSCA definition
     *
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
            ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.createPolicyType(body);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.POST);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            final var msg = "POST /policytypes";
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.POST);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Deletes specified version of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     * @param versionId the version of specified policy type
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> deleteSpecificVersionOfPolicyType(
            String policyTypeId,
            String versionId,
            UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.deletePolicyType(policyTypeId, versionId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.DELETE);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            var msg = String.format("DELETE /policytypes/%s/versions/%s", policyTypeId, versionId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.DELETE);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves all versions of a particular policy.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     *
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
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s/policies", policyTypeId, policyTypeVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves all versions of a particular policy.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId the ID of specified policy
     *
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
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("/policytypes/%s/versions/%s/policies/%s",
                policyTypeId, policyTypeVersion, policyId);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves the specified version of a particular policy.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId the ID of specified policy
     * @param policyVersion the version of specified policy
     *
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
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s/policies/%s/versions/%s",
                policyTypeId, policyTypeVersion, policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves the latest version of a particular policy.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId the ID of specified policy
     *
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
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s/policies/%s/versions/latest",
                policyTypeId, policyTypeVersion, policyId);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Creates a new policy for a particular policy type and version.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param body the body of policy following TOSCA definition
     *
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
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.createPolicy(policyTypeId, policyTypeVersion, body);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.POST);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            var msg = String.format("POST /policytypes/%s/versions/%s/policies", policyTypeId, policyTypeVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.POST);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Deletes the specified version of a particular policy.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId the ID of specified policy
     * @param policyVersion the version of specified policy
     *
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
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.deletePolicy(policyTypeId, policyTypeVersion, policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.DELETE);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            var msg = String.format("DELETE /policytypes/%s/versions/%s/policies/%s/versions/%s",
                policyTypeId, policyTypeVersion, policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.DELETE);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
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
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = "GET /policies/ --";
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
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
     * @param policyId the Name of specified policy
     * @param policyVersion the version of specified policy
     *
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
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policies/%s/versions/%s", policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Creates one or more new policies in one call.
     *
     * @param body the body of policy following TOSCA definition
     *
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
            ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService.createPolicies(body);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.POST);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            final var msg = "POST /policies";
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.POST);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Deletes the specified version of a particular policy.
     *
     * @param policyId the ID of specified policy
     * @param policyVersion the version of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> deleteSpecificPolicy(
        String policyId,
        String policyVersion,
        UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate =
                toscaServiceTemplateService.deletePolicy(null, null, policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.DELETE);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelRuntimeException pfme) {
            var msg = String.format("DELETE /policies/%s/versions/%s", policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.DELETE);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    private void updateApiStatisticsCounter(Target target, HttpStatus result, HttpMethod http) {
        mgr.updateTotalApiCallCount();
        switch (target) {
            case POLICY:
                updatePolicyStats(result, http);
                break;
            case POLICY_TYPE:
                updatePolicyTypeStats(result, http);
                break;
            default:
                mgr.updateApiCallSuccessCount();
                break;
        }
    }

    private void updatePolicyStats(HttpStatus result, HttpMethod http) {
        if (result.equals(HttpStatus.OK)) {
            switch (http) {
                case GET:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyGetCount();
                    mgr.updatePolicyGetSuccessCount();
                    break;
                case POST:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyPostCount();
                    mgr.updatePolicyPostSuccessCount();
                    break;
                case DELETE:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyDeleteCount();
                    mgr.updatePolicyDeleteSuccessCount();
                    break;
                default:
                    mgr.updateApiCallSuccessCount();
                    break;
            }
        } else {
            switch (http) {
                case GET:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyGetCount();
                    mgr.updatePolicyGetFailureCount();
                    break;
                case POST:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyPostCount();
                    mgr.updatePolicyPostFailureCount();
                    break;
                case DELETE:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyDeleteCount();
                    mgr.updatePolicyDeleteFailureCount();
                    break;
                default:
                    mgr.updateApiCallFailureCount();
                    break;
            }
        }
    }

    private void updatePolicyTypeStats(HttpStatus result, HttpMethod http) {
        if (result.equals(HttpStatus.OK)) {
            switch (http) {
                case GET:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyTypeGetCount();
                    mgr.updatePolicyTypeGetSuccessCount();
                    break;
                case POST:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyTypePostCount();
                    mgr.updatePolicyTypePostSuccessCount();
                    break;
                case DELETE:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyTypeDeleteCount();
                    mgr.updatePolicyTypeDeleteSuccessCount();
                    break;
                default:
                    mgr.updateApiCallSuccessCount();
                    break;
            }
        } else {
            switch (http) {
                case GET:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyTypeGetCount();
                    mgr.updatePolicyTypeGetFailureCount();
                    break;
                case POST:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyTypePostCount();
                    mgr.updatePolicyTypePostFailureCount();
                    break;
                case DELETE:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyTypeDeleteCount();
                    mgr.updatePolicyTypeDeleteFailureCount();
                    break;
                default:
                    mgr.updateApiCallFailureCount();
                    break;
            }
        }
    }
}
