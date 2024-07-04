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

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.policy.api.main.repository.ToscaServiceTemplateRepository;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;

/**
 * This class offers common mock utility methods for uni testing {@link ToscaServiceTemplateService}.
 */
public class TestCommonToscaServiceTemplateService {

    protected enum Operation {
        CREATE_POLICY_TYPE,
        DELETE_POLICY_TYPE,
        CREATE_POLICY,
        DELETE_POLICY;
    }

    @Mock
    protected ToscaServiceTemplateRepository toscaServiceTemplateRepository;
    @Mock
    protected PolicyTypeService policyTypeService;
    @Mock
    protected PolicyService policyService;
    @Mock
    protected NodeTemplateService nodeTemplateService;

    AutoCloseable autoCloseable;

    /**
     * Set up the DB TOSCA service template object post create, and delete request.
     *
     * @param dbSvcTemplate       ToscaServiceTemplate object to update
     * @param svcTemplateFragment the CRUD operation response ToscaServiceTemplate object
     * @param operation           the CRUD operation performed
     */
    protected void mockDbServiceTemplate(ToscaServiceTemplate dbSvcTemplate, ToscaServiceTemplate svcTemplateFragment,
                                         TestToscaServiceTemplateServiceForPolicyCrud.Operation operation) {
        if (operation != null) {
            switch (operation) {
                case CREATE_POLICY_TYPE -> {
                    dbSvcTemplate.getPolicyTypes().putAll(svcTemplateFragment.getPolicyTypes());
                    if (svcTemplateFragment.getDataTypes() != null) {
                        if (dbSvcTemplate.getDataTypes() == null) {
                            dbSvcTemplate.setDataTypes(svcTemplateFragment.getDataTypes());
                        } else {
                            dbSvcTemplate.getDataTypes().putAll(svcTemplateFragment.getDataTypes());
                        }
                    }
                }
                case DELETE_POLICY_TYPE ->
                    dbSvcTemplate.getPolicyTypes().keySet().removeAll(svcTemplateFragment.getPolicyTypes().keySet());
                case CREATE_POLICY ->
                    dbSvcTemplate.setToscaTopologyTemplate(svcTemplateFragment.getToscaTopologyTemplate());
                case DELETE_POLICY -> dbSvcTemplate.getToscaTopologyTemplate().setPolicies(null);
                default -> Mockito.doNothing();
            }
        }
        Mockito.when(toscaServiceTemplateRepository.findById(new PfConceptKey(JpaToscaServiceTemplate.DEFAULT_NAME,
                JpaToscaServiceTemplate.DEFAULT_VERSION)))
            .thenReturn(Optional.of(new JpaToscaServiceTemplate(dbSvcTemplate)));
    }

    /**
     * Setup to return empty DB service template.
     */
    @BeforeEach
    public void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        Mockito.when(toscaServiceTemplateRepository.findById(new PfConceptKey(JpaToscaServiceTemplate.DEFAULT_NAME,
            JpaToscaServiceTemplate.DEFAULT_VERSION))).thenReturn(Optional.of(new JpaToscaServiceTemplate()));
    }

    @AfterEach
    public void tearDown() throws Exception {
        autoCloseable.close();
    }
}