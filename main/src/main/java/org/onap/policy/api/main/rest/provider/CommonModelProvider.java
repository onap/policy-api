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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

/**
 * Super class for providers that use a model provider.
 */
public class CommonModelProvider implements AutoCloseable {

    protected final PolicyModelsProvider modelsProvider;

    private static final PfConceptKey LEGACY_OPERATIONAL_TYPE =
            new PfConceptKey("onap.policies.controlloop.Operational", "1.0.0");
    private static final Map<String, PfConceptKey> GUARD_POLICY_TYPE_MAP = new LinkedHashMap<>();

    static {
        GUARD_POLICY_TYPE_MAP.put("guard.frequency.",
                new PfConceptKey("onap.policies.controlloop.guard.FrequencyLimiter:1.0.0"));
        GUARD_POLICY_TYPE_MAP.put("guard.minmax.",
                new PfConceptKey("onap.policies.controlloop.guard.MinMax:1.0.0"));
        GUARD_POLICY_TYPE_MAP.put("guard.blacklist.",
                new PfConceptKey("onap.policies.controlloop.guard.Blacklist:1.0.0"));
    }

    /**
     * Constructs the object, populating {@link #modelsProvider}.
     *
     * @throws PfModelException if an error occurs
     */
    public CommonModelProvider() throws PfModelException {

        ApiParameterGroup parameterGroup = ParameterService.get("ApiGroup");
        PolicyModelsProviderParameters providerParameters = parameterGroup.getDatabaseProviderParameters();
        modelsProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(providerParameters);
    }

    /**
     * Closes the connection to database.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    @Override
    public void close() throws PfModelException {

        modelsProvider.close();
    }

    /**
     * Checks if service template contains any policy.
     *
     * @param serviceTemplate the service template to check against
     *
     * @return boolean whether service template contains any policy
     */
    protected boolean hasPolicy(ToscaServiceTemplate serviceTemplate) {

        return hasData(serviceTemplate.getToscaTopologyTemplate().getPolicies());
    }

    /**
     * Checks if service template contains any policy type.
     *
     * @param serviceTemplate the service template to check against
     *
     * @return boolean whether service template contains any policy type
     */
    protected boolean hasPolicyType(ToscaServiceTemplate serviceTemplate) {

        return hasData(serviceTemplate.getPolicyTypes());
    }

    /**
     * Checks if the first element of a list contains data.
     *
     * @param list list to be examined
     * @return {@code true} if the list contains data, {@code false} otherwise
     */
    protected <T> boolean hasData(List<Map<String, T>> list) {

        return (list != null && !list.isEmpty() && !list.get(0).isEmpty());
    }

    /**
     * Validates that some text represents a number.
     *
     * @param text text to be validated
     * @param errorMsg error message included in the exception, if the text is not a valid
     *        number
     * @throws PfModelException if the text is not a valid number
     */
    protected void validNumber(String text, String errorMsg) throws PfModelException {
        try {
            Integer.parseInt(text);

        } catch (NumberFormatException exc) {
            throw new PfModelException(Response.Status.BAD_REQUEST, errorMsg, exc);
        }
    }

    /**
     * Constructs returned message for policy delete rule violation.
     *
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     * @param pdpGroups the list of pdp groups
     *
     * @return the constructed message
     */
    protected String constructDeletePolicyViolationMessage(String policyId, String policyVersion,
                    List<PdpGroup> pdpGroups) {

        List<String> pdpGroupNameVersionList = new ArrayList<>(pdpGroups.size());
        for (PdpGroup pdpGroup : pdpGroups) {
            pdpGroupNameVersionList.add(pdpGroup.getName() + ":" + pdpGroup.getVersion());
        }
        String deployedPdpGroups = String.join(",", pdpGroupNameVersionList);
        return "policy with ID " + policyId + ":" + policyVersion
                        + " cannot be deleted as it is deployed in pdp groups " + deployedPdpGroups;
    }

    /**
     * Constructs returned message for policy type delete rule violation.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policies the list of policies that parameterizes specified policy type
     *
     * @return the constructed message
     */
    protected String constructDeletePolicyTypeViolationMessage(String policyTypeId, String policyTypeVersion,
                    List<ToscaPolicy> policies) {

        List<String> policyNameVersionList = new ArrayList<>(policies.size());
        for (ToscaPolicy policy : policies) {
            policyNameVersionList.add(policy.getName() + ":" + policy.getVersion());
        }
        String parameterizedPolicies = String.join(",", policyNameVersionList);
        return "policy type with ID " + policyTypeId + ":" + policyTypeVersion
                        + " cannot be deleted as it is parameterized by policies " + parameterizedPolicies;
    }

    /**
     * Collects all deployed versions of specified tosca policy in all pdp groups.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     *
     * @return a map between pdp group and deployed versions of specified policy in that group
     *
     * @throws PfModelException the PfModel parsing exception
     */
    protected Map<Pair<String, String>, List<ToscaPolicy>> collectDeployedToscaPolicies(
            String policyTypeId, String policyTypeVersion, String policyId) throws PfModelException {

        List<PdpGroup> pdpGroups = getPolicyTypeFilteredPdpGroups(policyTypeId, policyTypeVersion);
        hasActivePdpGroup(pdpGroups, policyTypeId, policyTypeVersion, policyId);

        Map<Pair<String, String>, List<ToscaPolicy>> deployedPolicyMap =
                constructDeployedPolicyMap(pdpGroups, policyId);
        hasDeployedToscaPolicyVersion(deployedPolicyMap, policyTypeId, policyTypeVersion, policyId);

        return deployedPolicyMap;
    }

    /**
     * Collects all deployed versions of specified operational policy in all pdp groups.
     *
     * @param policyId the ID of policy
     *
     * @return a map between pdp group and deployed versions of specified operational policy in that group
     *
     * @throws PfModelException the PfModel parsing exception
     */
    protected Map<Pair<String, String>, List<LegacyOperationalPolicy>> collectDeployedOperationalPolicies(
            String policyId) throws PfModelException {

        List<PdpGroup> pdpGroups = getPolicyTypeFilteredPdpGroups(
                LEGACY_OPERATIONAL_TYPE.getName(), LEGACY_OPERATIONAL_TYPE.getVersion());
        hasActivePdpGroup(pdpGroups, LEGACY_OPERATIONAL_TYPE.getName(), LEGACY_OPERATIONAL_TYPE.getVersion(),
                policyId);

        Map<Pair<String, String>, List<LegacyOperationalPolicy>> deployedPolicyMap =
                constructDeployedOpPolicyMap(pdpGroups, policyId);
        hasDeployedOpPolicyVersion(deployedPolicyMap, LEGACY_OPERATIONAL_TYPE.getName(),
                LEGACY_OPERATIONAL_TYPE.getVersion(), policyId);

        return deployedPolicyMap;
    }

    /**
     * Collects all deployed versions of specified guard policy in all pdp groups.
     *
     * @param policyId the ID of policy
     *
     * @return a map between pdp group and deployed versions of specified guard policy in that group
     *
     * @throws PfModelException the PfModel parsing exception
     */
    protected Map<Pair<String, String>, Map<String, LegacyGuardPolicyOutput>> collectDeployedGuardPolicies(
            String policyId) throws PfModelException {

        PfConceptKey guardPolicyType = getGuardPolicyType(policyId);
        if (guardPolicyType == null) {
            throw new PfModelException(Response.Status.BAD_REQUEST, "No policy type defined for " + policyId);
        }

        List<PdpGroup> pdpGroups =
                getPolicyTypeFilteredPdpGroups(guardPolicyType.getName(), guardPolicyType.getVersion());
        hasActivePdpGroup(pdpGroups, guardPolicyType.getName(), guardPolicyType.getVersion(), policyId);

        Map<Pair<String, String>, Map<String, LegacyGuardPolicyOutput>> deployedPolicyMap =
                constructDeployedGuardPolicyMap(pdpGroups, policyId);
        hasDeployedGuardPolicyVersion(deployedPolicyMap,
                guardPolicyType.getName(), guardPolicyType.getVersion(), policyId);

        return deployedPolicyMap;
    }

    /**
     * Checks if the list of pdp groups is empty.
     * If so, throws exception saying specified policy deployment is not found in all existing pdp groups.
     *
     * @param pdpGroups the list of pdp groups to check against
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void hasActivePdpGroup(List<PdpGroup> pdpGroups, String policyTypeId,
            String policyTypeVersion, String policyId) throws PfModelException {

        if (pdpGroups.isEmpty()) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructDeploymentNotFoundMessage(policyTypeId, policyTypeVersion, policyId));
        }
    }

    /**
     * Checks if deployed version of specified tosca policy is found in any pdp group.
     *
     * @param deployedPolicyMap the map between pdp group and deployed versions of specified tosca policy in that group
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void hasDeployedToscaPolicyVersion(Map<Pair<String, String>, List<ToscaPolicy>> deployedPolicyMap,
            String policyTypeId, String policyTypeVersion, String policyId) throws PfModelException {

        if (deployedPolicyMap.isEmpty()) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructDeploymentNotFoundMessage(policyTypeId, policyTypeVersion, policyId));
        }
    }

    /**
     * Checks if deployed version of specified operational policy is found in any pdp group.
     *
     * @param deployedPolicyMap the map between pdp group and deployed operational policy versions in that group
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void hasDeployedOpPolicyVersion(Map<Pair<String, String>, List<LegacyOperationalPolicy>> deployedPolicyMap,
            String policyTypeId, String policyTypeVersion, String policyId) throws PfModelException {

        if (deployedPolicyMap.isEmpty()) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructDeploymentNotFoundMessage(policyTypeId, policyTypeVersion, policyId));
        }
    }

    /**
     * Checks if deployed version of specified guard policy is found in any pdp group.
     *
     * @param deployedPolicyMap the map between pdp group and deployed versions of specified guard policy in that group
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void hasDeployedGuardPolicyVersion(
            Map<Pair<String, String>, Map<String, LegacyGuardPolicyOutput>> deployedPolicyMap,
            String policyTypeId, String policyTypeVersion, String policyId) throws PfModelException {

        if (deployedPolicyMap.isEmpty()) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructDeploymentNotFoundMessage(policyTypeId, policyTypeVersion, policyId));
        }
    }

    /**
     * Retrieves all pdp groups supporting specified policy type.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @return a list of pdp groups supporting specified policy type
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private List<PdpGroup> getPolicyTypeFilteredPdpGroups(String policyTypeId, String policyTypeVersion)
            throws PfModelException {

        List<ToscaPolicyTypeIdentifier> policyTypes = new ArrayList<>();
        policyTypes.add(new ToscaPolicyTypeIdentifier(policyTypeId, policyTypeVersion));
        PdpGroupFilter pdpGroupFilter = PdpGroupFilter.builder().policyTypeList(policyTypes)
                .groupState(PdpState.ACTIVE).pdpState(PdpState.ACTIVE).build();
        return modelsProvider.getFilteredPdpGroups(pdpGroupFilter);
    }

    /**
     * Constructs the map of deployed pdp groups and deployed policies.
     *
     * @param pdpGroups the list of pdp groups that contain the specified policy
     * @param policyId the ID of policy
     *
     * @return the constructed map of pdp groups and deployed policies
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private Map<Pair<String, String>, List<ToscaPolicy>> constructDeployedPolicyMap(
            List<PdpGroup> pdpGroups, String policyId) throws PfModelException {

        Map<Pair<String, String>, List<ToscaPolicy>> deployedPolicyMap = new HashMap<>();
        for (PdpGroup pdpGroup : pdpGroups) {
            List<ToscaPolicyIdentifier> policyIdentifiers = extractPolicyIdentifiers(policyId, pdpGroup);
            List<ToscaPolicy> deployedPolicies = getDeployedPolicies(policyIdentifiers);
            if (!deployedPolicies.isEmpty()) {
                deployedPolicyMap.put(Pair.of(pdpGroup.getName(), pdpGroup.getVersion()), deployedPolicies);
            }
        }
        return deployedPolicyMap;
    }

    /**
     * Constructs the map of deployed pdp groups and deployed operational policies.
     *
     * @param pdpGroups the list of pdp groups that contain the specified operational policy
     * @param policyId the ID of policy
     *
     * @return the constructed map of pdp groups and deployed operational policies
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private Map<Pair<String, String>, List<LegacyOperationalPolicy>> constructDeployedOpPolicyMap(
            List<PdpGroup> pdpGroups, String policyId) throws PfModelException {

        Map<Pair<String, String>, List<LegacyOperationalPolicy>> deployedOpPolicyMap = new HashMap<>();
        for (PdpGroup pdpGroup : pdpGroups) {
            List<ToscaPolicyIdentifier> policyIdentifiers = extractPolicyIdentifiers(policyId, pdpGroup);
            List<LegacyOperationalPolicy> deployedOpPolicies = getDeployedOpPolicies(policyIdentifiers);
            if (!deployedOpPolicies.isEmpty()) {
                deployedOpPolicyMap.put(Pair.of(pdpGroup.getName(), pdpGroup.getVersion()), deployedOpPolicies);
            }
        }
        return deployedOpPolicyMap;
    }

    /**
     * Constructs the map of deployed pdp groups and deployed guard policies.
     *
     * @param pdpGroups the list of pdp groups that contain the specified guard policy
     * @param policyId the ID of policy
     *
     * @return the constructed map of pdp groups and deployed guard policies
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private Map<Pair<String, String>, Map<String, LegacyGuardPolicyOutput>> constructDeployedGuardPolicyMap(
            List<PdpGroup> pdpGroups, String policyId) throws PfModelException {

        Map<Pair<String, String>, Map<String, LegacyGuardPolicyOutput>> deployedGuardPolicyMap = new HashMap<>();
        for (PdpGroup pdpGroup : pdpGroups) {
            List<ToscaPolicyIdentifier> policyIdentifiers = extractPolicyIdentifiers(policyId, pdpGroup);
            Map<String, LegacyGuardPolicyOutput> deployedGuardPolicies = getDeployedGuardPolicies(policyIdentifiers);
            if (!deployedGuardPolicies.isEmpty()) {
                deployedGuardPolicyMap.put(Pair.of(pdpGroup.getName(), pdpGroup.getVersion()), deployedGuardPolicies);
            }
        }
        return deployedGuardPolicyMap;
    }

    /**
     * Extract policy identifiers matching specified policy ID from specified pdp group.
     *
     * @param policyId the ID of policy to match
     * @param pdpGroup the target pdp group to search
     *
     * @return the list of policy identifiers
     */
    private List<ToscaPolicyIdentifier> extractPolicyIdentifiers(String policyId, PdpGroup pdpGroup) {
        List<ToscaPolicyIdentifier> policyIdentifiers = new ArrayList<>();
        for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
            for (ToscaPolicyIdentifier policyIdentifier : pdpSubGroup.getPolicies()) {
                if (policyId.equalsIgnoreCase(policyIdentifier.getName())) {
                    policyIdentifiers.add(policyIdentifier);
                }
            }
        }
        return policyIdentifiers;
    }

    /**
     * Retrieves the list of tosca policies located by their corresponding identifiers.
     *
     * @param policyIdentifiers the list of policy identifiers
     *
     * @return the list of tosca policies
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private List<ToscaPolicy> getDeployedPolicies(List<ToscaPolicyIdentifier> policyIdentifiers)
                    throws PfModelException {

        List<ToscaPolicy> deployedPolicies = new ArrayList<>();
        if (!policyIdentifiers.isEmpty()) {
            for (ToscaPolicyIdentifier policyIdentifier : policyIdentifiers) {
                deployedPolicies.addAll(
                        modelsProvider.getPolicyList(policyIdentifier.getName(), policyIdentifier.getVersion()));
            }
        }
        return deployedPolicies;
    }

    /**
     * Retrieves the list of operational policies located by their corresponding identifiers.
     *
     * @param policyIdentifiers the list of policy identifiers
     *
     * @return the list of operational policies
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private List<LegacyOperationalPolicy> getDeployedOpPolicies(List<ToscaPolicyIdentifier> policyIdentifiers)
                    throws PfModelException {

        List<LegacyOperationalPolicy> deployedOpPolicies = new ArrayList<>();
        if (!policyIdentifiers.isEmpty()) {
            for (ToscaPolicyIdentifier policyIdentifier : policyIdentifiers) {
                deployedOpPolicies.add(modelsProvider.getOperationalPolicy(policyIdentifier.getName(),
                        policyIdentifier.getVersion().split("\\.")[0]));
            }
        }
        return deployedOpPolicies;
    }

    /**
     * Retrieves the list of guard policies located by their corresponding identifiers.
     *
     * @param policyIdentifiers the list of policy identifiers
     *
     * @return the list of guard policies
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private Map<String, LegacyGuardPolicyOutput> getDeployedGuardPolicies(
            List<ToscaPolicyIdentifier> policyIdentifiers) throws PfModelException {

        Map<String, LegacyGuardPolicyOutput> deployedGuardPolicies = new HashMap<>();
        if (!policyIdentifiers.isEmpty()) {
            for (ToscaPolicyIdentifier policyIdentifier : policyIdentifiers) {
                deployedGuardPolicies.putAll(modelsProvider.getGuardPolicy(policyIdentifier.getName(),
                        policyIdentifier.getVersion().split("\\.")[0]));
            }
        }
        return deployedGuardPolicies;
    }

    /**
     * Constructs returned message for not found policy deployment.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     *
     * @return constructed message
     */
    private String constructDeploymentNotFoundMessage(String policyTypeId, String policyTypeVersion, String policyId) {

        return "could not find policy with ID " + policyId + " and type "
                + policyTypeId + ":" + policyTypeVersion + " deployed in any pdp group";
    }

    /**
     * Retrieves guard policy type given guard policy ID.
     *
     * @param policyId the ID of guard policy
     *
     * @return the concept key of guard policy type
     */
    private PfConceptKey getGuardPolicyType(String policyId) {

        if (policyId == null) {
            return null;
        }
        for (Entry<String, PfConceptKey> guardPolicyTypeEntry : GUARD_POLICY_TYPE_MAP.entrySet()) {
            if (policyId.startsWith(guardPolicyTypeEntry.getKey())) {
                return guardPolicyTypeEntry.getValue();
            }
        }
        return null;
    }
}
