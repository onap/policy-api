/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates initial policy types in the database.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ApiDatabaseInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDatabaseInitializer.class);

    private static final StandardYamlCoder coder = new StandardYamlCoder();
    private PolicyModelsProviderFactory factory;

    /**
     * Constructs the object.
     */
    public ApiDatabaseInitializer() {
        factory = new PolicyModelsProviderFactory();
    }

    /**
     * Initializes database by preloading policy types and policies.
     *
     * @param apiParameterGroup the apiParameterGroup parameters
     * @throws PolicyApiException in case of errors.
     */
    public void initializeApiDatabase(final ApiParameterGroup apiParameterGroup) throws PolicyApiException {

        try (PolicyModelsProvider databaseProvider =
                factory.createPolicyModelsProviderWithRetry(apiParameterGroup.getDatabaseProviderParameters())) {

            if (alreadyExists(databaseProvider)) {
                LOGGER.warn("DB already contains policy data - skipping preload");
                return;
            }

            ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();
            serviceTemplate.setDataTypes(new LinkedHashMap<>());
            serviceTemplate.setPolicyTypes(new LinkedHashMap<>());
            serviceTemplate.setToscaDefinitionsVersion("tosca_simple_yaml_1_1_0");

            ToscaServiceTemplate createdPolicyTypes = preloadServiceTemplate(serviceTemplate,
                    apiParameterGroup.getPreloadPolicyTypes(), databaseProvider::createPolicyTypes);
            preloadServiceTemplate(createdPolicyTypes,
                    apiParameterGroup.getPreloadPolicies(), databaseProvider::createPolicies);
        } catch (final PolicyApiException | PfModelException | CoderException exp) {
            throw new PolicyApiException(exp);
        }
    }

    private boolean alreadyExists(PolicyModelsProvider databaseProvider) throws PfModelException {
        try {
            ToscaServiceTemplate serviceTemplate =
                            databaseProvider.getFilteredPolicyTypes(ToscaPolicyTypeFilter.builder().build());
            if (!serviceTemplate.getPolicyTypes().isEmpty()) {
                return true;
            }

        } catch (PfModelRuntimeException e) {
            LOGGER.trace("DB does not yet contain policy types", e);
        }

        return false;
    }

    private ToscaServiceTemplate preloadServiceTemplate(ToscaServiceTemplate serviceTemplate,
            List<String> entities, FunctionWithEx<ToscaServiceTemplate, ToscaServiceTemplate> getter)
                    throws PolicyApiException, CoderException, PfModelException {

        for (String entity : entities) {
            String entityAsStringYaml = ResourceUtils.getResourceAsString(entity);
            if (entityAsStringYaml == null) {
                LOGGER.warn("Preloading entity cannot be found: {}", entity);
                continue;
            }

            ToscaServiceTemplate singleEntity =
                    coder.decode(entityAsStringYaml,  ToscaServiceTemplate.class);
            if (singleEntity == null) {
                throw new PolicyApiException("Error deserializaing entity from file: " + entity);
            }

            // Consolidate data types and policy types
            if (singleEntity.getDataTypes() != null) {
                serviceTemplate.getDataTypes().putAll(singleEntity.getDataTypes());
            }
            if (singleEntity.getPolicyTypes() != null) {
                serviceTemplate.getPolicyTypes().putAll(singleEntity.getPolicyTypes());
            }

            // Consolidate policies
            ToscaTopologyTemplate topologyTemplate = singleEntity.getToscaTopologyTemplate();
            if (topologyTemplate != null && topologyTemplate.getPolicies() != null) {
                serviceTemplate.setToscaTopologyTemplate(new ToscaTopologyTemplate());
                serviceTemplate.getToscaTopologyTemplate().setPolicies(new LinkedList<>());
                serviceTemplate.getToscaTopologyTemplate().getPolicies()
                    .addAll(singleEntity.getToscaTopologyTemplate().getPolicies());
            }
        }
        // Preload the specified entities
        ToscaServiceTemplate createdServiceTemplate = getter.apply(serviceTemplate);
        LOGGER.debug("Created initial tosca service template in DB - {}", createdServiceTemplate);
        return createdServiceTemplate;
    }

    @FunctionalInterface
    protected interface FunctionWithEx<T, R> {
        public R apply(T value) throws PfModelException;
    }
}
