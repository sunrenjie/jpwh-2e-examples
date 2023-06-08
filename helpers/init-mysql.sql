DROP DATABASE IF EXISTS jpwh_2e_examples;
CREATE DATABASE IF NOT EXISTS jpwh_2e_examples;
DROP USER IF EXISTS jpwh_2e_examples;
CREATE USER IF NOT EXISTS 'jpwh_2e_examples'@'localhost' IDENTIFIED BY 'MyNewPass4!';
GRANT ALL PRIVILEGES ON jpwh_2e_examples.* TO 'jpwh_2e_examples'@'localhost';
