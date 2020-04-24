FROM jboss/keycloak

ENV KEYCLOAK_ADMIN_USER=admin
ENV KEYCLOAK_ADMIN_PASSWORD=admin

RUN /opt/jboss/keycloak/bin/add-user-keycloak.sh -u ${KEYCLOAK_ADMIN_USER} -p ${KEYCLOAK_ADMIN_PASSWORD}

RUN /opt/jboss/keycloak/bin/add-user-keycloak.sh -r "dina" -u bob -p test --roles user
RUN /opt/jboss/keycloak/bin/add-user-keycloak.sh -r "dina" -u jeff -p test

ADD README.* dina-realm.* /opt/jboss/keycloak/
ADD README.* local/keycloak/dina-realm.* /opt/jboss/keycloak/

EXPOSE 8080
ENTRYPOINT [ "/opt/jboss/tools/docker-entrypoint.sh" ]

CMD ["-b", "0.0.0.0", "-Dkeycloak.import=/opt/jboss/keycloak/dina-realm.json"]
