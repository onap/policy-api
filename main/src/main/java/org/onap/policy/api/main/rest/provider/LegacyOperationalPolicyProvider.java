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

import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.provider.impl.DummyPolicyModelsProviderImpl;
import org.onap.policy.models.tosca.authorative.concepts.PlainToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

/**
 * Class to provide all kinds of legacy operational policy operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class LegacyOperationalPolicyProvider {

    private PolicyModelsProvider modelsProvider;

    /**
     * Default constructor.
     */
    public LegacyOperationalPolicyProvider() throws PfModelException {

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
     * Retrieves a list of operational policies matching specified ID and version.
     *
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the PlainToscaServiceTemplate object
     */
    public PlainToscaServiceTemplate fetchOperationalPolicies(String policyId, String policyVersion)
            throws PfModelException {
        // placeholder
        return new PlainToscaServiceTemplate();
    }

    /**
     * Creates a new operational policy.
     *
     * @param body the entity body of policy
     *
     * @return the PlainToscaServiceTemplate object
     */
    public PlainToscaServiceTemplate createOperationalPolicy(LegacyOperationalPolicy body) throws PfModelException {
        // placeholder
        return new PlainToscaServiceTemplate();
    }

    /**
     * Deletes the operational policies matching specified ID and version.
     *
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the PlainToscaServiceTemplate object
     */
    public PlainToscaServiceTemplate deleteOperationalPolicies(String policyId, String policyVersion)
            throws PfModelException {
        // placeholder
        return new PlainToscaServiceTemplate();
    }
}