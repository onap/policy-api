/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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
import java.util.function.BiConsumer;
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

/**
 * Super class for providers that use a model provider.
 */
public class CommonModelProvider implements AutoCloseable {

    protected final PolicyModelsProvider modelsProvider;

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
     * Checks if the first element of a list of maps contains data.
     *
     * @param listOfMapsToCheck list of maps to be examined
     * @return {@code true} if the list contains data, {@code false} otherwise
     */
    protected <T> boolean hasData(List<Map<String, T>> listOfMapsToCheck) {

        return (listOfMapsToCheck != null && !listOfMapsToCheck.isEmpty() && !listOfMapsToCheck.get(0).isEmpty());
    }


    /**
     * Checks if a maps contains data.
     *
     * @param mapToCheck map to be examined
     * @return {@code true} if the list contains data, {@code false} otherwise
     */
    protected <T> boolean hasData(Map<String, T> mapToCheck) {

        // We don't allow a null or empty map as well as a map entry with a valid key but null value
        return (mapToCheck != null && !mapToCheck.isEmpty() && !mapToCheck.containsValue(null));
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
     * Collects all deployed versions of specified policy in all pdp groups.
     *
     * @param policyId the ID of policy
     * @param policyType the concept key of policy type
     * @param getter the custom generic getter Bifunction
     * @param consumer the BiConsumer
     * @param data the data structure storing retrieved deployed policies
     *
     * @return a map between pdp group and deployed versions of specified policy in that group
     *
     * @throws PfModelException the PfModel parsing exception
     */
    protected <T, R> Map<Pair<String, String>, T> collectDeployedPolicies(String policyId, PfConceptKey policyType,
            BiFunctionWithEx<String, String, R> getter, BiConsumer<T, R> consumer, T data) throws PfModelException {

        List<PdpGroup> pdpGroups = getPolicyTypeFilteredPdpGroups(policyType);
        hasActivePdpGroup(pdpGroups, policyType, policyId);
        return constructDeployedPolicyMap(pdpGroups, policyId, policyType, getter, consumer, data);
    }

    @FunctionalInterface
    protected interface BiFunctionWithEx<T,U,R> {
        public R apply(T value1, U value2) throws PfModelException;
    }

    /**
     * Checks if the list of pdp groups is empty.
     * If so, throws exception saying specified policy deployment is not found in all existing pdp groups.
     *
     * @param pdpGroups the list of pdp groups to check against
     * @param policyType the concept key of policy type
     * @param policyId the ID of policy
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void hasActivePdpGroup(List<PdpGroup> pdpGroups, PfConceptKey policyType, String policyId)
            throws PfModelException {

        if (pdpGroups.isEmpty()) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructDeploymentNotFoundMessage(policyType, policyId));
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
    private List<PdpGroup> getPolicyTypeFilteredPdpGroups(PfConceptKey policyType)
            throws PfModelException {

        List<ToscaPolicyTypeIdentifier> policyTypes = new ArrayList<>();
        policyTypes.add(new ToscaPolicyTypeIdentifier(policyType.getName(), policyType.getVersion()));
        PdpGroupFilter pdpGroupFilter = PdpGroupFilter.builder().policyTypeList(policyTypes)
                .groupState(PdpState.ACTIVE).pdpState(PdpState.ACTIVE).build();
        return modelsProvider.getFilteredPdpGroups(pdpGroupFilter);
    }

    /**
     * Constructs the map of deployed pdp groups and deployed policies.
     *
     * @param pdpGroups the list of pdp groups that contain the specified policy
     * @param policyId the ID of policy
     * @param policyType the concept key of policy type
     * @param getter the custom generic getter BiFunction
     * @param consumer the BiConsumer
     * @param data the data structure storing retrieved deployed policies
     *
     * @return the constructed map of pdp groups and deployed policies
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private <T, R> Map<Pair<String, String>, T> constructDeployedPolicyMap(List<PdpGroup> pdpGroups, String policyId,
            PfConceptKey policyType, BiFunctionWithEx<String, String, R> getter, BiConsumer<T, R> consumer, T data)
                    throws PfModelException {

        Map<Pair<String, String>, T> deployedPolicyMap = new HashMap<>();
        for (PdpGroup pdpGroup : pdpGroups) {
            List<ToscaPolicyIdentifier> policyIdentifiers = extractPolicyIdentifiers(policyId, pdpGroup, policyType);
            T deployedPolicies = getDeployedPolicies(policyIdentifiers, policyType, getter, consumer, data);
            deployedPolicyMap.put(Pair.of(pdpGroup.getName(), pdpGroup.getVersion()), deployedPolicies);
        }
        return deployedPolicyMap;
    }

    /**
     * Extracts policy identifiers matching specified policy ID from specified pdp group.
     *
     * @param policyId the ID of policy to match
     * @param pdpGroup the target pdp group to search
     * @param policyType the concept key of policy type
     *
     * @return the list of policy identifiers
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private List<ToscaPolicyIdentifier> extractPolicyIdentifiers(String policyId, PdpGroup pdpGroup,
            PfConceptKey policyType) throws PfModelException {

        List<ToscaPolicyIdentifier> policyIdentifiers = new ArrayList<>();
        for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
            for (ToscaPolicyIdentifier policyIdentifier : pdpSubGroup.getPolicies()) {
                if (policyId.equalsIgnoreCase(policyIdentifier.getName())) {
                    policyIdentifiers.add(policyIdentifier);
                }
            }
        }
        if (policyIdentifiers.isEmpty()) {
            throw new PfModelException(Response.Status.NOT_FOUND,
                    constructDeploymentNotFoundMessage(policyType, policyId));
        }
        return policyIdentifiers;
    }

    /**
     * Retrieves deployed policies in a generic way.
     *
     * @param policyIdentifiers the identifiers of the policies to return
     * @param policyType the concept key of current policy type
     * @param getter the method reference of getting deployed policies
     * @param consumer the method reference of consuming the returned policies
     * @param data the data structure of deployed policies to return
     *
     * @return the generic type of policy data structure to return
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private <T, R> T getDeployedPolicies(List<ToscaPolicyIdentifier> policyIdentifiers, PfConceptKey policyType,
            BiFunctionWithEx<String, String, R> getter, BiConsumer<T, R> consumer, T data) throws PfModelException {

        for (ToscaPolicyIdentifier policyIdentifier : policyIdentifiers) {
            R result = getter.apply(policyIdentifier.getName(),
                    getTrimedVersionForLegacyType(policyIdentifier.getVersion(), policyType));
            consumer.accept(data, result);
        }
        return data;
    }

    /**
     * Trims the version for legacy policies.
     *
     * @param fullVersion the full version format with major, minor, patch
     * @param policyType the concept key of policy type
     *
     * @return the trimmed version
     */
    private String getTrimedVersionForLegacyType(String fullVersion, PfConceptKey policyType) {
        return (policyType.getName().contains("guard")
                || policyType.getName().contains("Operational")) ? fullVersion.split("\\.")[0] : fullVersion;
    }

    /**
     * Constructs returned message for not found policy deployment.
     *
     * @param policyType the concept key of policy type
     * @param policyId the ID of policy
     *
     * @return constructed message
     */
    private String constructDeploymentNotFoundMessage(PfConceptKey policyType, String policyId) {

        return "could not find policy with ID " + policyId + " and type "
                + policyType.getName() + ":" + policyType.getVersion() + " deployed in any pdp group";
    }
}
