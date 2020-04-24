FROM jboss/keycloak

ENV KEYCLOAK_ADMIN_USER=admin
ENV KEYCLOAK_ADMIN_PASWORD=admin

RUN /opt/jboss/keycloak/bin/add-user-keycloak.sh -u ${KEYCLOAK_ADMIN_USER} -p ${KEYCLOAK_ADMIN_PASWORD}

RUN /opt/jboss/keycloak/bin/add-user-keycloak.sh -r "dina" -u bob -p test --roles user
RUN /opt/jboss/keycloak/bin/add-user-keycloak.sh -r "dina" -u jeff -p test

ADD README.* dina-realm.* /opt/jboss/keycloak/
ADD README.* local/keycloak/dina-realm.* /opt/jboss/keycloak/

ENTRYPOINT [ "/opt/jboss/tools/docker-entrypoint.sh" ]

CMD ["-b", "0.0.0.0", "-Dkeycloak.import=/opt/jboss/keycloak/dina-realm.json"]
