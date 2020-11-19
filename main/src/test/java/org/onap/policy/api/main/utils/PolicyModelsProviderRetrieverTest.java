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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.impl.DummyPolicyModelsProviderImpl;

public class PolicyModelsProviderRetrieverTest {
    private static final int RETRY_PERIOD_SECONDS_DEFAULT = 5;
    private AutoCloseable closeable;

    @Mock
    private PolicyModelsProviderFactory mockFactory;

    @Before
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @After
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    public void testRetrieveEventuallyReturnsProvider() throws PfModelException {

        DummyPolicyModelsProviderImpl dummyProvider = new DummyPolicyModelsProviderImpl(null);
        PolicyModelsProviderFactory factory = mockFactory;
        PolicyModelsProviderRetriever retriever = new PolicyModelsProviderRetriever();

        Mockito.when(mockFactory.createPolicyModelsProvider(null))
                .thenThrow(PfModelException.class)
                .thenThrow(PfModelException.class)
                .thenReturn(dummyProvider);

        assertEquals(dummyProvider, retriever.retrieve(factory, null, RETRY_PERIOD_SECONDS_DEFAULT));

        Mockito.verify(mockFactory, Mockito.times(3)).createPolicyModelsProvider(null);
    }
}
