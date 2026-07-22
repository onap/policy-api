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

import com.google.gson.Gson;
import java.util.List;
import org.onap.policy.api.main.config.converter.StringToEnumConverter;
import org.onap.policy.common.spring.utils.YamlHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Register custom converters to Spring configuration.
 *
 * <p>The TOSCA model classes (e.g. {@code ToscaServiceTemplate}) are bound with Gson
 * {@code @SerializedName} annotations that map camelCase Java fields to the snake_case JSON keys the
 * Policy API contract uses (for example {@code topologyTemplate} &rarr; {@code topology_template}).
 * Jackson binds by field name and therefore silently drops those keys, so Gson MUST be the JSON
 * message converter for the service to honour its own request/response contract.
 *
 * <p>Relying on {@code spring.http.converters.preferred-json-mapper=gson} alone is not sufficient:
 * the property is easily lost (e.g. an externally mounted config still using the pre-Spring-Boot-4
 * {@code spring.mvc.converters} name is silently ignored), and with both Gson and Jackson on the
 * classpath Boot then falls back to Jackson. The Gson converter is therefore registered explicitly
 * here so that JSON (de)serialization is deterministic regardless of property configuration.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Gson gson;

    public WebConfig(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToEnumConverter());
    }

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        var yamlConverter = new YamlHttpMessageConverter();
        yamlConverter.setSupportedMediaTypes(List.of(MediaType.parseMediaType("application/yaml")));

        builder.withJsonConverter(new GsonHttpMessageConverter(gson))
            .withYamlConverter(yamlConverter);
    }
}
