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

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.provider.impl.DummyPolicyModelsProviderImpl;
import org.onap.policy.models.tosca.authorative.concepts.PlainToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.mapping.PlainToscaServiceTemplateMapper;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;

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

        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();
        // Use dummy provider tentatively to test dummy things
        // Will change to use real database version
        parameters.setImplementation(DummyPolicyModelsProviderImpl.class.getCanonicalName());
        parameters.setDatabaseUrl("jdbc:dummy");
        parameters.setPersistenceUnit("dummy");

        modelsProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);
        modelsProvider.init();
    }

    /**
     * Retrieves a list of policy types matching specified policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @return the PlainToscaServiceTemplate object
     * @throws PfModelException the PfModel parsing exception
     */
    public PlainToscaServiceTemplate fetchPolicyTypes(String policyTypeId, String policyTypeVersion)
            throws PfModelException {

        ToscaServiceTemplate serviceTemplate = modelsProvider.getPolicyTypes(
                new PfConceptKey("dummyName", "dummyVersion"));
        return new PlainToscaServiceTemplateMapper().fromToscaServiceTemplate(serviceTemplate);
    }

    /**
     * Creates a new policy type.
     *
     * @param body the entity body of policy type
     *
     * @return the PlainToscaServiceTemplate object
     * @throws PfModelException the PfModel parsing exception
     */
    public PlainToscaServiceTemplate createPolicyType(PlainToscaServiceTemplate body) throws PfModelException {

        PlainToscaServiceTemplateMapper mapper = new PlainToscaServiceTemplateMapper();
        ToscaServiceTemplate mappedBody = mapper.toToscaServiceTemplate(body);

        ToscaServiceTemplate serviceTemplate = modelsProvider.createPolicyTypes(mappedBody);
        return mapper.fromToscaServiceTemplate(serviceTemplate);
    }

    /**
     * Delete the policy types matching specified policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @return the PlainToscaServiceTemplate object
     * @throws PfModelException the PfModel parsing exception
     */
    public PlainToscaServiceTemplate deletePolicyTypes(String policyTypeId, String policyTypeVersion)
            throws PfModelException {
        // placeholder
        return new PlainToscaServiceTemplate();
    }
}
