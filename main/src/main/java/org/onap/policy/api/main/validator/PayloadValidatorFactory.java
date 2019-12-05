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

package org.onap.policy.api.main.validator;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.onap.policy.models.base.PfModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating PayloadValidator objects using the default Policy Framework implementation.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class PayloadValidatorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PayloadValidatorFactory.class);

    public PayloadValidator createPayloadValidator(@NonNull final PayloadValidatorParameters parameters)
            throws PfModelException {
        // Get the class for the PayloadValidator
        Class<?> implementationClass = null;
        try {
            implementationClass = Class.forName(parameters.getImplementation());
        } catch (final Exception exc) {
            String errorMsg = "could not find the implementation of \"PayloadValidator\" interface: \""
                    + parameters.getImplementation() + "\"";
            LOGGER.warn(errorMsg, exc);
            throw new PfModelException(Response.Status.NOT_FOUND, errorMsg, exc);
        }

        // Check if it is a PayloadValidator
        if (!PayloadValidator.class.isAssignableFrom(implementationClass)) {
            String errorMsg = "the class \"" + implementationClass.getName()
                + "\" is not an implementation of the \"PayloadValidator\" interface";
            LOGGER.warn(errorMsg);
            throw new PfModelException(Response.Status.BAD_REQUEST, errorMsg);
        }

        try {
            return (PayloadValidator) implementationClass.getConstructor(PayloadValidator.class)
                    .newInstance(parameters);
        } catch (Exception exc) {
            String errorMsg =
                    "could not create an instance of PayloadValidator \"" + parameters.getImplementation() + "\"";
            LOGGER.warn(errorMsg, exc);
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, errorMsg, exc);
        }
    }
}
