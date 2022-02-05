/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
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

package org.onap.policy.api.main.rest;

import java.util.UUID;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/**
 * Super class from which REST controllers are derived.
 */
public class CommonRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonRestController.class);

    private final Coder coder = new StandardCoder();

    protected <T> ResponseEntity<T> makeOkResponse(UUID requestId, T respEntity) {
        HttpHeaders headers = new HttpHeaders();
        addVersionControlHeaders(headers);
        addLoggingHeaders(headers, requestId);
        return ResponseEntity.ok().headers(headers).body(respEntity);
    }

    protected <T> ResponseEntity<T> makeErrorResponse(UUID requestId, T respEntity, int status) {
        HttpHeaders headers = new HttpHeaders();
        addVersionControlHeaders(headers);
        addLoggingHeaders(headers, requestId);
        return ResponseEntity.status(status).headers(headers).body(respEntity);
    }

    private void addVersionControlHeaders(HttpHeaders headers) {
        headers.add("X-MinorVersion", "0");
        headers.add("X-PatchVersion", "0");
        headers.add("X-LatestVersion", "1.0.0");
    }

    private void addLoggingHeaders(HttpHeaders headers, UUID requestId) {
        if (requestId == null) {
            // Generate a random uuid if client does not embed requestId in rest request
            headers.add("X-ONAP-RequestID", UUID.randomUUID().toString());
        } else {
            headers.add("X-ONAP-RequestID", requestId.toString());
        }
    }

    /**
     * Converts an object to a JSON string.
     *
     * @param object object to convert
     * @return a JSON string representing the object
     */
    protected String toJson(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return coder.encode(object);

        } catch (CoderException e) {
            LOGGER.warn("cannot convert {} to JSON", object.getClass().getName(), e);
            return null;
        }
    }
}