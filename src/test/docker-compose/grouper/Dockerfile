FROM unicon/grouper-demo:2.3.0

MAINTAINER jgasper@unicon.net

COPY conf/ /opt/grouper.apiBinary-2.3.0/conf/
COPY lib/* /opt/grouper.apiBinary-2.3.0/lib/custom/

COPY conf/* /opt/tomcat/webapps/grouper/WEB-INF/classes/
COPY webapp/* /opt/tomcat/webapps/grouper/WEB-INF/
COPY lib/* /opt/tomcat/webapps/grouper/WEB-INF/lib/

COPY seed-data/ /seed-data/

RUN set -x; \
    rm -rf /opt/tomcat/work/ \
    && cd /opt/grouper.apiBinary-2.3.0/; \
    (/usr/sbin/ns-slapd -D /etc/dirsrv/slapd-dir &); \
    (/usr/bin/mysqld_safe &); \
    while ! curl -s localhost:3306 > /dev/null; do echo waiting for mysql to start; sleep 3; done; \
    while ! curl -s ldap://localhost:389 > /dev/null; do echo waiting for ldap to start; sleep 3; done; \
    mysql grouper < /seed-data/externalUsers.sql \
    && mysql grouper < /seed-data/protectedGroupNames.sql \
    && bin/gsh /seed-data/provisioningTargetAttributes.gsh

EXPOSE 389 3306 8080

CMD ["run-grouper.sh"]
