/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.api.main;

import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.yaml.snakeyaml.Yaml;

/**
 * Utility class to support API tests.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public final class ApiTestSupportUtilities {
    private ApiTestSupportUtilities() {
        // prevent instantiation of this class
    }

    /**
     * Convert a YAML string to a JSON string.
     *
     * @param yamlString the Yaml string.
     * @return the JSON string
     * @throws CoderException on encoding JSON errors
     */
    public static final String yaml2Json(final String yamlString) throws CoderException {
        Object yamlObject = new Yaml().load(yamlString);
        return new StandardCoder().encode(yamlObject);
    }
}
