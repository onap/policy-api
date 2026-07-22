/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Bell Canada. All rights reserved.
 *  Modifications Copyright (C) 2026 OpenInfra Foundation Europe. All rights reserved.
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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

/**
 * Policy API Spring Boot application.
 *
 * <p>The TOSCA model classes are bound with Gson {@code @SerializedName} annotations that map
 * camelCase Java fields to the snake_case JSON keys the Policy API contract uses (for example
 * {@code topologyTemplate} &rarr; {@code topology_template}), so Gson must be the JSON message
 * converter. {@link JacksonAutoConfiguration} is excluded so that no {@code JsonMapper} bean
 * exists; the Boot-provided Jackson message converter is {@code @ConditionalOnBean} on that mapper
 * and therefore drops out, leaving the auto-configured Gson converter as the JSON converter
 * regardless of the {@code spring.http.converters.preferred-json-mapper} property.
 */
@SpringBootApplication(exclude = {JacksonAutoConfiguration.class})
@EntityScan(
    basePackages =  {"org.onap.policy.models.pdp.persistence.concepts", "org.onap.policy.models.tosca.simple.concepts"})
public class PolicyApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PolicyApiApplication.class, args);
    }
}
