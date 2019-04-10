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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;
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

        ToscaPolicyFilter policyFilter = ToscaPolicyFilter.builder()
                .name(policyId).version(policyVersion)
                .type(policyTypeId).typeVersion(policyTypeVersion).build();
        ToscaServiceTemplate serviceTemplate = modelsProvider.getFilteredPolicies(policyFilter);

        if (!hasPolicy(serviceTemplate)) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructResourceNotFoundMessage(policyTypeId, policyTypeVersion, policyId, policyVersion));
        }

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

        ToscaPolicyFilter policyFilter = ToscaPolicyFilter.builder()
                .name(policyId).version(ToscaPolicyFilter.LATEST_VERSION)
                .type(policyTypeId).typeVersion(policyTypeVersion).build();
        ToscaServiceTemplate serviceTemplate = modelsProvider.getFilteredPolicies(policyFilter);

        if (!hasPolicy(serviceTemplate)) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructResourceNotFoundMessage(policyTypeId, policyTypeVersion, policyId, null));
        }

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
    public Map<Pair<String, String>, List<ToscaPolicy>> fetchDeployedPolicies(
            String policyTypeId, String policyTypeVersion, String policyId) throws PfModelException {

        List<ToscaPolicyTypeIdentifier> policyTypes = new ArrayList<>();
        policyTypes.add(new ToscaPolicyTypeIdentifier(policyTypeId, policyTypeVersion));
        PdpGroupFilter pdpGroupFilter = PdpGroupFilter.builder().policyTypeList(policyTypes).build();
        List<PdpGroup> pdpGroups = modelsProvider.getFilteredPdpGroups(pdpGroupFilter);

        if (pdpGroups.isEmpty()) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructDeploymentNotFoundMessage(policyTypeId, policyTypeVersion, policyId));
        }

        Map<Pair<String, String>, List<ToscaPolicy>> deployedPolicyMap = constructDeployedPolicyMap(
                pdpGroups, policyId);
        if (deployedPolicyMap.isEmpty()) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructDeploymentNotFoundMessage(policyTypeId, policyTypeVersion, policyId));
        }

        close();
        return deployedPolicyMap;
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

        validatePolicyTypeExist(policyTypeId, policyTypeVersion);
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

        validateDeleteEligibility(policyTypeId, policyTypeVersion, policyId, policyVersion);

        ToscaServiceTemplate serviceTemplate = modelsProvider.deletePolicy(policyId, policyVersion);

        if (!hasPolicy(serviceTemplate)) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructResourceNotFoundMessage(policyTypeId, policyTypeVersion, policyId, policyVersion));
        }

        close();
        return serviceTemplate;
    }

    /**
     * Validates whether policy type exists.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void validatePolicyTypeExist(String policyTypeId, String policyTypeVersion) throws PfModelException {

        try {
            modelsProvider.getPolicyTypes(policyTypeId, policyTypeVersion);
        } catch (Exception e) {
            throw new PfModelException(Response.Status.NOT_FOUND, "specified policy type does not exist", e);
        }
    }

    /**
     * Validates the match between policy type specified in path and the one specified in type of policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param serviceTemplate the ToscaServiceTemplate to validate
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void validatePolicyTypeMatch(String policyTypeId, String policyTypeVersion,
            ToscaServiceTemplate serviceTemplate) throws PfModelException {

        List<Map<String, ToscaPolicy>> policies = serviceTemplate.getToscaTopologyTemplate().getPolicies();
        for (Map<String, ToscaPolicy> policy : policies) {
            if (policy.size() > 1) {
                throw new PfModelException(Response.Status.BAD_REQUEST,
                        "one policy block contains more than one policies");
            }
            ToscaPolicy policyContent = policy.values().iterator().next();
            if (!policyTypeId.equalsIgnoreCase(policyContent.getType())) {
                throw new PfModelException(Response.Status.BAD_REQUEST, "policy type id does not match");
            }
            if (policyContent.getTypeVersion() != null
                    && !policyTypeVersion.equalsIgnoreCase(policyContent.getTypeVersion())) {
                throw new PfModelException(Response.Status.BAD_REQUEST, "policy type version does not match");
            }
        }
    }

    /**
     * Validates whether specified policy can be deleted based on the rule that deployed policy cannot be deleted.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void validateDeleteEligibility(String policyTypeId, String policyTypeVersion,
            String policyId, String policyVersion) throws PfModelException {

        List<ToscaPolicyTypeIdentifier> policyTypes = new ArrayList<>();
        policyTypes.add(new ToscaPolicyTypeIdentifier(policyTypeId, policyTypeVersion));
        List<ToscaPolicyIdentifier> policies = new ArrayList<>();
        policies.add(new ToscaPolicyIdentifier(policyId, policyVersion));
        PdpGroupFilter pdpGroupFilter = PdpGroupFilter.builder()
                .policyTypeList(policyTypes).policyList(policies).build();

        List<PdpGroup> pdpGroups = modelsProvider.getFilteredPdpGroups(pdpGroupFilter);

        if (!pdpGroups.isEmpty()) {
            throw new PfModelException(Response.Status.CONFLICT, "the policy has been deployed in pdp group");
        }
    }

    private Map<Pair<String, String>, List<ToscaPolicy>> constructDeployedPolicyMap(
            List<PdpGroup> pdpGroups, String policyId) throws PfModelException {

        Map<Pair<String, String>, List<ToscaPolicy>> deployedPolicyMap = new HashMap<>();
        for (PdpGroup pdpGroup : pdpGroups) {
            List<ToscaPolicyIdentifier> policyIdentifiers = new ArrayList<>();
            for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
                for (ToscaPolicyIdentifier policyIdentifier : pdpSubGroup.getPolicies()) {
                    if (policyId.equalsIgnoreCase(policyIdentifier.getName())) {
                        policyIdentifiers.add(policyIdentifier);
                    }
                }
            }
            List<ToscaPolicy> deployedPolicies = new ArrayList<>();
            if (!policyIdentifiers.isEmpty()) {
                for (ToscaPolicyIdentifier policyIdentifier : policyIdentifiers) {
                    deployedPolicies.addAll(
                            modelsProvider.getPolicyList(policyIdentifier.getName(), policyIdentifier.getVersion()));
                }
            }
            if (!deployedPolicies.isEmpty()) {
                deployedPolicyMap.put(Pair.of(pdpGroup.getName(), pdpGroup.getVersion()), deployedPolicies);
            }
        }
        return deployedPolicyMap;
    }

    /**
     * Constructs returned message for not found resource.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return constructed message
     */
    private String constructResourceNotFoundMessage(String policyTypeId, String policyTypeVersion,
            String policyId, String policyVersion) {

        return "policy with ID " + policyId + ":" + policyVersion
                + " and type " + policyTypeId + ":" + policyTypeVersion + " does not exist";
    }

    /**
     * Constructs returned message for not found policy deployment.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return constructed message
     */
    private String constructDeploymentNotFoundMessage(String policyTypeId, String policyTypeVersion,
            String policyId) {

        return "could not find policy with ID " + policyId + " and type "
                + policyTypeId + ":" + policyTypeVersion + " deployed in any pdp group";
    }

    /**
     * Checks if service template contains any policy.
     *
     * @param serviceTemplate the service template to check against
     *
     * @return boolean whether service template contains any policy
     */
    private boolean hasPolicy(ToscaServiceTemplate serviceTemplate) {

        if (serviceTemplate.getToscaTopologyTemplate().getPolicies() == null) {
            return false;
        } else if (serviceTemplate.getToscaTopologyTemplate().getPolicies().isEmpty()) {
            return false;
        } else if (serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).isEmpty()) {
            return false;
        } else {
            return true;
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
