/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2021 Nordix Foundation.
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

package org.onap.policy.api.main.rest.provider;

import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Class to provide all kinds of policy type operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Service
public class PolicyTypeProvider extends CommonModelProvider {

    /**
     * Default constructor.
     */
    @Autowired
    public PolicyTypeProvider(PolicyModelsProvider modelsProvider) throws PfModelException {
        super(modelsProvider);
    }

    /**
     * Retrieves a list of policy types matching specified policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate fetchPolicyTypes(String policyTypeId, String policyTypeVersion)
            throws PfModelException {

        return getFilteredPolicyTypes(policyTypeId, policyTypeVersion);
    }

    /**
     * Retrieves a list of policy types with the latest versions.
     *
     * @param policyTypeId the ID of policy type
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate fetchLatestPolicyTypes(String policyTypeId) throws PfModelException {

        return getFilteredPolicyTypes(policyTypeId, ToscaEntityFilter.LATEST_VERSION);
    }

    /**
     * Creates a new policy type.
     *
     * @param body the entity body of policy type
     *
     * @return the ToscaServiceTemplate object
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate createPolicyType(ToscaServiceTemplate body) throws PfModelException {

        return modelsProvider.createPolicyTypes(body);
    }

    /**
     * Delete the policy type matching specified policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate deletePolicyType(String policyTypeId, String policyTypeVersion)
            throws PfModelException {

        return modelsProvider.deletePolicyType(policyTypeId, policyTypeVersion);
    }

    /**
     * Retrieves the specified version of the policy type.
     *
     * @param policyTypeName the name of the policy type
     * @param policyTypeVersion the version of the policy type
     *
     * @return the TOSCA service template containing the specified version of the policy type
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private ToscaServiceTemplate getFilteredPolicyTypes(String policyTypeName, String policyTypeVersion)
            throws PfModelException {

        ToscaEntityFilter<ToscaPolicyType> policyTypeFilter =
                ToscaEntityFilter.<ToscaPolicyType>builder().name(policyTypeName).version(policyTypeVersion).build();
        return modelsProvider.getFilteredPolicyTypes(policyTypeFilter);
    }
}