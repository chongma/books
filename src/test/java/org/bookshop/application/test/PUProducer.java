package org.bookshop.application.test;

import org.apache.commons.dbcp2.BasicDataSource;
import org.bookshop.entities.Book;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

// create the test EMF
@ApplicationScoped
public class PUProducer {
    // a RESOURCE_LOCAL PU works too, this is just an alternative enabling to control the datasource a bit better/easier
    @Produces
    @ApplicationScoped
    public EntityManagerFactory entityManagerFactory(final DataSource dataSource) {
        final PersistenceProvider provider = ServiceLoader.load(PersistenceProvider.class).iterator().next();
        return provider.createContainerEntityManagerFactory(newUnitInfo(provider, dataSource), new HashMap<>());
    }

    public void releaseEntityManagerFactory(@Disposes final EntityManagerFactory entityManagerFactory) {
        entityManagerFactory.close();
    }

    @Produces
    @ApplicationScoped
    public BasicDataSource dataSource() {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:test");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    public void releaseDataSource(@Disposes final BasicDataSource ds) {
        try {
            ds.close();
        } catch (final SQLException throwables) {
            throw new IllegalStateException(throwables);
        }
    }

    private PersistenceUnitInfo newUnitInfo(final PersistenceProvider provider, final DataSource dataSource) {
        final Properties properties = new Properties();
        properties.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");

        return new PersistenceUnitInfo() { // using SE API you don't need this impl
            @Override
            public String getPersistenceUnitName() {
                return "book-pu";
            }

            @Override
            public String getPersistenceProviderClassName() {
                return provider.getClass().getName();
            }

            @Override
            public PersistenceUnitTransactionType getTransactionType() {
                return PersistenceUnitTransactionType.RESOURCE_LOCAL;
            }

            @Override
            public DataSource getJtaDataSource() {
                return dataSource;
            }

            @Override
            public DataSource getNonJtaDataSource() {
                return getJtaDataSource();
            }

            @Override
            public List<String> getMappingFileNames() {
                return emptyList();
            }

            @Override
            public List<URL> getJarFileUrls() {
                return emptyList();
            }

            @Override
            public URL getPersistenceUnitRootUrl() {
                try {
                    return new File("src/main/resources").toURI().toURL();
                } catch (final MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public List<String> getManagedClassNames() {
                return singletonList(Book.class.getName());
            }

            @Override
            public boolean excludeUnlistedClasses() {
                return true;
            }

            @Override
            public SharedCacheMode getSharedCacheMode() {
                return SharedCacheMode.UNSPECIFIED;
            }

            @Override
            public ValidationMode getValidationMode() {
                return ValidationMode.AUTO;
            }

            @Override
            public Properties getProperties() {
                return properties;
            }

            @Override
            public String getPersistenceXMLSchemaVersion() {
                return "2.0";
            }

            @Override
            public ClassLoader getClassLoader() {
                return Thread.currentThread().getContextClassLoader();
            }

            @Override
            public void addTransformer(final ClassTransformer classTransformer) {
                // not needed since entities are enhanced at build time
            }

            @Override
            public ClassLoader getNewTempClassLoader() {
                return getClassLoader(); // no transformer so not needed too
            }
        };
    }

}
