/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2022 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2021, 2023, 2025 Nordix Foundation.
 * Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
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

package org.onap.policy.api.main.startstop;

import com.google.common.collect.Sets;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.onap.policy.api.main.config.PolicyPreloadConfig;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.service.ToscaServiceTemplateService;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntity;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * This class creates initial policy types in the database.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Component
@ConditionalOnProperty(value = "database.initialize", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ApiDatabaseInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDatabaseInitializer.class);
    private static final StandardYamlCoder coder = new StandardYamlCoder();

    private final ToscaServiceTemplateService toscaServiceTemplateService;
    private final PolicyPreloadConfig policyPreloadConfig;

    @PostConstruct
    public void loadData() throws PolicyApiException {
        initializeApiDatabase(policyPreloadConfig.getPolicyTypes(), policyPreloadConfig.getPolicies());
    }

    /**
     * Initializes database by preloading policy types and policies.
     *
     * @param policyTypes List of policy types to preload.
     * @param policies List of policies to preload.
     * @throws PolicyApiException in case of errors.
     */
    public void initializeApiDatabase(final List<String> policyTypes, final List<String> policies)
        throws PolicyApiException {
        try {
            if (alreadyExists()) {
                LOGGER.warn("DB already contains policy data - skipping preload");
                return;
            }

            var serviceTemplate = new ToscaServiceTemplate();
            serviceTemplate.setDataTypes(new LinkedHashMap<>());
            serviceTemplate.setPolicyTypes(new LinkedHashMap<>());
            serviceTemplate.setToscaDefinitionsVersion("tosca_simple_yaml_1_1_0");

            ToscaServiceTemplate createdPolicyTypes =
                preloadServiceTemplate(serviceTemplate, policyTypes, toscaServiceTemplateService::createPolicyType);
            preloadServiceTemplate(createdPolicyTypes, policies, toscaServiceTemplateService::createPolicies);
        } catch (final PolicyApiException | PfModelException | CoderException exp) {
            throw new PolicyApiException(exp);
        }
    }

    private boolean alreadyExists() throws PfModelException {
        try {
            ToscaServiceTemplate serviceTemplate = toscaServiceTemplateService
                .getFilteredPolicyTypes(ToscaEntityFilter.<ToscaPolicyType>builder().build());
            if (!serviceTemplate.getPolicyTypes().isEmpty()) {
                return true;
            }
        } catch (PfModelRuntimeException e) {
            LOGGER.trace("DB does not yet contain policy types", e);
        }
        return false;
    }

    private ToscaServiceTemplate preloadServiceTemplate(ToscaServiceTemplate serviceTemplate, List<String> entities,
            FunctionWithEx<ToscaServiceTemplate, ToscaServiceTemplate> getter)
            throws PolicyApiException, CoderException, PfModelException {

        var multiVersionTemplates = new ArrayList<ToscaServiceTemplate>();

        for (String entity : entities) {
            ToscaServiceTemplate singleEntity = deserializeServiceTemplate(entity);

            if (isMultiVersion(serviceTemplate.getPolicyTypes(), singleEntity.getPolicyTypes())) {
                // if this entity introduces a new policy version of an existing policy type,
                // process it on its own as continuing here will override the existing policy type
                // in a different version

                multiVersionTemplates.add(singleEntity);
                LOGGER.warn("Detected multi-versioned type: {}", entity);
                continue;
            }

            // Consolidate data types and policy types
            if (singleEntity.getDataTypes() != null) {
                serviceTemplate.getDataTypes().putAll(singleEntity.getDataTypes());
            }
            if (singleEntity.getPolicyTypes() != null) {
                serviceTemplate.getPolicyTypes().putAll(singleEntity.getPolicyTypes());
            }

            // Consolidate policies
            var topologyTemplate = singleEntity.getToscaTopologyTemplate();
            if (topologyTemplate != null && topologyTemplate.getPolicies() != null) {
                consolidatePolicies(serviceTemplate, topologyTemplate);
            }
        }
        // Preload the specified entities
        ToscaServiceTemplate createdServiceTemplate = getter.apply(serviceTemplate);
        LOGGER.debug("Created initial tosca service template in DB - {}", createdServiceTemplate);

        multiVersionTemplates
            .forEach(mvServiceTemplate -> {
                try {
                    LOGGER.info("Multi-versioned Service Template {}", mvServiceTemplate.getPolicyTypes().keySet());
                    getter.apply(mvServiceTemplate);
                } catch (PfModelException e) {
                    LOGGER.warn("ToscaServiceTemple cannot be preloaded: {}", mvServiceTemplate, e);
                }
            });
        return createdServiceTemplate;
    }

    /**
     * Validates the topology template to have policies and add them to the final service template.
     * @param serviceTemplate the service template to be sent to database
     * @param topologyTemplate the topology template containing the policies
     */
    private static void consolidatePolicies(ToscaServiceTemplate serviceTemplate,
                                  ToscaTopologyTemplate topologyTemplate) {
        if (serviceTemplate.getToscaTopologyTemplate() == null) {
            serviceTemplate.setToscaTopologyTemplate(new ToscaTopologyTemplate());
        }

        if (serviceTemplate.getToscaTopologyTemplate().getPolicies() == null) {
            serviceTemplate.getToscaTopologyTemplate().setPolicies(new LinkedList<>());
        }
        serviceTemplate.getToscaTopologyTemplate().getPolicies()
                .addAll(topologyTemplate.getPolicies());
    }

    private ToscaServiceTemplate deserializeServiceTemplate(String entity) throws PolicyApiException, CoderException {
        var entityAsStringYaml = ResourceUtils.getResourceAsString(entity);
        if (entityAsStringYaml == null) {
            throw new PolicyApiException("Preloaded entity cannot be found " + entity);
        }

        ToscaServiceTemplate singleEntity = coder.decode(entityAsStringYaml, ToscaServiceTemplate.class);
        if (singleEntity == null) {
            throw new PolicyApiException("Error deserializing entity from file: " + entity);
        }
        return singleEntity;
    }

    // This method is templated, so it can be used with other derivations of ToscaEntity in the future,
    // if multi-version are desired.

    protected <T extends ToscaEntity> boolean isMultiVersion(Map<String, T> aggEntity,
                                                             Map<String, T> singleEntity) {
        if (aggEntity == null || singleEntity == null) {
            return false;
        }

        // There is a multi-version entity if both key sets have the same
        // entity name but different version.

        return
            Sets.intersection(aggEntity.keySet(), singleEntity.keySet())
                .stream()
                .anyMatch(e -> !Objects.equals(aggEntity.get(e).getVersion(), singleEntity.get(e).getVersion()));
    }

    @FunctionalInterface
    protected interface FunctionWithEx<T, R> {
        R apply(T value) throws PfModelException;
    }
}