/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2022-2024 Nordix Foundation.
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

import jakarta.ws.rs.core.Response;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import org.onap.policy.api.main.exception.PolicyApiRuntimeException;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.errors.concepts.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Super class from which REST controllers are derived.
 */
public class CommonRestController {

    protected static Semaphore mutex = new Semaphore(1);

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonRestController.class);

    protected static final String API_VERSION = "1.0.0";
    protected static final String VERSION_MINOR_NAME = "X-MinorVersion";
    protected static final String VERSION_PATCH_NAME = "X-PatchVersion";
    protected static final String VERSION_LATEST_NAME = "X-LatestVersion";
    public static final String REQUEST_ID_NAME = "X-ONAP-RequestID";
    protected static final String ERROR_MESSAGE_NO_POLICIES_FOUND = "No policies found";

    protected final Coder coder = new StandardCoder();

    protected <T> ResponseEntity<T> makeOkResponse(UUID requestId, T respEntity) {
        return makeResponse(requestId, respEntity, HttpStatus.OK.value());
    }

    protected <T> ResponseEntity<T> makeCreatedResponse(UUID requestId, T respEntity) {
        return makeResponse(requestId, respEntity, HttpStatus.CREATED.value());
    }

    protected <T> ResponseEntity<T> makeResponse(UUID requestId, T respEntity, int status) {
        return CommonRestController
            .addLoggingHeaders(addVersionControlHeaders(ResponseEntity.status(status)), requestId)
            .body(respEntity);
    }

    /**
     * Adds version headers to the response.
     *
     * @param respBuilder response builder
     * @return the response builder, with version headers
     */
    public static ResponseEntity.BodyBuilder addVersionControlHeaders(ResponseEntity.BodyBuilder respBuilder) {
        return respBuilder.header(VERSION_MINOR_NAME, "0").header(VERSION_PATCH_NAME, "0").header(VERSION_LATEST_NAME,
            API_VERSION);
    }

    /**
     * Adds logging headers to the response.
     *
     * @param respBuilder response builder
     * @return the response builder, with version logging
     */
    public static ResponseEntity.BodyBuilder addLoggingHeaders(ResponseEntity.BodyBuilder respBuilder, UUID requestId) {
        // Generate a random uuid if client does not embed requestId in rest request
        return respBuilder.header(REQUEST_ID_NAME,
            Objects.requireNonNullElseGet(requestId, UUID::randomUUID).toString());

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

    protected void lock() throws PolicyApiRuntimeException {
        try {
            mutex.acquire();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            final var errorResponse = new ErrorResponse();
            errorResponse.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR);
            errorResponse.setErrorMessage(exception.getMessage());
            throw new PolicyApiRuntimeException(exception.getMessage(), exception.getCause(), errorResponse, null);
        } finally {
            mutex.release();
        }
    }

    @ExceptionHandler(value = {PolicyApiRuntimeException.class})
    protected ResponseEntity<Object> handleException(PolicyApiRuntimeException ex, WebRequest req) {
        LOGGER.warn(ex.getMessage(), ex.getCause());
        final var requestId = req.getHeader(CommonRestController.REQUEST_ID_NAME);
        final var status = ex.getErrorResponse().getResponseCode().getStatusCode();
        return CommonRestController.addLoggingHeaders(
            CommonRestController.addVersionControlHeaders(ResponseEntity.status(status)),
            requestId != null ? UUID.fromString(requestId) : ex.getRequestId()).body(ex.getErrorResponse());
    }
}
