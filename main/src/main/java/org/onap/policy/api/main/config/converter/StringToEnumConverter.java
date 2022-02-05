/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Bell Canada. All rights reserved.
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

package org.onap.policy.api.main.config.converter;

import org.onap.policy.api.main.rest.PolicyFetchMode;
import org.springframework.core.convert.converter.Converter;

/**
 * Custom converter to support lowercase request parameters for policy fetch mode enumeration.
 */
public class StringToEnumConverter implements Converter<String, PolicyFetchMode> {

    @Override
    public PolicyFetchMode convert(String source) {
        try {
            return PolicyFetchMode.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}