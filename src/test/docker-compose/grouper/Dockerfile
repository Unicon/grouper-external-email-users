FROM unicon/grouper-demo:2.3.0

MAINTAINER jgasper@unicon.net

COPY grouper.apiBinary/ /opt/grouper.apiBinary-2.3.0/
COPY grouper.ui/ /opt/grouper.ui-2.3.0/
COPY grouper.ui/java/lib/* /opt/grouper.apiBinary-2.3.0/lib/custom/
COPY provisioningTargetAttributes.gsh /
COPY externalUsers.sql /
COPY protectedGroupNames.sql /

RUN set -x; \
    cd /opt/grouper.ui-2.3.0 \
    && /opt/apache-ant-1.9.5/bin/ant war \
    && cp dist/grouper.war /opt/apache-tomcat-6.0.44/webapps \
    && rm -rf /opt/apache-tomcat-6.0.44/webapps/grouper/ \
    && rm -rf /opt/apache-tomcat-6.0.44/work/ \
    && cd /opt/grouper.apiBinary-2.3.0/ \
    && service mysql start \
    && service slapd start \
    && mysql grouper < /externalUsers.sql \
    && mysql grouper < /protectedGroupNames.sql \
    && bin/gsh /provisioningTargetAttributes.gsh \
    && rm /provisioningTargetAttributes.gsh

EXPOSE 389 3306 8080

CMD ["run-grouper.sh"]
