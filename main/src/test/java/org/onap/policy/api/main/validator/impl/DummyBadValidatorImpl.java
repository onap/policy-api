/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.api.main.validator.impl;

import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.onap.policy.api.main.validator.PolicyValidator;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * Dummy implementation of {@link PolicyValidator} with bad constructor.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 *
 */
public class DummyBadValidatorImpl implements PolicyValidator {

    public DummyBadValidatorImpl() {
        throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, "Bad Request");
    }

    @Override
    public void validatePoliciesAgainstPolicyTypes(List<Map<String, ToscaPolicy>> policies) throws PfModelException {
        // do nothing
    }

    @Override
    public void validateNativePolicies(List<Map<String, ToscaPolicy>> policies) throws PfModelException {
        // do nothing
    }

    @Override
    public void validateDroolsPolicies(List<Map<String, ToscaPolicy>> droolsPolicies) throws PfModelException {
        // do nothing
    }

    @Override
    public void validateXacmlPolicies(List<Map<String, ToscaPolicy>> xacmlPolicies) throws PfModelException {
        // do nothing
    }

    @Override
    public void validateApexPolicies(List<Map<String, ToscaPolicy>> apexPolicies) throws PfModelException {
        // do nothing
    }
}
