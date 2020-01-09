/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.api.main.rest.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.Response;

import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * Class to provide all kinds of policy type operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class PolicyTypeProvider extends CommonModelProvider {

    /**
     * Default constructor.
     */
    public PolicyTypeProvider() throws PfModelException {
        super();
    }

    /**
     * Retrieves a list of policy types matching specified policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate fetchPolicyTypes(String policyTypeId, String policyTypeVersion)
            throws PfModelException {

        ToscaServiceTemplate serviceTemplate = getFilteredPolicyTypes(policyTypeId, policyTypeVersion);

        if (policyTypeId != null && !hasPolicyType(serviceTemplate)) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructResourceNotFoundMessage(policyTypeId, policyTypeVersion));
        }

        return serviceTemplate;
    }

    /**
     * Retrieves a list of policy types with the latest versions.
     *
     * @param policyTypeId the ID of policy type
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate fetchLatestPolicyTypes(String policyTypeId) throws PfModelException {

        ToscaServiceTemplate serviceTemplate =
                getFilteredPolicyTypes(policyTypeId, ToscaPolicyTypeFilter.LATEST_VERSION);
        if (!hasPolicyType(serviceTemplate)) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructResourceNotFoundMessage(policyTypeId, null));
        }

        return serviceTemplate;
    }

    /**
     * Creates a new policy type.
     *
     * @param body the entity body of policy type
     *
     * @return the ToscaServiceTemplate object
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate createPolicyType(ToscaServiceTemplate body) throws PfModelException {

        if (!hasPolicyType(body)) {
            throw new PfModelException(Response.Status.BAD_REQUEST,
                    "no policy types specified in the service template");
        }
        validatePolicyTypeVersion(body);
        return modelsProvider.createPolicyTypes(body);
    }

    /**
     * Delete the policy type matching specified policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate deletePolicyType(String policyTypeId, String policyTypeVersion)
            throws PfModelException {

        validateDeleteEligibility(policyTypeId, policyTypeVersion);

        ToscaServiceTemplate serviceTemplate = modelsProvider.deletePolicyType(policyTypeId, policyTypeVersion);

        if (!hasPolicyType(serviceTemplate)) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructResourceNotFoundMessage(policyTypeId, policyTypeVersion));
        }

        return serviceTemplate;
    }

    /**
     * Validates whether specified policy type can be deleted based on the rule that
     * policy type parameterized by at least one policies cannot be deleted.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void validateDeleteEligibility(String policyTypeId, String policyTypeVersion) throws PfModelException {

        ToscaPolicyFilter policyFilter = ToscaPolicyFilter.builder()
                .type(policyTypeId).typeVersion(policyTypeVersion).build();
        List<ToscaPolicy> policies = modelsProvider.getFilteredPolicyList(policyFilter);
        if (!policies.isEmpty()) {
            throw new PfModelException(Response.Status.CONFLICT,
                    constructDeletePolicyTypeViolationMessage(policyTypeId, policyTypeVersion, policies));
        }
    }

    /**
     * Validates the provided policy type version in the payload.
     *
     * @param body the provided TOSCA service template which contains the policy types
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void validatePolicyTypeVersion(ToscaServiceTemplate body) throws PfModelException {

        validatePolicyTypeVersionExist(body);
        validateNoDuplicateVersionInDb(body);
    }

    /**
     * Validates that each policy type has a version specified in the payload.
     *
     * @param body the TOSCA service template payload to check against
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void validatePolicyTypeVersionExist(ToscaServiceTemplate body) throws PfModelException {

        List<String> invalidPolicyTypeNames = new ArrayList<>(5);
        for (Entry<String, ToscaPolicyType> policyType: body.getPolicyTypes().entrySet()) {
            if (!"tosca.policies.Root".equals(policyType.getValue().getDerivedFrom())
                    && policyType.getValue().getVersion() == null) {
                invalidPolicyTypeNames.add(policyType.getKey());
            }
        }

        if (!invalidPolicyTypeNames.isEmpty()) {
            String errorMsg = "mandatory 'version' field is missing in policy types: "
                    + String.join(", ", invalidPolicyTypeNames);
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errorMsg);
        }
    }

    /**
     * Validates that there is no duplicate version of the policy type stored in the database.
     *
     * @param body the TOSCA service template payload
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void validateNoDuplicateVersionInDb(ToscaServiceTemplate body) throws PfModelException {

        Map<String, String> invalidPolicyTypes = new HashMap<>();
        for (Entry<String, ToscaPolicyType> policyType: body.getPolicyTypes().entrySet()) {
            if ("tosca.policies.Root".equals(policyType.getValue().getDerivedFrom())) {
                continue;
            }
            String policyTypeName = policyType.getKey();
            String policyTypeVersion = policyType.getValue().getVersion();
            ToscaServiceTemplate serviceTemplate = getFilteredPolicyTypes(policyTypeName, policyTypeVersion);
            if (hasPolicyType(serviceTemplate)) {
                String latestVersion = getFilteredPolicyTypes(policyTypeName, ToscaPolicyTypeFilter.LATEST_VERSION)
                        .getPolicyTypesAsMap().entrySet().iterator().next().getKey().getVersion();
                invalidPolicyTypes.put(String.join(":", policyTypeName, policyTypeVersion), latestVersion);
            }
        }

        if (!invalidPolicyTypes.isEmpty()) {
            List<String> duplicateVersions = new ArrayList<>(5);
            for (Entry<String, String> invalidPolicyType : invalidPolicyTypes.entrySet()) {
                String eachDuplicateVersion = "policy type " + invalidPolicyType.getKey()
                    + " already exists; its latest version is " + invalidPolicyType.getValue();
                duplicateVersions.add(eachDuplicateVersion);
            }
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, String.join("\n", duplicateVersions));
        }
    }

    /**
     * Retrieves the specified version of the policy type.
     *
     * @param policyTypeName the name of the policy type
     * @param policyTypeVersion the version of the policy type
     *
     * @return the TOSCA service template containing the specified version of the policy type
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private ToscaServiceTemplate getFilteredPolicyTypes(String policyTypeName, String policyTypeVersion)
            throws PfModelException {

        ToscaPolicyTypeFilter policyTypeFilter = ToscaPolicyTypeFilter.builder()
                .name(policyTypeName).version(policyTypeVersion).build();
        return modelsProvider.getFilteredPolicyTypes(policyTypeFilter);
    }

    /**
     * Constructs returned message for not found resource.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @return constructed message
     */
    private String constructResourceNotFoundMessage(String policyTypeId, String policyTypeVersion) {

        return "policy type with ID " + policyTypeId + ":" + policyTypeVersion + " does not exist";
    }
}
