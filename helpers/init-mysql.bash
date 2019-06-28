s="
  DROP DATABASE IF EXISTS test;
  CREATE DATABASE test;
  CREATE USER IF NOT EXISTS 'test'@'localhost' IDENTIFIED BY 'b4.nvjad_7L-';
  GRANT ALL PRIVILEGES ON test.* TO 'test'@'localhost';
"
cat <<EOF
Please ensure that mysql client is configured to use the root password stored at ~/.my.cnf with the contents:
[client]
user = root
password = 'super-secret'
port = 3306
host = localhost

Will execute the following SQL excerpt:
$s
EOF

mysql -e "$s"
