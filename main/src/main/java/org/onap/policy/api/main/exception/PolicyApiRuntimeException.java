/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.api.main.exception;

import java.io.Serial;
import java.util.UUID;
import lombok.Getter;
import org.onap.policy.models.errors.concepts.ErrorResponse;

/**
 * This runtime exception will be called if a runtime error occurs when using policy api.
 */
@Getter
public class PolicyApiRuntimeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8507246953751956974L;

    private final UUID requestId;
    private final ErrorResponse errorResponse;

    /**
     * Instantiates a new policy api runtime exception with a message.
     *
     * @param message the message
     */
    public PolicyApiRuntimeException(final String message) {
        super(message);
        this.requestId = null;
        this.errorResponse = null;
    }

    /**
     * Instantiates a new policy api runtime exception with a message and a caused by exception.
     *
     * @param message the message
     * @param exp the exception that caused this exception to be thrown
     */
    public PolicyApiRuntimeException(final String message, final Exception exp) {
        super(message, exp);
        this.requestId = null;
        this.errorResponse = null;
    }

    /**
     * Instantiates a new policy api runtime exception with requestId, errorResponse object
     * along with message and a caused by exception.
     *
     * @param message the message
     * @param cause the exception that caused this exception to be thrown
     * @param requestId request identifier
     * @param errorResponse error response object
     */
    public PolicyApiRuntimeException(final String message, final Throwable cause,
                                     final ErrorResponse errorResponse, final UUID requestId) {
        super(message, cause);
        this.requestId = requestId;
        this.errorResponse = errorResponse;
    }
}