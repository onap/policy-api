/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.List;
import javax.ws.rs.core.Response;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * Class to provide all kinds of policy type operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class PolicyTypeProvider {

    private PolicyModelsProvider modelsProvider;

    /**
     * Default constructor.
     */
    public PolicyTypeProvider() throws PfModelException {

        ApiParameterGroup parameterGroup = ParameterService.get("ApiGroup");
        PolicyModelsProviderParameters providerParameters = parameterGroup.getDatabaseProviderParameters();
        modelsProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(providerParameters);
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

        ToscaPolicyTypeFilter policyTypeFilter = ToscaPolicyTypeFilter.builder()
                .name(policyTypeId).version(policyTypeVersion).build();
        ToscaServiceTemplate serviceTemplate = modelsProvider.getFilteredPolicyTypes(policyTypeFilter);
        if (policyTypeId != null && serviceTemplate.getPolicyTypes().isEmpty()) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructResourceNotFoundMessage(policyTypeId, policyTypeVersion));
        }

        close();
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

        ToscaPolicyTypeFilter policyTypeFilter = ToscaPolicyTypeFilter.builder()
                .name(policyTypeId).version(ToscaPolicyTypeFilter.LATEST_VERSION).build();
        ToscaServiceTemplate serviceTemplate = modelsProvider.getFilteredPolicyTypes(policyTypeFilter);
        if (serviceTemplate.getPolicyTypes().isEmpty()) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructResourceNotFoundMessage(policyTypeId, null));
        }

        close();
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

        ToscaServiceTemplate serviceTemplate = modelsProvider.createPolicyTypes(body);

        close();
        return serviceTemplate;
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

        close();
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
                    "the policy type has been parameterized by at least one policies");
        }
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
        StringBuilder builder = new StringBuilder().append("policy type with ID ").append(policyTypeId)
                .append(":").append(policyTypeVersion).append(" cannot be found");
        return builder.toString();
    }

    /**
     * Closes the connection to database.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void close() throws PfModelException {
        try {
            modelsProvider.close();
        } catch (Exception e) {
            throw new PfModelException(
                    Response.Status.INTERNAL_SERVER_ERROR, "error closing connection to database", e);
        }
    }
}
