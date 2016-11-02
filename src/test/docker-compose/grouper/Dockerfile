FROM unicon/grouper-demo:2.3.0

MAINTAINER jgasper@unicon.net

COPY conf/ /opt/grouper.apiBinary-2.3.0/conf/
COPY lib/* /opt/grouper.apiBinary-2.3.0/lib/custom/

COPY conf/* /opt/apache-tomcat-6.0.44/webapps/grouper/WEB-INF/classes/
COPY webapp/* /opt/apache-tomcat-6.0.44/webapps/grouper/WEB-INF/
COPY lib/* /opt/apache-tomcat-6.0.44/webapps/grouper/WEB-INF/lib/

COPY seed-data/ /seed-data/

RUN set -x; \
    rm -rf /opt/apache-tomcat-6.0.44/work/ \
    && cd /opt/grouper.apiBinary-2.3.0/ \
    && service mysql start \
    && service slapd start \
    && mysql grouper < /seed-data/externalUsers.sql \
    && mysql grouper < /seed-data/protectedGroupNames.sql \
    && bin/gsh /seed-data/provisioningTargetAttributes.gsh \
    && rm /*.gsh /*.sql

EXPOSE 389 3306 8080

CMD ["run-grouper.sh"]
