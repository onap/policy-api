#!/bin/bash
# ============LICENSE_START=======================================================
#  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

# the directory of the script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo ${DIR}
docker run -p 9090:9090 -p 6969:6969 -v ${DIR}/config/api/bin/policy-api.sh:/opt/app/policy/api/bin/policy-api.sh -v ${DIR}/config/api/etc/defaultConfig.json:/opt/app/policy/api/etc/defaultConfig.json --add-host mariadb:10.12.6.83 --name policy-api -d --rm nexus3.onap.org:10001/onap/policy-api:2.1.1-SNAPSHOT