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

package org.onap.policy.api.main.utils;

import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyModelsProviderRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyModelsProviderRetriever.class);

    /**
     * Retrieves a {@link PolicyModelsProvider} from {@link PolicyModelsProviderFactory}, retries on failure.
     *
     * @param factory the factory on which to create the {@link PolicyModelsProvider}
     * @param params the {@link PolicyModelsProviderParameters}
     * @param retryPeriodSeconds the number of seconds to pause before retrying
     * @throws PfModelException in case of errors.
     */
    public PolicyModelsProvider retrieve(
            PolicyModelsProviderFactory factory, PolicyModelsProviderParameters params, int retryPeriodSeconds)
            throws PfModelException {

        for (; ; ) {
            PfModelException latestException;
            try {
                return factory.createPolicyModelsProvider(params);

            } catch (final PfModelException e) {
                latestException = e;
                final String message =
                    "Database connection failed with message:"
                        + " {}. Connection will be retried after {} seconds";

                LOGGER.warn(message, e.getMessage(), retryPeriodSeconds);
                LOGGER.debug(message, e.getMessage(), retryPeriodSeconds, e);
            }

            try {
                Thread.sleep(retryPeriodSeconds * 1000);
            } catch (InterruptedException e) {
                LOGGER.warn("Retry delay interrupted, database connection failed", e);
                Thread.currentThread().interrupt();

                // Something went wrong
                throw latestException;
            }
        }
    }
}
