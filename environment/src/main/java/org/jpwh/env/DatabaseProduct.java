package org.jpwh.env;

import bitronix.tm.resource.jdbc.PoolingDataSource;

import java.util.Properties;

public enum DatabaseProduct {

    H2(
        new DataSourceConfiguration() {
            @Override
            public void configure(PoolingDataSource ds, String connectionURL) {
                ds.setClassName("org.h2.jdbcx.JdbcDataSource");

                // External instance: jdbc:h2:tcp://localhost/mem:test;USER=sa
                ds.getDriverProperties().put(
                    "URL",
                    connectionURL != null
                        ? connectionURL :
                        "jdbc:h2:mem:test"
                );

                // TODO: http://code.google.com/p/h2database/issues/detail?id=502
                ds.getDriverProperties().put("user", "sa");

                // TODO: Don't trace log values larger than X bytes (especially useful for
                // debugging LOBs, which are accessed in toString()!)
                // System.setProperty("h2.maxTraceDataLength", "256"); 256 bytes, default is 64 kilobytes
            }
        },
        org.jpwh.shared.ImprovedH2Dialect.class.getName()
    ),

    ORACLE(
        new DataSourceConfiguration() {
            @Override
            public void configure(PoolingDataSource ds, String connectionURL) {
                ds.setClassName("oracle.jdbc.xa.client.OracleXADataSource");
                ds.getDriverProperties().put(
                    "URL",
                    connectionURL != null
                        ? connectionURL :
                        "jdbc:oracle:thin:test/test@192.168.56.101:1521:xe"
                );

                // Required for reading VARBINARY/LONG RAW columns easily, see
                // http://stackoverflow.com/questions/10174951
                Properties connectionProperties = new Properties();
                connectionProperties.put("useFetchSizeWithLongColumn", "true");
                ds.getDriverProperties().put("connectionProperties", connectionProperties);
            }
        },
        org.hibernate.dialect.Oracle10gDialect.class.getName()
    ),

    POSTGRESQL(
        new DataSourceConfiguration() {
            @Override
            public void configure(PoolingDataSource ds, String connectionURL) {
                ds.setClassName("org.postgresql.xa.PGXADataSource");
                if (connectionURL != null) {
                    throw new IllegalArgumentException(
                        "PostgreSQL XADataSource doesn't support connection URLs"
                    );
                }
                ds.getDriverProperties().put("serverName", "127.0.0.1");
                ds.getDriverProperties().put("databaseName", "jpwh_2e_examples");
                ds.getDriverProperties().put("user", "jpwh_2e_examples");
                ds.getDriverProperties().put("password", "MyNewPass4!");
            }
        },
        org.hibernate.dialect.PostgreSQL10Dialect.class.getName()
    ),

    MYSQL(
        new DataSourceConfiguration() {
            @Override
            public void configure(PoolingDataSource ds, String connectionURL) {
                // TODO: MySQL XA support is completely broken, we use the BTM XA wrapper
                //ds.setClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
                ds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
                ds.getDriverProperties().put(
                    "url",
                    connectionURL != null
                        ? connectionURL :
                        "jdbc:mysql://localhost/jpwh_2e_examples?sessionVariables=sql_mode='PIPES_AS_CONCAT'"
                );

                ds.getDriverProperties().put("driverClassName", "com.mysql.cj.jdbc.Driver");
            }
        },
        // While we use MySQL8Dialect nevertheless, JPASetup constructor will leave hibernate.dialect undefined for
        // MySQL to leave it to runtime detection. This approach is ugly but has these benefits:
        // 1) the best dialect is always chosen at runtime
        // 2) no need to define one MYSQL enum value here for each MYSQL dialect that's available
        // 2) no need to re-design the DatabaseProduct, which is simple yet elegant
        org.hibernate.dialect.MySQL8Dialect.class.getName()
    );

    public DataSourceConfiguration configuration;
    public String hibernateDialect;

    private DatabaseProduct(DataSourceConfiguration configuration,
                            String hibernateDialect) {
        this.configuration = configuration;
        this.hibernateDialect = hibernateDialect;
    }

    public interface DataSourceConfiguration {

        void configure(PoolingDataSource ds, String connectionURL);
    }

}
