FROM onap/policy-common-alpine

LABEL maintainer="Policy Team"

ARG POLICY_LOGS=/var/log/onap/policy/api

ENV POLICY_LOGS=$POLICY_LOGS
ENV POLICY_HOME=$POLICY_HOME/api

RUN mkdir -p $POLICY_LOGS $POLICY_HOME $POLICY_HOME/bin && \
    chown -R policy:policy $POLICY_HOME $POLICY_LOGS

RUN mkdir /packages
COPY /maven/* /packages
RUN tar xvfz /packages/policy-api.tar.gz --directory $POLICY_HOME && \
    rm /packages/policy-api.tar.gz

WORKDIR $POLICY_HOME
COPY policy-api.sh  bin/.

RUN chown -R policy:policy * && chmod 755 bin/*.sh

USER policy
WORKDIR $POLICY_HOME/bin
ENTRYPOINT [ "bash", "./policy-api.sh" ]
