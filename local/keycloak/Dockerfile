FROM jboss/keycloak:9.0.3

USER root

ENV KEYCLOAK_ADMIN_USER=admin
ENV KEYCLOAK_ADMIN_PASSWORD=admin
ENV KEYCLOAK_IMPORT=/opt/jboss/keycloak/dina-realm.json
ENV jboss.http.port=8080

RUN mkdir /data
RUN /opt/jboss/keycloak/bin/add-user-keycloak.sh -u ${KEYCLOAK_ADMIN_USER} -p ${KEYCLOAK_ADMIN_PASSWORD}

ADD dina-realm.* /opt/jboss/keycloak/
ADD dina-keycloak-theme/ /opt/jboss/keycloak/themes/dina

EXPOSE 8080
ENTRYPOINT [ "/opt/jboss/tools/docker-entrypoint.sh" ]

USER 8000
