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
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;
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

        ToscaServiceTemplate serviceTemplate;
        if (policyTypeId == null || policyTypeVersion == null) {
            ToscaPolicyTypeFilter policyTypeFilter = ToscaPolicyTypeFilter.builder()
                    .name(policyTypeId).version(policyTypeVersion).build();
            serviceTemplate = modelsProvider.getFilteredPolicyTypes(policyTypeFilter);
        } else {
            serviceTemplate = modelsProvider.getPolicyTypes(policyTypeId, policyTypeVersion);
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
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void validateDeleteEligibility(String policyTypeId, String policyTypeVersion) throws PfModelException {

        PdpGroupFilter pdpGroupFilter = PdpGroupFilter.builder()
                .policyType(new ToscaPolicyTypeIdentifier(policyTypeId, policyTypeVersion)).build();

        List<PdpGroup> pdpGroups = modelsProvider.getFilteredPdpGroups(pdpGroupFilter);
        if (!pdpGroups.isEmpty()) {
            throw new PfModelException(Response.Status.CONFLICT,
                    "the policy type is parameterized by at least one policies that have been deployed in pdp group");
        }
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
