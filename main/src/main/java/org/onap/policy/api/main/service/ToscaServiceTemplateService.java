/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Bell Canada. All rights reserved.
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

package org.onap.policy.api.main.service;

import java.util.List;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.policy.api.main.repository.ToscaServiceTemplateRepository;
import org.onap.policy.api.main.rest.PolicyFetchMode;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTypedEntityFilter;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
import org.onap.policy.models.tosca.simple.provider.SimpleToscaProvider;
import org.onap.policy.models.tosca.utils.ToscaServiceTemplateUtils;
import org.onap.policy.models.tosca.utils.ToscaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ToscaServiceTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaServiceTemplateService.class);

    // Recurring string constants
    private static final String POLICY_TYPE = "policy type ";
    private static final String NOT_FOUND = " not found";
    public static final String SERVICE_TEMPLATE_NOT_FOUND_MSG = "service template not found in database";
    public static final String DO_NOT_EXIST_MSG = " do not exist";

    private final ToscaServiceTemplateRepository toscaServiceTemplateRepository;
    private final PdpGroupService pdpGroupService;
    private final PolicyTypeService policyTypeService;
    private final PolicyService policyService;

    /**
     * Retrieves a list of policy types matching specified policy type name and version.
     *
     * @param policyTypeName the name of policy type
     * @param policyTypeVersion the version of policy type
     * @return the ToscaServiceTemplate object
     */
    public ToscaServiceTemplate fetchPolicyTypes(final String policyTypeName, final String policyTypeVersion)
        throws PfModelException {
        return getFilteredPolicyTypes(policyTypeName, policyTypeVersion);
    }

    /**
     * Retrieves a list of policy types with the latest versions.
     *
     * @param policyTypeName the name of policy type
     * @return the ToscaServiceTemplate object
     */
    public ToscaServiceTemplate fetchLatestPolicyTypes(final String policyTypeName) throws PfModelException {
        return getFilteredPolicyTypes(policyTypeName, ToscaEntityFilter.LATEST_VERSION);
    }

    /**
     * Creates a new policy type.
     *
     * @param body the entity body of policy type
     * @return the TOSCA service template containing the created policy types
     * @throws PfModelRuntimeException on errors creating policy types
     */
    public ToscaServiceTemplate createPolicyType(@NonNull final ToscaServiceTemplate body)
        throws PfModelRuntimeException {
        final var incomingServiceTemplate = new JpaToscaServiceTemplate(body);
        LOGGER.debug("->createPolicyType: serviceTemplate={}", incomingServiceTemplate);

        // assert incoming body contains policyTypes
        ToscaUtils.assertPolicyTypesExist(incomingServiceTemplate);

        // append the incoming fragment to the DB TOSCA service template
        final var serviceTemplateToWrite =
            ToscaServiceTemplateUtils.addFragment(getDefaultJpaToscaServiceTemplate(), incomingServiceTemplate);

        final var result = serviceTemplateToWrite.validate("service template");
        if (!result.isValid()) {
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, result.getResult());
        } else {
            toscaServiceTemplateRepository.save(serviceTemplateToWrite);
            LOGGER.debug("<-createPolicyType: writtenServiceTemplate={}", serviceTemplateToWrite);
        }
        return body;
    }

    /**
     * Delete the policy type matching specified policy type name and version.
     *
     * @param policyTypeName the name of policy type
     * @param policyTypeVersion the version of policy type, if the version of the key is null,
     *                          all versions of the policy type are deleted.
     * @return the TOSCA service template containing the policy types that were deleted
     * @throws PfModelRuntimeException on errors deleting policy types
     */
    public ToscaServiceTemplate deletePolicyType(final String policyTypeName, final String policyTypeVersion)
        throws PfModelRuntimeException {
        final var policyTypeKey = new PfConceptKey(policyTypeName, policyTypeVersion);
        LOGGER.debug("->deletePolicyType: name={}, version={}", policyTypeName, policyTypeVersion);

        // terminate deletion if supported in a PdpGroup
        pdpGroupService.assertPolicyTypeNotSupportedInPdpGroup(policyTypeName, policyTypeVersion);

        final var serviceTemplate = getDefaultJpaToscaServiceTemplate();

        // terminate deletion if not found
        if (!ToscaUtils.doPolicyTypesExist(serviceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "no policy types found");
        }

        final var policyTypeForDeletion = serviceTemplate.getPolicyTypes().get(policyTypeKey);
        if (policyTypeForDeletion == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                POLICY_TYPE + policyTypeKey.getId() + NOT_FOUND);
        }

        final var result = new BeanValidationResult("policy types", serviceTemplate);

        for (final var policyType : serviceTemplate.getPolicyTypes().getAll(null)) {
            final var ancestorList = ToscaUtils
                .getEntityTypeAncestors(serviceTemplate.getPolicyTypes(), policyType, result);
            // terminate deletion if referenced by another via derived_from property
            if (ancestorList.contains(policyTypeForDeletion)) {
                throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, POLICY_TYPE + policyTypeKey.getId()
                    + " is in use, it is referenced in policy type " + policyType.getId());
            }
        }
        if (ToscaUtils.doPoliciesExist(serviceTemplate)) {
            for (final var policy : serviceTemplate.getTopologyTemplate().getPolicies().getAll(null)) {
                // terminate deletion if referenced by a policy
                if (policyTypeKey.equals(policy.getType())) {
                    throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, POLICY_TYPE
                        + policyTypeKey.getId() + " is in use, it is referenced in policy " + policy.getId());
                }
            }
        }

        // remove policyType from service template and write to DB
        serviceTemplate.getPolicyTypes().getConceptMap().remove(policyTypeKey);
        toscaServiceTemplateRepository.save(serviceTemplate);

        // remove the entry from the Policy table
        policyTypeService.deletePolicyType(policyTypeKey);

        // prepare return service template object
        var deletedServiceTemplate = new JpaToscaServiceTemplate();
        deletedServiceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());
        deletedServiceTemplate.getPolicyTypes().getConceptMap().put(policyTypeKey, policyTypeForDeletion);

        LOGGER.debug("<-deletePolicyType: key={}, serviceTemplate={}", policyTypeKey, deletedServiceTemplate);
        return deletedServiceTemplate.toAuthorative();
    }

    /**
     * Retrieves a list of policies matching specified name and version of both policy type and policy.
     *
     * @param policyTypeName the name of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyName the name of policy
     * @param policyVersion the version of policy
     * @param mode the fetch mode for policies
     * @return the ToscaServiceTemplate object with the policies found
     * @throws PfModelException on errors getting the policy
     */
    public ToscaServiceTemplate fetchPolicies(final String policyTypeName, final String policyTypeVersion,
        final String policyName, final String policyVersion, final PolicyFetchMode mode) throws PfModelException {
        return getFilteredPolicies(policyTypeName, policyTypeVersion, policyName, policyVersion, mode);
    }

    /**
     * Retrieves a list of policies with the latest versions that match specified policy type id and version.
     *
     * @param policyTypeName the name of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyName the name of the policy
     * @param mode the fetch mode for policies
     * @return the ToscaServiceTemplate object with the policies found
     * @throws PfModelException on errors getting the policy
     */
    public ToscaServiceTemplate fetchLatestPolicies(final String policyTypeName, final String policyTypeVersion,
        final String policyName, final PolicyFetchMode mode) throws PfModelException {
        return getFilteredPolicies(policyTypeName, policyTypeVersion, policyName, ToscaTypedEntityFilter.LATEST_VERSION,
            mode);
    }

    /**
     * Creates one or more new policies for the same policy type name and version.
     *
     * @param policyTypeName the name of policy type
     * @param policyTypeVersion the version of policy type
     * @param body the entity body of polic(ies)
     * @return the ToscaServiceTemplate object containing the policy types that were created
     * @throws PfModelRuntimeException on errors creating the policy
     */
    public ToscaServiceTemplate createPolicy(final String policyTypeName, final String policyTypeVersion,
        final ToscaServiceTemplate body) throws PfModelRuntimeException {
        return createPolicies(body);
    }

    /**
     * Creates one or more new policies.
     *
     * @param body the entity body of policy
     * @return the ToscaServiceTemplate object containing the policy types that were created
     * @throws PfModelRuntimeException on errors creating the policy
     */
    public ToscaServiceTemplate createPolicies(final ToscaServiceTemplate body) throws PfModelRuntimeException {
        final var incomingServiceTemplate = new JpaToscaServiceTemplate(body);

        // assert incoming body contains policies
        ToscaUtils.assertPoliciesExist(incomingServiceTemplate);

        // append the incoming fragment to the DB TOSCA service template
        final var serviceTemplateToWrite =
            ToscaServiceTemplateUtils.addFragment(getDefaultJpaToscaServiceTemplate(), incomingServiceTemplate);

        final var result = serviceTemplateToWrite.validate("Policies CRUD service template.");
        if (!result.isValid()) {
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, result.getResult());
        }

        toscaServiceTemplateRepository.save(serviceTemplateToWrite);

        LOGGER.debug("<-appendServiceTemplateFragment: returnServiceTempalate={}", serviceTemplateToWrite);
        return body;
    }

    /**
     * Deletes the policy matching specified name and version of both policy type and policy.
     *
     * @param policyTypeName the name of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyName the name of policy
     * @param policyVersion the version of policy
     * @return the ToscaServiceTemplate object containing the policies that were deleted
     * @throws PfModelRuntimeException on errors deleting the policy
     */
    public ToscaServiceTemplate deletePolicy(final String policyTypeName, final String policyTypeVersion,
        final String policyName, final String policyVersion) throws PfModelRuntimeException {
        final var policyKey = new PfConceptKey(policyName, policyVersion);
        LOGGER.debug("->deletePolicy: name={}, version={}", policyName, policyVersion);

        // terminate if deployed in a PdpGroup
        pdpGroupService.assertPolicyNotDeployedInPdpGroup(policyName, policyVersion);

        final var serviceTemplate = getDefaultJpaToscaServiceTemplate();

        // terminate deletion if not found
        if (!ToscaUtils.doPoliciesExist(serviceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "no policies found");
        }

        final var policyForDeletion = serviceTemplate.getTopologyTemplate().getPolicies().get(policyKey);
        if (policyForDeletion == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "policy " + policyKey.getId() + NOT_FOUND);
        }

        // remove policy from service template and write to DB
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().remove(policyKey);
        toscaServiceTemplateRepository.save(serviceTemplate);

        // remove the entry from the Policy table
        policyService.deletePolicy(policyKey);

        // prepare return service template object
        var deletedServiceTemplate = new JpaToscaServiceTemplate();
        deletedServiceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        deletedServiceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());
        deletedServiceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(policyKey, policyForDeletion);

        LOGGER.debug("<-deletePolicy: key={}, serviceTemplate={}", policyKey, deletedServiceTemplate);
        return deletedServiceTemplate.toAuthorative();
    }

    /**
     * Retrieves TOSCA service template with the specified version of the policy type.
     *
     * @param policyTypeName the name of the policy type
     * @param policyTypeVersion the version of the policy type
     * @return the TOSCA service template containing the specified version of the policy type
     * @throws PfModelException on errors getting the policy type
     */
    private ToscaServiceTemplate getFilteredPolicyTypes(final String policyTypeName, final String policyTypeVersion)
        throws PfModelException {
        final var dbServiceTemplate = getDefaultJpaToscaServiceTemplate();
        final var policyTypeFilter =
            ToscaEntityFilter.<ToscaPolicyType>builder().name(policyTypeName).version(policyTypeVersion).build();
        LOGGER.debug("->getFilteredPolicyTypes: filter={}, serviceTemplate={}", policyTypeFilter, dbServiceTemplate);

        // validate that policyTypes exist in db
        if (!ToscaUtils.doPolicyTypesExist(dbServiceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policy types for filter " + policyTypeFilter + DO_NOT_EXIST_MSG);
        }

        // fetch all polices and filter by policyType, policy name and version
        final var serviceTemplate = new SimpleToscaProvider()
            .getCascadedPolicyTypes(dbServiceTemplate, policyTypeName, policyTypeVersion);
        var simpleToscaProvider = new SimpleToscaProvider();

        List<ToscaPolicyType> filteredPolicyTypes = serviceTemplate.getPolicyTypes().toAuthorativeList();
        filteredPolicyTypes = policyTypeFilter.filter(filteredPolicyTypes);

        // validate that filtered policyTypes exist
        if (CollectionUtils.isEmpty(filteredPolicyTypes)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policy types for filter " + policyTypeFilter + DO_NOT_EXIST_MSG);
        }

        // prepare return service template object
        var returnServiceTemplate = new JpaToscaServiceTemplate();
        for (var policyType : filteredPolicyTypes) {
            final var cascadedServiceTemplate = simpleToscaProvider
                .getCascadedPolicyTypes(dbServiceTemplate, policyType.getName(), policyType.getVersion());
            returnServiceTemplate =
                ToscaServiceTemplateUtils.addFragment(returnServiceTemplate, cascadedServiceTemplate);
        }

        LOGGER.debug("<-getFilteredPolicyTypes: filter={}, serviceTemplate={}", policyTypeFilter,
            returnServiceTemplate);
        return returnServiceTemplate.toAuthorative();

    }

    /**
     * Retrieves TOSCA service template with the specified version of the policy.
     *
     * @param policyName the name of the policy
     * @param policyVersion the version of the policy
     * @param mode the fetch mode for policies
     * @return the TOSCA service template containing the specified version of the policy
     * @throws PfModelException on errors getting the policy
     */
    private ToscaServiceTemplate getFilteredPolicies(final String policyTypeName, final String policyTypeVersion,
        final String policyName, final String policyVersion, final PolicyFetchMode mode) throws PfModelException {
        final var policyFilter = ToscaTypedEntityFilter.<ToscaPolicy>builder()
            .name(policyName).version(policyVersion).type(policyTypeName).typeVersion(policyTypeVersion).build();
        final var dbServiceTemplate = getDefaultJpaToscaServiceTemplate();
        LOGGER.debug("<-getFilteredPolicies: filter={}, serviceTemplate={}", policyFilter, dbServiceTemplate);

        // validate that policies exist in db
        if (!ToscaUtils.doPolicyTypesExist(dbServiceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policies for filter " + policyFilter + DO_NOT_EXIST_MSG);
        }

        final var version =
            ToscaTypedEntityFilter.LATEST_VERSION.equals(policyFilter.getVersion()) ? null : policyFilter.getVersion();

        // fetch all polices and filter by policyType, policy name and version
        final var simpleToscaProvider = new SimpleToscaProvider();
        final var serviceTemplate =
            simpleToscaProvider.getCascadedPolicies(dbServiceTemplate, policyFilter.getName(), version);

        var filteredPolicies = serviceTemplate.getTopologyTemplate()
            .getPolicies().toAuthorativeList();
        filteredPolicies = policyFilter.filter(filteredPolicies);

        // validate that filtered policies exist
        if (CollectionUtils.isEmpty(filteredPolicies)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policies for filter " + policyFilter + DO_NOT_EXIST_MSG);
        }

        // prepare return service template object
        var returnServiceTemplate = new JpaToscaServiceTemplate();
        for (var policy : filteredPolicies) {
            final var cascadedServiceTemplate = simpleToscaProvider
                .getCascadedPolicies(dbServiceTemplate, policy.getName(), policy.getVersion());
            returnServiceTemplate =
                ToscaServiceTemplateUtils.addFragment(returnServiceTemplate, cascadedServiceTemplate);
        }

        if (mode == null || PolicyFetchMode.BARE.equals(mode)) {
            returnServiceTemplate.setPolicyTypes(null);
            returnServiceTemplate.setDataTypes(null);
        }
        LOGGER.debug("<-getFilteredPolicies: filter={}, , serviceTemplate={}", policyFilter, returnServiceTemplate);
        return returnServiceTemplate.toAuthorative();
    }

    /**
     * Get Service Template.
     * @return the Service Template read from the database
     */
    private JpaToscaServiceTemplate getDefaultJpaToscaServiceTemplate() {
        final var defaultServiceTemplateOpt = toscaServiceTemplateRepository
            .findById(new PfConceptKey(JpaToscaServiceTemplate.DEFAULT_NAME, JpaToscaServiceTemplate.DEFAULT_VERSION));
        if (defaultServiceTemplateOpt.isEmpty()) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, SERVICE_TEMPLATE_NOT_FOUND_MSG);
        }
        LOGGER.debug("<-getDefaultJpaToscaServiceTemplate: serviceTemplate={}", defaultServiceTemplateOpt.get());
        return defaultServiceTemplateOpt.get();
    }
}