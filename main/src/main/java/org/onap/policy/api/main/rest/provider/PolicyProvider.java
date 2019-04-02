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
import java.util.Map;
import javax.ws.rs.core.Response;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * Class to provide all kinds of policy operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class PolicyProvider {

    private PolicyModelsProvider modelsProvider;

    /**
     * Default constructor.
     */
    public PolicyProvider() throws PfModelException {

        ApiParameterGroup parameterGroup = ParameterService.get("ApiGroup");
        PolicyModelsProviderParameters providerParameters = parameterGroup.getDatabaseProviderParameters();
        modelsProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(providerParameters);
        modelsProvider.init();
    }

    /**
     * Retrieves a list of policies matching specified ID and version of both policy type and policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate fetchPolicies(String policyTypeId, String policyTypeVersion,
            String policyId, String policyVersion) throws PfModelException {

        validatePathParam(policyTypeId, policyTypeVersion, policyId, policyVersion);
        ToscaServiceTemplate serviceTemplate = modelsProvider.getPolicies(policyId, policyVersion);
        close();
        return serviceTemplate;
    }

    /**
     * Retrieves a list of policies with the latest versions that match specified policy type id and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of the policy
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate fetchLatestPolicies(String policyTypeId, String policyTypeVersion,
            String policyId) throws PfModelException {

        validatePathParam(policyTypeId, policyTypeVersion, policyId, null);
        ToscaServiceTemplate serviceTemplate = modelsProvider.getLatestPolicies(policyId);
        close();
        return serviceTemplate;
    }

    /**
     * Retrieves a list of deployed policies in each pdp group.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of the policy
     *
     * @return a list of deployed policies in each pdp group
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public Map<PdpGroup, List<ToscaPolicy>> fetchDeployedPolicies(String policyTypeId, String policyTypeVersion,
            String policyId) throws PfModelException {

        validatePathParam(policyTypeId, policyTypeVersion, policyId, null);
        Map<PdpGroup, List<ToscaPolicy>> deployedPolicies = modelsProvider.getDeployedPolicyList(policyId);
        close();
        return deployedPolicies;
    }

    /**
     * Creates a new policy for a policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param body the entity body of policy
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate createPolicy(String policyTypeId, String policyTypeVersion,
                                             ToscaServiceTemplate body) throws PfModelException {

        validatePathParam(policyTypeId, policyTypeVersion, null, null);
        validatePolicyTypeMatch(policyTypeId, policyTypeVersion, body);
        ToscaServiceTemplate serviceTemplate = modelsProvider.createPolicies(body);
        close();
        return serviceTemplate;
    }

    /**
     * Deletes the policy matching specified ID and version of both policy type and policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate deletePolicy(String policyTypeId, String policyTypeVersion,
                                 String policyId, String policyVersion) throws PfModelException {

        validatePathParam(policyTypeId, policyTypeVersion, policyId, policyVersion);
        ToscaServiceTemplate serviceTemplate = modelsProvider.deletePolicy(policyId, policyVersion);
        close();
        return serviceTemplate;
    }

    /**
     * Checks the validation of policy type and policy info passed in as path param.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void validatePathParam(String policyTypeId, String policyTypeVersion,
            String policyId, String policyVersion) throws PfModelException {

        if (policyId == null && policyVersion != null) {
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, "concrete polciy version but null policy id");
        }
        if (policyTypeId == null || policyTypeVersion == null) {
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE,
                    "either policy type id or version is null");
        }

        // Check policy type existence
        try {
            modelsProvider.getPolicyTypes(policyTypeId, policyTypeVersion);
        } catch (Exception e) {
            throw new PfModelException(Response.Status.NOT_FOUND, "specified policy type does not exist", e);
        }
    }

    /**
     * Validates the match between policy type specified in path param and the one specified in type of policy.
     *
     * @param body the ToscaServiceTemplate to create
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void validatePolicyTypeMatch(String policyTypeId, String policyTypeVersion, ToscaServiceTemplate body)
            throws PfModelException {

        List<Map<String, ToscaPolicy>> policies = body.getToscaTopologyTemplate().getPolicies();
        for (Map<String, ToscaPolicy> policy : policies) {
            if (policy.size() != 1) {
                throw new PfModelException(Response.Status.BAD_REQUEST,
                        "one policy block contains more than one policies");
            }
            ToscaPolicy policyContent = policy.values().iterator().next();
            if (!policyTypeId.equalsIgnoreCase(policyContent.getType())
                    || !policyTypeVersion.equalsIgnoreCase(policyContent.getVersion())) {
                throw new PfModelException(Response.Status.BAD_REQUEST, "policy type info does not match");
            }
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