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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import javax.ws.rs.core.Response.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.utils.time.CurrentTime;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.impl.DummyPolicyModelsProviderImpl;

public class PolicyModelsProviderRetrieverTest {
    private static final String SEARCH_STRING = "Connection refused";
    private static final String SEARCH_STRING_BAD = "Connection not refused";
    private AutoCloseable closeable;

    @Mock
    private PolicyModelsProviderFactory mockFactory;
    @Mock
    private CurrentTime mockCurrentTime;

    @Before
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @After
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    public void testRetrieveEventuallyReturnsProvider() throws PfModelException, InterruptedException {

        DummyPolicyModelsProviderImpl dummyProvider = new DummyPolicyModelsProviderImpl(null);
        PolicyModelsProviderRetriever retriever = new PolicyModelsProviderRetriever();

        Mockito.when(mockFactory.createPolicyModelsProvider(null))
                .thenThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING))
                .thenThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING))
                .thenReturn(dummyProvider);
        Mockito.doNothing().when(mockCurrentTime).sleep(Mockito.anyLong());

        assertEquals(dummyProvider, retriever.retrieve(mockFactory, null, mockCurrentTime));
        Mockito.verify(mockFactory, Mockito.times(3)).createPolicyModelsProvider(null);
        Mockito.verify(mockCurrentTime, Mockito.times(2)).sleep(Mockito.anyLong());
    }

    @Test
    public void testRetrieveThrowsPfModelExceptionOnOtherException() throws PfModelException {
        DummyPolicyModelsProviderImpl dummyProvider = new DummyPolicyModelsProviderImpl(null);
        PolicyModelsProviderRetriever retriever = new PolicyModelsProviderRetriever();

        Mockito
                .when(mockFactory.createPolicyModelsProvider(null))
                .thenThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING_BAD))
                .thenReturn(dummyProvider);

        assertThrows(PfModelException.class, () -> {
            retriever.retrieve(mockFactory, null, null);
        });
        Mockito.verify(mockFactory, Mockito.times(1)).createPolicyModelsProvider(null);
    }

    @Test
    public void testRetrieveThrowsLatestPfModelExceptionOnInterruptedException()
        throws PfModelException, InterruptedException {
        DummyPolicyModelsProviderImpl dummyProvider = new DummyPolicyModelsProviderImpl(null);
        PolicyModelsProviderRetriever retriever = new PolicyModelsProviderRetriever();

        Mockito
                .when(mockFactory.createPolicyModelsProvider(null))
                .thenThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING + " 1"))
                .thenThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING + " 2"))
                .thenThrow(new PfModelException(Status.INTERNAL_SERVER_ERROR, SEARCH_STRING + " 3"))
                .thenReturn(dummyProvider);
        Mockito
                .doNothing()
                .doThrow(new InterruptedException())
                .when(mockCurrentTime).sleep(Mockito.anyLong());

        Exception e = assertThrows(PfModelException.class, () -> {
            retriever.retrieve(mockFactory, null, mockCurrentTime);
        });
        assertEquals(e.getMessage(), "Connection refused 2");
        Mockito.verify(mockFactory, Mockito.times(2)).createPolicyModelsProvider(null);
        Mockito.verify(mockCurrentTime, Mockito.times(2)).sleep(Mockito.anyLong());
    }
}
