FROM onap/policy-common-alpine

LABEL maintainer="Policy Team"

ARG POLICY_LOGS=/var/log/onap/policy/api

ENV POLICY_LOGS ${POLICY_LOGS}
ENV POLICY_API_HOME=${POLICY_HOME}/api

RUN mkdir -p ${POLICY_LOGS} ${POLICY_API_HOME} ${POLICY_API_HOME}/bin && \
    chown  -R policy:policy ${POLICY_API_HOME}

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
