/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.policy.api.main.config;

import java.util.List;
import org.onap.policy.api.main.config.converter.StringToEnumConverter;
import org.onap.policy.common.spring.utils.YamlHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Register custom converters to Spring configuration.
 *
 * <p>Only the YAML converter is registered here; the JSON and other default converters are kept
 * via {@code registerDefaults()}. Gson (not Jackson) ends up as the JSON converter because
 * {@link org.onap.policy.api.main.PolicyApiApplication} excludes Jackson auto-configuration, so
 * that JSON (de)serialization honours the snake_case Policy API contract; see that class for
 * details.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToEnumConverter());
    }

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        var yamlConverter = new YamlHttpMessageConverter();
        yamlConverter.setSupportedMediaTypes(List.of(MediaType.parseMediaType("application/yaml")));

        builder.withYamlConverter(yamlConverter);
    }
}
