package org.jpwh.env;

import org.hibernate.internal.util.StringHelper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;

/**
 * Creates an EntityManagerFactory.
 * <p>
 * Configuration of the persistence units is taken from <code>META-INF/persistence.xml</code>
 * and other sources. Additional <code>hbm.xml</code> file names can be given to the
 * constructor.
 * </p>
 */
public class JPASetup {

    protected final String persistenceUnitName;
    protected final Map<String, String> properties = new HashMap<>();
    protected final EntityManagerFactory entityManagerFactory;

    public JPASetup(DatabaseProduct databaseProduct,
                    String persistenceUnitName,
                    String... hbmResources) throws Exception {

        this.persistenceUnitName = persistenceUnitName;

        // No automatic scanning by Hibernate, all persistence units list explicit classes/packages
        properties.put(
            "hibernate.archive.autodetection",
            "none"
        );

        // Really the only way how we can get hbm.xml files into an explicit persistence
        // unit (where Hibernate scanning is disabled)
        List<String> ls = new ArrayList<>();
        if (hbmResources != null) {
            Collections.addAll(ls, hbmResources);
        }

        // We don't want to repeat these settings for all units in persistence.xml, so
        // they are set here programmatically
        properties.put("hibernate.hbmxml.files", StringHelper.join(",", ls.iterator()));
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "true");
        // Takes effect at {@link HibernateSchemaManagementTool#getSchemaCreator() }.
        // See https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#configurations-hbmddl
        // Don't use hibernate.hbm2ddl.auto value. Seen from source code,
        // the entire hbm2ddl tool and package org.hibernate.tool.hbm2ddl are deprecated.
        // Note: only enable it in dev env !!!
        properties.put(org.hibernate.cfg.AvailableSettings.HBM2DDL_DATABASE_ACTION, "drop-and-create");

        // Runtime Byte code enhancements related. Yet to figure out its usefulness and compare it with compile-time
        // enhancement via the hibernate-enhance-maven-plugin .
        // properties.put(org.hibernate.jpa.AvailableSettings.ENHANCER_ENABLE_DIRTY_TRACKING, "true");
        // properties.put(org.hibernate.jpa.AvailableSettings.ENHANCER_ENABLE_LAZY_INITIALIZATION, "true");
        // properties.put(org.hibernate.jpa.AvailableSettings.ENHANCER_ENABLE_ASSOCIATION_MANAGEMENT, "true");
        // properties.put(org.hibernate.cfg.AvailableSettings.USE_REFLECTION_OPTIMIZER, "true");

        // Select database SQL dialect
        // In order for DatabaseProduct.MYSQL to migrate from MySQL57InnoDBDialect to MySQL57Dialect or others,
        // we will also have to set the db storage engine here.
        // We will leave hibernate.dialect undefined for MySQL, such that the best dialect will be determined in
        // org.hibernate.dialect.Database.MYSQL#resolveDialect(). However, one thing to note is that, database-object
        // dialect-scope instructions in hibernate-mapping XML files shall be defined for all possibly supported MySQL
        // versions. Fortunately, multiple dialect-scope instructions are allowed for one database-object.
        if (databaseProduct.equals(DatabaseProduct.MYSQL)) {
            properties.put("hibernate.dialect.storage_engine", "innodb");
        } else {
            properties.put("hibernate.dialect", databaseProduct.hibernateDialect);
        }

        entityManagerFactory = Persistence.createEntityManagerFactory(getPersistenceUnitName(), properties);
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public void createSchema() {
        generateSchema("create");
    }

    public void dropSchema() {
        generateSchema("drop");
    }

    public void generateSchema(String action) {
        // Take exiting EMF properties, override the schema generation setting on a copy
        Map<String, String> createSchemaProperties = new HashMap<>(properties);
        createSchemaProperties.put(
            "javax.persistence.schema-generation.database.action",
            action
        );
        Persistence.generateSchema(getPersistenceUnitName(), createSchemaProperties);
    }
}
