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
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
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
public class PolicyProvider extends CommonModelProvider {

    /**
     * Default constructor.
     */
    public PolicyProvider() throws PfModelException {
        super();
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

        return collectDeployedPolicies(policyId, new PfConceptKey(policyTypeId, policyTypeVersion),
                modelsProvider::getPolicyList, List::addAll, new ArrayList<>());
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

        return modelsProvider.createPolicies(body);
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

        ToscaServiceTemplate serviceTemplate = modelsProvider.getPolicyTypes(policyTypeId, policyTypeVersion);
        if (!hasPolicyType(serviceTemplate)) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    "policy type with ID " + policyTypeId + ":" + policyTypeVersion + " does not exist");
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
            throw new PfModelException(Response.Status.CONFLICT,
                    constructDeletePolicyViolationMessage(policyId, policyVersion, pdpGroups));
        }
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
}
