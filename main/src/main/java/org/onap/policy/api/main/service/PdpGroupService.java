/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Bell Canada. All rights reserved.
 *  Modifications Copyright (C) 2023-2024 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================;
 */

package org.onap.policy.api.main.service;

import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.onap.policy.api.main.repository.PdpGroupRepository;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdpGroup;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PdpGroupService {

    private final PdpGroupRepository pdpGroupRepository;

    /**
     * Fetch all the PDP groups from the DB.
     *
     * @return a list of {@link PdpGroup}
     */
    private List<PdpGroup> getAllPdpGroups() {
        return pdpGroupRepository.findAll().stream().map(JpaPdpGroup::toAuthorative).toList();
    }

    /**
     * Assert that the policy type is not supported in any PDP group.
     *
     * @param policyTypeName    the policy type name
     * @param policyTypeVersion the policy type version
     * @throws PfModelRuntimeException if the policy type is supported in a PDP group
     */
    public void assertPolicyTypeNotSupportedInPdpGroup(final String policyTypeName, final String policyTypeVersion)
        throws PfModelRuntimeException {
        final var policyTypeIdentifier = new ToscaConceptIdentifier(policyTypeName, policyTypeVersion);
        for (PdpGroup pdpGroup : getAllPdpGroups()) {
            for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
                if (pdpSubGroup.getSupportedPolicyTypes().contains(policyTypeIdentifier)) {
                    throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE,
                        "policy type is in use, it is referenced in PDP group " + pdpGroup.getName() + " subgroup "
                            + pdpSubGroup.getPdpType());
                }
            }
        }
    }

    /**
     * Assert that the policy is not deployed in a PDP group.
     *
     * @param policyName    the policy name
     * @param policyVersion the policy version
     * @throws PfModelRuntimeException thrown if the policy is deployed in a PDP group
     */
    public void assertPolicyNotDeployedInPdpGroup(final String policyName, final String policyVersion)
        throws PfModelRuntimeException {
        final var policyIdentifier = new ToscaConceptIdentifier(policyName, policyVersion);
        for (PdpGroup pdpGroup : getAllPdpGroups()) {
            for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
                if (pdpSubGroup.getPolicies().contains(policyIdentifier)) {
                    throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE,
                        "policy is in use, it is deployed in PDP group " + pdpGroup.getName() + " subgroup "
                            + pdpSubGroup.getPdpType());
                }
            }
        }
    }
}