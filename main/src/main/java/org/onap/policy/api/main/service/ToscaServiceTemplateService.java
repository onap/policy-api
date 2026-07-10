/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Bell Canada. All rights reserved.
 *  Modifications Copyright (C) 2022-2024 Nordix Foundation.
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

import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.policy.api.main.repository.DataTypeRepository;
import org.onap.policy.api.main.repository.NodeTemplateRepository;
import org.onap.policy.api.main.repository.NodeTypeRepository;
import org.onap.policy.api.main.repository.PolicyRepository;
import org.onap.policy.api.main.repository.PolicyTypeRepository;
import org.onap.policy.api.main.repository.ToscaServiceTemplateRepository;
import org.onap.policy.api.main.rest.PolicyFetchMode;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTypedEntityFilter;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaNodeTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaNodeTemplates;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaNodeTypes;
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
    private final PolicyRepository policyRepository;
    private final PolicyTypeRepository policyTypeRepository;
    private final DataTypeRepository dataTypeRepository;
    private final NodeTypeRepository nodeTypeRepository;
    private final NodeTemplateRepository nodeTemplateRepository;
    private final NodeTemplateService nodeTemplateService;
    private final PdpGroupService pdpGroupService;
    private final PolicyTypeService policyTypeService;
    private final PolicyService policyService;

    /**
     * Retrieves a list of policy types matching specified policy type name and version.
     *
     * @param policyTypeName    the name of policy type
     * @param policyTypeVersion the version of policy type
     * @return the ToscaServiceTemplate object
     */
    public ToscaServiceTemplate fetchPolicyTypes(final String policyTypeName, final String policyTypeVersion)
        throws PfModelException {
        final var policyTypeFilter =
            ToscaEntityFilter.<ToscaPolicyType>builder().name(policyTypeName).version(policyTypeVersion).build();
        return getFilteredPolicyTypes(policyTypeFilter);
    }

    /**
     * Retrieves a list of policy types with the latest versions.
     *
     * @param policyTypeName the name of policy type
     * @return the ToscaServiceTemplate object
     */
    public ToscaServiceTemplate fetchLatestPolicyTypes(final String policyTypeName) throws PfModelException {
        final var policyTypeFilter = ToscaEntityFilter.<ToscaPolicyType>builder()
            .name(policyTypeName).version(ToscaEntityFilter.LATEST_VERSION).build();
        return getFilteredPolicyTypes(policyTypeFilter);
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
        var dbServiceTemplateOpt = getDefaultJpaToscaServiceTemplateOpt();
        JpaToscaServiceTemplate serviceTemplateToWrite;
        serviceTemplateToWrite = dbServiceTemplateOpt.map(
            jpaToscaServiceTemplate -> ToscaServiceTemplateUtils.addFragment(jpaToscaServiceTemplate,
                incomingServiceTemplate)).orElse(incomingServiceTemplate);

        final var result = serviceTemplateToWrite.validate("service template");
        if (result.isValid()) {
            toscaServiceTemplateRepository.save(serviceTemplateToWrite);
            LOGGER.debug("<-createPolicyType: writtenServiceTemplate={}", serviceTemplateToWrite);
        } else {
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, result.getResult());
        }
        return body;
    }

    /**
     * Delete the policy type matching specified policy type name and version.
     *
     * @param policyTypeName    the name of policy type
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
     * @param policyTypeName    the name of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyName        the name of policy
     * @param policyVersion     the version of policy
     * @param mode              the fetch mode for policies
     * @return the ToscaServiceTemplate object with the policies found
     * @throws PfModelException on errors getting the policy
     */
    public ToscaServiceTemplate fetchPolicies(final String policyTypeName, final String policyTypeVersion,
                                              final String policyName, final String policyVersion,
                                              final PolicyFetchMode mode) throws PfModelException {
        return fetchPolicies(policyTypeName, policyTypeVersion, policyName, policyVersion, mode, false);
    }

    /**
     * Retrieves a list of policies matching specified name and version of both policy type and policy.
     *
     * @param policyTypeName    the name of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyName        the name of policy
     * @param policyVersion     the version of policy
     * @param mode              the fetch mode for policies
     * @param skipMetadata      when true and a single policy is addressed by an exact name and version, fetch it
     *                          directly by key instead of reconstructing it from the full service template; this
     *                          may omit topology-template-level metadata that is not needed to describe the policy
     * @return the ToscaServiceTemplate object with the policies found
     * @throws PfModelException on errors getting the policy
     */
    public ToscaServiceTemplate fetchPolicies(final String policyTypeName, final String policyTypeVersion,
                                              final String policyName, final String policyVersion,
                                              final PolicyFetchMode mode, final boolean skipMetadata)
        throws PfModelException {
        return getFilteredPolicies(policyTypeName, policyTypeVersion, policyName, policyVersion, mode, skipMetadata);
    }

    /**
     * Retrieves a list of policies with the latest versions that match specified policy type id and version.
     *
     * @param policyTypeName    the name of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyName        the name of the policy
     * @param mode              the fetch mode for policies
     * @return the ToscaServiceTemplate object with the policies found
     * @throws PfModelException on errors getting the policy
     */
    public ToscaServiceTemplate fetchLatestPolicies(final String policyTypeName, final String policyTypeVersion,
                                                    final String policyName, final PolicyFetchMode mode)
        throws PfModelException {
        return getFilteredPolicies(policyTypeName, policyTypeVersion, policyName, ToscaTypedEntityFilter.LATEST_VERSION,
            mode, false);
    }

    /**
     * Creates one or more new policies for the same policy type name and version.
     *
     * @param body the entity body of polic(ies)
     * @return the ToscaServiceTemplate object containing the policy types that were created
     * @throws PfModelRuntimeException on errors creating the policy
     */
    public ToscaServiceTemplate createPolicy(final ToscaServiceTemplate body) throws PfModelRuntimeException {
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
        var dbServiceTemplateOpt = getDefaultJpaToscaServiceTemplateOpt();
        JpaToscaServiceTemplate serviceTemplateToWrite;
        serviceTemplateToWrite = dbServiceTemplateOpt.map(
            jpaToscaServiceTemplate -> ToscaServiceTemplateUtils.addFragment(jpaToscaServiceTemplate,
                incomingServiceTemplate)).orElse(incomingServiceTemplate);

        final var result = serviceTemplateToWrite.validate("Policies CRUD service template.");
        if (!result.isValid()) {
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, result.getResult());
        }

        toscaServiceTemplateRepository.save(serviceTemplateToWrite);

        LOGGER.debug("<-appendServiceTemplateFragment: returnServiceTemplate={}", serviceTemplateToWrite);
        return body;
    }

    /**
     * Deletes the policy matching specified name and version of both policy type and policy.
     *
     * @param policyName    the name of policy
     * @param policyVersion the version of policy
     * @return the ToscaServiceTemplate object containing the policies that were deleted
     * @throws PfModelRuntimeException on errors deleting the policy
     */
    public ToscaServiceTemplate deletePolicy(final String policyName, final String policyVersion)
        throws PfModelRuntimeException {
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
     * @param policyTypeFilter the policy type filter containing name and version of the policy type
     * @return the TOSCA service template containing the specified version of the policy type
     * @throws PfModelException on errors getting the policy type
     */
    public ToscaServiceTemplate getFilteredPolicyTypes(final ToscaEntityFilter<ToscaPolicyType> policyTypeFilter)
        throws PfModelException {
        final var dbServiceTemplate = getDefaultJpaToscaServiceTemplate();
        LOGGER.debug("->getFilteredPolicyTypes: filter={}, serviceTemplate={}", policyTypeFilter, dbServiceTemplate);

        // validate that policyTypes exist in db
        if (!ToscaUtils.doPolicyTypesExist(dbServiceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policy types for filter " + policyTypeFilter + DO_NOT_EXIST_MSG);
        }

        var version = ToscaTypedEntityFilter.LATEST_VERSION
            .equals(policyTypeFilter.getVersion()) ? null : policyTypeFilter.getVersion();
        // fetch all polices and filter by policyType, policy name and version
        final var serviceTemplate = new SimpleToscaProvider()
            .getCascadedPolicyTypes(dbServiceTemplate, policyTypeFilter.getName(), version);
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
     * @param policyTypeName    the name of the policy type
     * @param policyTypeVersion the version of the policy type
     * @param policyName        the name of the policy
     * @param policyVersion     the version of the policy
     * @param mode              the fetch mode for policies
     * @param skipMetadata      when true and a single policy is addressed by an exact name and version, the policy is
     *                          fetched directly by key instead of from the full service template (see
     *                          {@link #getServiceTemplateForSinglePolicy(PfConceptKey)})
     * @return the TOSCA service template containing the specified version of the policy
     * @throws PfModelException on errors getting the policy
     */
    private ToscaServiceTemplate getFilteredPolicies(final String policyTypeName, final String policyTypeVersion,
                                                     final String policyName, final String policyVersion,
                                                     final PolicyFetchMode mode, final boolean skipMetadata)
        throws PfModelException {
        final var policyFilter = ToscaTypedEntityFilter.<ToscaPolicy>builder()
            .name(policyName).version(policyVersion).type(policyTypeName).typeVersion(policyTypeVersion).build();

        // Fast path (opt-in via skipMetadata): a single policy identified by an exact name and version can be
        // fetched by key from the flat policy table instead of loading and deep-copying the entire (single) service
        // template aggregate, which holds every policy type, data type and policy in the database. This keeps the
        // cost of a specific-policy read independent of the total number of stored policies. As the reduced template
        // is rebuilt from the flat tables, topology-template-level metadata (description, inputs) that is not needed
        // to describe the policy may be omitted, so this path is only taken when the caller opts in.
        final var dbServiceTemplate = skipMetadata && isExactPolicyKey(policyName, policyVersion)
            ? getServiceTemplateForSinglePolicy(new PfConceptKey(policyName, policyVersion))
            : getDefaultJpaToscaServiceTemplate();
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
     * A policy can be looked up directly by key only when both its name and an exact (non-latest) version are given.
     *
     * @param policyName    the requested policy name
     * @param policyVersion the requested policy version
     * @return true if the pair identifies exactly one policy that can be fetched by key
     */
    private boolean isExactPolicyKey(final String policyName, final String policyVersion) {
        return policyName != null && policyVersion != null
            && !ToscaTypedEntityFilter.LATEST_VERSION.equals(policyVersion);
    }

    /**
     * Assembles a service template that is equivalent to the default database template for the purpose of fetching a
     * single policy, but that contains only the requested policy in its topology template rather than every stored
     * policy. All the bounded entities that a cascaded policy fetch may reference (policy types, data types, node
     * types and node templates) are included so the downstream cascade produces exactly the same result as it would
     * against the full template. When the policy does not exist the topology template is left with an empty policy
     * map, so the existing downstream validation reports the same "not found" error as the full-template path.
     *
     * @param policyKey the key (name and version) of the policy to fetch
     * @return the reduced service template
     */
    private JpaToscaServiceTemplate getServiceTemplateForSinglePolicy(final PfConceptKey policyKey) {

        if (!toscaServiceTemplateRepository.existsById(
            new PfConceptKey(JpaToscaServiceTemplate.DEFAULT_NAME, JpaToscaServiceTemplate.DEFAULT_VERSION))) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, SERVICE_TEMPLATE_NOT_FOUND_MSG);
        }

        final var serviceTemplate = new JpaToscaServiceTemplate();

        final var policyTypes = policyTypeRepository.findAll();
        if (!policyTypes.isEmpty()) {
            serviceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());
            policyTypes.forEach(pt -> serviceTemplate.getPolicyTypes().getConceptMap().put(pt.getKey(), pt));
        }

        final var dataTypes = dataTypeRepository.findAll();
        if (!dataTypes.isEmpty()) {
            serviceTemplate.setDataTypes(new JpaToscaDataTypes());
            dataTypes.forEach(dt -> serviceTemplate.getDataTypes().getConceptMap().put(dt.getKey(), dt));
        }

        final var nodeTypes = nodeTypeRepository.findAll();
        if (!nodeTypes.isEmpty()) {
            serviceTemplate.setNodeTypes(new JpaToscaNodeTypes());
            nodeTypes.forEach(nt -> serviceTemplate.getNodeTypes().getConceptMap().put(nt.getKey(), nt));
        }

        final var topologyTemplate = new JpaToscaTopologyTemplate();
        topologyTemplate.setPolicies(new JpaToscaPolicies());
        policyRepository.findById(policyKey)
            .ifPresent(policy -> topologyTemplate.getPolicies().getConceptMap().put(policy.getKey(), policy));

        final var nodeTemplates = nodeTemplateRepository.findAll();
        if (!nodeTemplates.isEmpty()) {
            topologyTemplate.setNodeTemplates(new JpaToscaNodeTemplates());
            nodeTemplates.forEach(nt -> topologyTemplate.getNodeTemplates().getConceptMap().put(nt.getKey(), nt));
        }

        serviceTemplate.setTopologyTemplate(topologyTemplate);
        return serviceTemplate;
    }

    /**
     * Write a node template to the database.
     *
     * @param serviceTemplate the service template to be written
     * @return the service template created by this method
     * @throws PfModelException on errors writing the metadataSets
     */
    public ToscaServiceTemplate createToscaNodeTemplates(@NonNull final ToscaServiceTemplate serviceTemplate)
        throws PfModelException {

        LOGGER.debug("->write: tosca nodeTemplates={}", serviceTemplate);
        final var incomingServiceTemplate = new JpaToscaServiceTemplate(serviceTemplate);

        ToscaUtils.assertNodeTemplatesExist(incomingServiceTemplate);

        Optional<JpaToscaNodeTypes> nodeTypes = Optional.ofNullable(incomingServiceTemplate.getNodeTypes());
        for (JpaToscaNodeTemplate nodeTemplate : incomingServiceTemplate.getTopologyTemplate().getNodeTemplates()
            .getAll(null)) {
            // verify node types in the db if mismatch/empty entities in the template
            if (!(nodeTypes.isPresent() && nodeTypes.get().getKeys().contains(nodeTemplate.getType()))) {
                nodeTemplateService.verifyNodeTypeInDbTemplate(nodeTemplate);
            }
        }
        // append the incoming fragment to the DB TOSCA service template
        final var serviceTemplateToWrite =
            ToscaServiceTemplateUtils.addFragment(getDefaultJpaToscaServiceTemplate(), incomingServiceTemplate);

        final var result = serviceTemplateToWrite.validate("service template.");
        if (!result.isValid()) {
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, result.getResult());
        }
        toscaServiceTemplateRepository.save(serviceTemplateToWrite);
        LOGGER.debug("<-createdToscaNodeTemplates: writtenServiceTemplate={}", serviceTemplateToWrite);

        return serviceTemplate;
    }

    /**
     * Update tosca node template.
     *
     * @param serviceTemplate the service template containing the definitions of the node templates to be updated.
     * @return the TOSCA service template containing the node templates that were updated
     * @throws PfModelRuntimeException on errors updating node templates
     */
    public ToscaServiceTemplate updateToscaNodeTemplates(@NonNull final ToscaServiceTemplate serviceTemplate)
        throws PfModelException {
        LOGGER.debug("->updateToscaNodeTemplates: serviceTemplate={}", serviceTemplate);
        final var incomingServiceTemplate = new JpaToscaServiceTemplate(serviceTemplate);

        ToscaUtils.assertNodeTemplatesExist(incomingServiceTemplate);
        nodeTemplateService.updateToscaNodeTemplates(incomingServiceTemplate);

        LOGGER.debug("<-updatedToscaNodeTemplates: serviceTemplate={}", serviceTemplate);
        return incomingServiceTemplate.toAuthorative();
    }


    /**
     * Delete a tosca node template.
     *
     * @param name    the name of node template
     * @param version the version of node template
     * @return the TOSCA service template containing the node template that were deleted
     * @throws PfModelException on errors deleting node templates
     */
    public ToscaServiceTemplate deleteToscaNodeTemplate(@NonNull final String name, @NonNull final String version)
        throws PfModelException {
        LOGGER.debug("->deleteToscaNodeTemplate: name={}, version={}", name, version);

        JpaToscaServiceTemplate dbServiceTemplate = getDefaultJpaToscaServiceTemplate();
        final var nodeTemplateKey = new PfConceptKey(name, version);

        if (!ToscaUtils.doNodeTemplatesExist(dbServiceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "no node templates found");
        }
        JpaToscaNodeTemplate nodeTemplate4Deletion = dbServiceTemplate.getTopologyTemplate().getNodeTemplates()
            .get(new PfConceptKey(name, version));
        if (nodeTemplate4Deletion == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "node template " + name + ":" + version
                + NOT_FOUND);
        }
        //Verify if the node template is referenced in the metadata of created policies
        nodeTemplateService.assertNodeTemplateNotUsedInPolicy(name, version, dbServiceTemplate);

        dbServiceTemplate.getTopologyTemplate().getNodeTemplates().getConceptMap().remove(nodeTemplateKey);
        toscaServiceTemplateRepository.save(dbServiceTemplate);

        // remove the entry from the tosca node template table
        nodeTemplateService.deleteNodeTemplate(nodeTemplateKey);

        // prepare the return service template
        var deletedServiceTemplate = new JpaToscaServiceTemplate();
        deletedServiceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        deletedServiceTemplate.getTopologyTemplate().setNodeTemplates(new JpaToscaNodeTemplates());
        deletedServiceTemplate.getTopologyTemplate().getNodeTemplates().getConceptMap()
            .put(nodeTemplateKey, nodeTemplate4Deletion);

        LOGGER.debug("<-deleteToscaNodeTemplate: key={}, serviceTemplate={}", nodeTemplateKey, deletedServiceTemplate);
        return deletedServiceTemplate.toAuthorative();
    }


    /**
     * Get tosca node templates.
     *
     * @param name    the name of the node template to get, set to null to get all node templates
     * @param version the version of the node template to get, set to null to get all versions
     * @return the node templates with the specified key
     * @throws PfModelException on errors getting node templates
     */
    public List<ToscaNodeTemplate> fetchToscaNodeTemplates(final String name, final String version)
        throws PfModelException {
        LOGGER.debug("->getNodeTemplate: name={}, version={}", name, version);
        List<ToscaNodeTemplate> nodeTemplates = new ArrayList<>();

        var dbServiceTemplate = getDefaultJpaToscaServiceTemplate();
        //Return empty if no nodeTemplates present in db
        if (!ToscaUtils.doNodeTemplatesExist(dbServiceTemplate)) {
            return nodeTemplates;
        }
        var jpaNodeTemplates = new JpaToscaNodeTemplates(dbServiceTemplate.getTopologyTemplate().getNodeTemplates());

        //Filter specific nodeTemplates
        if (name != null && version != null) {
            var filterKey = new PfConceptKey(name, version);
            jpaNodeTemplates.getConceptMap().entrySet().removeIf(entity -> !entity.getKey().equals(filterKey));
        }
        jpaNodeTemplates.getConceptMap().forEach((key, value) -> nodeTemplates.add(value.toAuthorative()));
        LOGGER.debug("<-getNodeTemplateMetadataSet: name={}, version={}, nodeTemplates={}", name, version,
            nodeTemplates);

        return nodeTemplates;
    }


    /**
     * Get Service Template.
     *
     * @return the Service Template read from the database
     * @throws PfModelRuntimeException if service template not found in database.
     */
    public JpaToscaServiceTemplate getDefaultJpaToscaServiceTemplate() throws PfModelRuntimeException {
        final var defaultServiceTemplateOpt = getDefaultJpaToscaServiceTemplateOpt();
        if (defaultServiceTemplateOpt.isEmpty()) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, SERVICE_TEMPLATE_NOT_FOUND_MSG);
        }
        LOGGER.debug("<-getDefaultJpaToscaServiceTemplate: serviceTemplate={}", defaultServiceTemplateOpt.get());
        return defaultServiceTemplateOpt.get();
    }

    /**
     * Get Service Template Optional object.
     *
     * @return the Optional object for Service Template read from the database
     */
    private Optional<JpaToscaServiceTemplate> getDefaultJpaToscaServiceTemplateOpt() {
        return toscaServiceTemplateRepository
            .findById(new PfConceptKey(JpaToscaServiceTemplate.DEFAULT_NAME, JpaToscaServiceTemplate.DEFAULT_VERSION));
    }
}