#!/usr/bin/env sh
#
# ============LICENSE_START=======================================================
#  Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
#  Modifications Copyright (C) 2019-2022, 2025 OpenInfra Foundation Europe. All rights reserved.
#  Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=========================================================
#

if [ "$#" -eq 1 ]; then
    CONFIG_FILE=$1
else
    # shellcheck disable=SC2269
    CONFIG_FILE=${CONFIG_FILE}
fi

if [ -z "$CONFIG_FILE" ]; then
    CONFIG_FILE="${POLICY_HOME}/etc/apiParameters.yaml"
fi

echo "Policy api config file: $CONFIG_FILE"

if [ -f "${POLICY_HOME}/etc/mounted/logback.xml" ]; then
    echo "overriding logback.xml"
    cp -f "${POLICY_HOME}"/etc/mounted/logback.xml "${POLICY_HOME}"/etc/
fi

"$JAVA_HOME"/bin/java \
    -Dlogging.config="${POLICY_HOME}/etc/logback.xml" \
    -jar /app/api.jar \
    --spring.config.location="${CONFIG_FILE}"
