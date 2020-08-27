CREATE SCHEMA object_store;

GRANT USAGE ON SCHEMA object_store TO $spring_datasource_username;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA object_store TO $spring_datasource_username;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA object_store TO $spring_datasource_username;

alter default privileges in schema object_store grant SELECT, INSERT, UPDATE, DELETE on tables to $spring_datasource_username;
alter default privileges in schema object_store grant all on sequences to $spring_datasource_username;
