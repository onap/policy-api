/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

import org.onap.policy.common.utils.time.CurrentTime;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyModelsProviderRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyModelsProviderRetriever.class);
    private static final String SEARCH_STRING_DEFAULT = "Connection refused";
    private static final int RETRY_PERIOD_SECONDS_DEFAULT = 30;

    /**
     * Retrieves a {@link PolicyModelsProvider} from {@link PolicyModelsProviderFactory}, retries on failure.
     *
     * @param factory the factory on which to create the {@link PolicyModelsProvider}
     * @param params the {@link PolicyModelsProviderParameters}
     * @throws PfModelException in case of errors.
     */
    public PolicyModelsProvider retrieve(
        PolicyModelsProviderFactory factory, PolicyModelsProviderParameters params, CurrentTime sleeper)
        throws PfModelException {

        // searchString and retryPeriodSeconds should be set by params when implemented
        String searchString = SEARCH_STRING_DEFAULT;
        int retryPeriodSeconds = RETRY_PERIOD_SECONDS_DEFAULT;

        for (; ; ) {
            PfModelException latestException;
            try {
                return factory.createPolicyModelsProvider(params);

            } catch (final PfModelException e) {
                latestException = e;
                Throwable t = e;
                do {
                    if (t.getMessage() != null && t.getMessage().contains(searchString)) {
                        final String message =
                                "Database connection failed. Connection will be retried after {} seconds";

                        // Log the original Exception
                        LOGGER.warn(message, retryPeriodSeconds, e);

                        // Continue to delay period
                        break;
                    }
                    t = t.getCause();
                } while (t != null);

                // Never found searchString
                if (t == null) {
                    throw e;
                }
            }

            try {
                sleeper.sleep(retryPeriodSeconds * 1000);
            } catch (InterruptedException e) {
                LOGGER.warn("Retry delay interrupted, database connection failed", e);
                Thread.currentThread().interrupt();

                // Something went wrong - give up
                throw latestException;
            }
        }
    }
}
