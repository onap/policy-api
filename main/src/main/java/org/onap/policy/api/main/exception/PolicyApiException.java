/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
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

/**
 * This exception will be called if an error occurs in policy api external service.
 */
public class PolicyApiException extends Exception {
    @Serial
    private static final long serialVersionUID = -8507246953751956974L;

    /**
     * Instantiates a new policy api exception with a message.
     *
     * @param message the message
     */
    public PolicyApiException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new policy api exception with a caused by exception.
     *
     * @param exp the exception that caused this exception to be thrown
     */
    public PolicyApiException(final Exception exp) {
        super(exp);
    }

    /**
     * Instantiates a new policy api exception with a message and a caused by exception.
     *
     * @param message the message
     * @param exp     the exception that caused this exception to be thrown
     */
    public PolicyApiException(final String message, final Exception exp) {
        super(message, exp);
    }
}
