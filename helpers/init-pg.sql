DROP DATABASE IF EXISTS jpwh_2e_examples;
CREATE DATABASE jpwh_2e_examples;
DROP USER IF EXISTS jpwh_2e_examples;
CREATE USER jpwh_2e_examples WITH PASSWORD 'MyNewPass4!';
GRANT ALL PRIVILEGES ON DATABASE jpwh_2e_examples TO jpwh_2e_examples;
-- Needed for ordinary user to create tables, etc. PG 15; see also
-- https://github.com/FusionAuth/fusionauth-issues/issues/2015 and https://www.postgresql.org/docs/release/15.0/
GRANT ALL ON SCHEMA public TO jpwh_2e_examples;
