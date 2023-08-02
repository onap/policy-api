/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

package org.onap.policy.api.main.rest.utils;

import static org.junit.Assert.assertTrue;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.onap.policy.common.endpoints.http.server.YamlMessageBodyHandler;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * Util class to perform REST unit tests.
 *
 */
public class CommonTestRestController {

    protected static final String APP_JSON = "application/json";
    protected static final String APP_YAML = "application/yaml";

    protected static final StandardCoder standardCoder = new StandardCoder();
    protected static StandardYamlCoder standardYamlCoder = new StandardYamlCoder();

    protected static final String HTTPS_PREFIX = "https://localhost:";
    protected static final String CONTEXT_PATH = "/policy/api/v1/";

    protected void testSwagger(final int apiPort) throws Exception {
        final Invocation.Builder invocationBuilder = sendHttpsRequest("v3/api-docs", APP_JSON, apiPort);
        final String resp = invocationBuilder.get(String.class);
        assertTrue((resp).contains("{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Policy Framework Lifecycle API\""));
    }

    protected Response createResource(String endpoint, String resourceName, int apiPort)
        throws Exception {

        ToscaServiceTemplate rawServiceTemplate = getRawServiceTemplate(resourceName);
        String mediaType = getMediaType(resourceName);
        mediaType = mediaType == null ?  APP_JSON : mediaType;

        final Invocation.Builder invocationBuilder;
        invocationBuilder = sendHttpsRequest(endpoint, mediaType, apiPort);
        Entity<ToscaServiceTemplate> entity = Entity.entity(rawServiceTemplate, mediaType);
        return invocationBuilder.post(entity);
    }

    protected Response readResource(String endpoint, String mediaType, int apiPort) throws Exception {

        final Invocation.Builder invocationBuilder;
        invocationBuilder = sendHttpsRequest(endpoint, mediaType, apiPort);
        return invocationBuilder.get();
    }

    protected Response deleteResource(String endpoint, String mediaType, int apiPort) throws Exception {

        final Invocation.Builder invocationBuilder;
        invocationBuilder = sendHttpsRequest(endpoint, mediaType, apiPort);
        return invocationBuilder.delete();
    }

    protected Response updateResource(String endpoint, String resourceName, String mediaType, int apiPort)
        throws Exception {

        ToscaServiceTemplate rawServiceTemplate = getRawServiceTemplate(resourceName);

        final Invocation.Builder invocationBuilder;
        invocationBuilder = sendHttpsRequest(endpoint, mediaType, apiPort);
        Entity<ToscaServiceTemplate> entity = Entity.entity(rawServiceTemplate, mediaType);
        return invocationBuilder.put(entity);
    }

    protected ToscaServiceTemplate decodeJson(String resourceName) throws CoderException {
        return standardCoder.decode(ResourceUtils.getResourceAsString(resourceName), ToscaServiceTemplate.class);
    }

    protected ToscaServiceTemplate decodeYaml(String resourceName) throws CoderException {
        return standardYamlCoder.decode(ResourceUtils.getResourceAsString(resourceName), ToscaServiceTemplate.class);
    }

    protected Invocation.Builder sendHttpsRequest(
            final String endpoint, String mediaType, int apiPort) throws Exception {

        final TrustManager[] noopTrustManager = NetworkUtil.getAlwaysTrustingManager();

        final SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, noopTrustManager, new SecureRandom());
        final ClientBuilder clientBuilder =
            ClientBuilder.newBuilder().sslContext(sc).hostnameVerifier((host, session) -> true);
        final Client client = clientBuilder.build();
        final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("policyadmin", "zb!XztG34");
        client.register(feature);

        client.property(ClientProperties.METAINF_SERVICES_LOOKUP_DISABLE, "true");
        if (APP_JSON.equalsIgnoreCase(mediaType)) {
            client.register(GsonMessageBodyHandler.class);
        } else if (APP_YAML.equalsIgnoreCase(mediaType)) {
            client.register(YamlMessageBodyHandler.class);
        }

        final WebTarget webTarget = client.target(HTTPS_PREFIX + apiPort + CONTEXT_PATH + endpoint);

        final Invocation.Builder invocationBuilder = webTarget.request(mediaType);

        if (!NetworkUtil.isTcpPortOpen("localhost", apiPort, 60, 1000L)) {
            throw new IllegalStateException("cannot connect to port " + apiPort);
        }
        return invocationBuilder;
    }

    private ToscaServiceTemplate getRawServiceTemplate(String resourceName) throws CoderException {
        ToscaServiceTemplate rawServiceTemplate = new ToscaServiceTemplate();
        if (APP_JSON.equals(getMediaType(resourceName))) {
            rawServiceTemplate = decodeJson(resourceName);
        } else if (APP_YAML.equals(getMediaType(resourceName))) {
            rawServiceTemplate = decodeYaml(resourceName);
        }
        return  rawServiceTemplate;
    }

    private String getMediaType(String resourceName) {
        if (resourceName.endsWith(".json")) {
            return APP_JSON;
        } else if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
            return APP_YAML;
        }
        return null;
    }

}
