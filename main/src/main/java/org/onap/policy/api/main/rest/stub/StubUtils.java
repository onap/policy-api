/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023-2024 Nordix Foundation.
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

package org.onap.policy.api.main.rest.stub;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Profile("stub")
class StubUtils {
    private static final Logger log = LoggerFactory.getLogger(StubUtils.class);
    private static final String APPLICATION_JSON = "application/json";
    private static final String COULDNT_SERIALIZE_RESPONSE_ERROR =
        "Couldn't serialize response for content type application/json";
    private final HttpServletRequest request;
    private static final String ACCEPT = "Accept";
    private static final String TOSCA_NODE_TEMPLATE_RESOURCE =
            "nodetemplates/nodetemplates.metadatasets.input.tosca.json";
    private static final Gson JSON_TRANSLATOR = new Gson();

    <T> ResponseEntity<T> getCreateStubbedResponse(Class<T> clazz) {
        var accept = request.getHeader(ACCEPT);
        if (accept != null && accept.contains(APPLICATION_JSON)) {
            final var resource = new ClassPathResource(TOSCA_NODE_TEMPLATE_RESOURCE);
            try (var inputStream = resource.getInputStream()) {
                final var string = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                var targetObject = JSON_TRANSLATOR.fromJson(string, clazz);
                return new ResponseEntity<>(targetObject, HttpStatus.CREATED);
            } catch (IOException e) {
                log.error(COULDNT_SERIALIZE_RESPONSE_ERROR, e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    <T> ResponseEntity<T> getOkStubbedResponse(Class<T> clazz) {
        var accept = request.getHeader(ACCEPT);
        if (accept != null && accept.contains(APPLICATION_JSON)) {
            final var resource = new ClassPathResource(TOSCA_NODE_TEMPLATE_RESOURCE);
            try (var inputStream = resource.getInputStream()) {
                final var string = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                var targetObject = JSON_TRANSLATOR.fromJson(string, clazz);
                return new ResponseEntity<>(targetObject, HttpStatus.OK);
            } catch (IOException e) {
                log.error(COULDNT_SERIALIZE_RESPONSE_ERROR, e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    <T> ResponseEntity<List<T>> getStubbedResponseList(Class<T> clazz) {
        var accept = request.getHeader(ACCEPT);
        if (accept != null && accept.contains(APPLICATION_JSON)) {
            final var resource = new ClassPathResource(TOSCA_NODE_TEMPLATE_RESOURCE);
            try (var inputStream = resource.getInputStream()) {
                final var string = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                var targetObject = List.of(JSON_TRANSLATOR.fromJson(string, clazz));
                return new ResponseEntity<>(targetObject, HttpStatus.OK);
            } catch (IOException e) {
                log.error(COULDNT_SERIALIZE_RESPONSE_ERROR, e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
