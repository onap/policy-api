FROM alpine:3.9

ARG HTTP_PROXY=${HTTP_PROXY}
ARG HTTPS_PROXY=${HTTPS_PROXY}
ARG BUILD_VERSION=${BUILD_VERSION}
ARG POLICY_LOGS=/var/log/onap/policy/api

ENV http_proxy $HTTP_PROXY
ENV https_proxy $HTTPS_PROXY
ENV BUILD_VERSION ${BUILD_VERSION}
ENV POLICY_LOGS ${POLICY_LOGS}

ENV POLICY_HOME=/opt/app/policy
ENV POLICY_API_HOME=${POLICY_HOME}/api

RUN \
    apk add --no-cache --update busybox-extras bash nss procps coreutils findutils grep \
                                zip unzip curl wget openssh maven openjdk8 jq httpie py-pip

RUN addgroup -S policy && \
    adduser -S  --shell /bin/bash -G policy policy


RUN mkdir -p ${POLICY_API_HOME} ${POLICY_LOGS} ${POLICY_HOME}/etc/ssl ${POLICY_API_HOME}/bin && \
    chown  -R policy:policy ${POLICY_HOME} ${POLICY_API_HOME} ${POLICY_LOGS}

RUN mkdir /packages
COPY /maven/* /packages
RUN tar xvfz /packages/policy-api.tar.gz --directory ${POLICY_API_HOME} && \
    rm /packages/policy-api.tar.gz

WORKDIR ${POLICY_API_HOME}
COPY policy-api.sh  bin/.
RUN chown -R policy:policy * && chmod +x bin/*.sh && \
    cp ${POLICY_API_HOME}/etc/ssl/* ${POLICY_HOME}/etc/ssl && chown policy:policy ${POLICY_HOME}/etc/ssl/*

USER policy
WORKDIR ${POLICY_API_HOME}/bin
ENTRYPOINT [ "bash", "./policy-api.sh" ]
