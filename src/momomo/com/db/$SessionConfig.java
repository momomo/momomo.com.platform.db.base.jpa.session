/* Copyright(C) 2014 - 2020 Momomo LTD. Proprietary and confidential. Usage of this file on any medium without a written consent by Momomo LTD. is strictly prohibited. All Rights Reserved. */
package momomo.com.db;

import lombok.Setter;
import lombok.experimental.Accessors;
import momomo.com.Ex;
import momomo.com.IO;
import momomo.com.Is;
import momomo.com.Lambda;
import momomo.com.Reflects;
import momomo.com.Strings;
import momomo.com.annotations.informative.Overridable;
import momomo.com.annotations.informative.Overriden;
import momomo.com.db.sessionfactory.$SessionFactory;
import momomo.com.log;
import momomo.com.Globals;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.boot.internal.SessionFactoryOptionsBuilder;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.bytecode.internal.SessionFactoryObserverForBytecodeEnhancer;
import org.hibernate.bytecode.spi.BytecodeProvider;
import org.hibernate.cache.jcache.internal.JCacheRegionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.transaction.internal.TransactionImpl;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Driver;
import java.util.EnumSet;
import java.util.Properties;

/**
 *
 * Pseudo example:
 *
 *  public static final SessionFactory SINGLETON = new $SessionConfig...(){ ... }.create(new Params());
 *
 * @author Joseph S.
 */
public abstract class $SessionConfig<DATABASE extends $Database> {
    public static final String BATCH_SIZE = "50";
    
    public    final DATABASE    database;  // We expose this for access from anywhere intentionally should the config be provided inline in sub constuctor
    protected final boolean     existed;   // At times, someone might want to know if the database already existed before or not
    protected final $Migrations migrations;
    protected final Properties  properties;
    
    protected abstract String[] packages();
    
    protected $SessionConfig(DATABASE database) {
        this.database   = database;
        this.existed    = existsDB();
        this.migrations = newMigrations();
        this.properties = newProperties();
    }
    
    @Overridable protected $Migrations newMigrations() {
        return new $Migrations(database);
    }
    
    @Overridable protected Properties newProperties() {
        return new Properties();
    }
    
    @Overridable protected boolean drop() {
        return false;
    }
    
    @Overridable protected void ensure() {
        if ( !this.existed ) {
            createDB();
        }
        else if ( drop() ) {
            dropDB();
            
            // It might not have been fully dropped, but only certain tables might be dropped
            if ( !existsDB() ) {
                createDB();
            }
        }
    }
    
    protected void createDB() {
        database.create();
    }
    
    @Overriden
    @Overridable protected void dropDB() {
        // Instead of dropping all of the database, instead we drop the tables, since it behaves better with 'pgadmin', 'intellij database' and so forth.
        // A dropped database, requires a new connection and lost context so in development .
        if ( Is.Development() ) {
            database.tablesDrop();
            
            // We also drop the sequences in Development if there is an implementation for it ( {@link momomo.com.db.$DatabasePostgres} implements it ).
            if ( database instanceof $DatabaseSystemSequences) {
                (($DatabaseSystemSequences) database).sequencesDrop();
            }
            
            // We might actually need to drop other things for other databases, who knows? Implement in your subclass. 
        }
        else {
            // For test and production a normal drop is performed if invoked
            database.drop();
        }
    }
    
    protected final boolean existsDB() {
        return Ex.runtime(database::exists);
    }
    
    @Overridable
    protected String url() {
        return database.url();
    }
    
    @Overridable
    protected String username() {
        return database.username();
    }
    
    @Overridable
    protected String password() {
        return database.password();
    }
    
    @Overridable
    protected Class<? extends Driver> driverClass() {
        return database.driverClass();
    }
    @Overridable
    protected Class<?> dialect() {
        return database.dialect();
    }
    
    /////////////////////////////////////////////////////////////////////
    
    @Accessors(chain = true, fluent = true) @Setter public static final class Params {
        private final Export export = new Export();
        
        public <E extends Exception> Params export(Lambda.V1E<Export, E> lambda) throws E{
            lambda.call(this.export); return this;
        }
        
        @Accessors(chain = true, fluent = true) @Setter public static final class Export {
            String              delimiter   = ";";
            String              outputFile  = IO.getCanonicalPath(IO.tmpFileCreate());
            boolean             haltOnError = true;
            TargetType          target      = TargetType.SCRIPT;
            SchemaExport.Action action      = SchemaExport.Action.CREATE;
        }
    }
    
    @Overridable protected Params params() {
        return new Params();
    }
    
    public SessionFactory create() {
        Params params = params();
        
        ensure();
        
        properties();
        
        // We do everything manually rather than use the "quick" mechanism to ensure we can manipulate every steps
        
        BootstrapServiceRegistryBuilder bootstrapBuilder = new BootstrapServiceRegistryBuilder().enableAutoClose();
        
        onBootstrap(bootstrapBuilder);
    
        BootstrapServiceRegistry bootstrap = bootstrapBuilder.build();
        
        /////////////////////////////////////////////////////////////////////
        
        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder(bootstrap).applySettings(properties);
        
        onRegistry(registryBuilder);
        
        StandardServiceRegistry registry = registryBuilder.build();
        
        /////////////////////////////////////////////////////////////////////
    
        MetadataSources metadataSources = new MetadataSources(registry);
        log.info(getClass(), "Started iterating packages for entities.");
        
        for (String packege : packages()) {
            eachClass(packege, klassName -> {
                Class<Object> klass = Reflects.getClass(klassName);
            
                metadataSources.addAnnotatedClass(klass);
            });
        }
        log.info(getClass(), "Done iterating packages for entities.");
        
        onMetadataSources(metadataSources);
        
        /////////////////////////////////////////////////////////////////////
        
        MetadataBuilderImpl metadataBuilder = (MetadataBuilderImpl) metadataSources.getMetadataBuilder();
        
        BootstrapContext bootstrapContext = metadataBuilder.getBootstrapContext();
                                                
        // On this call here, the database has to exists for hibernate c3p0 to connect to it properly unfortunately
        MetadataImpl metadata = (MetadataImpl) metadataSources.buildMetadata();
        
        onMetadata(metadata);
        
        /////////////////////////////////////////////////////////////////////
        
        SchemaExport export = new SchemaExport();
        export.setOutputFile  (params.export.outputFile);
        export.setDelimiter   (params.export.delimiter);
        export.setHaltOnError (params.export.haltOnError);
        
        onExport(export);
        
        if ( params.export.action != null ) {
            export.execute(EnumSet.of(params.export.target), params.export.action, metadata);
        }
        
        /////////////////////////////////////////////////////////////////////
        
        // Now the sql has been generated, so we can call migrate to do some stuff for us should we want to
        migrate( new File(params.export.outputFile) );
    
        /////////////////////////////////////////////////////////////////////
        
        if ( useMomomoSessionFactory() ) {
            SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
    
            return sessionFactoryBuilder.build();
        }
        else {
            // Also works, and is basically the stuf in the if block inlined in order to be able to manipulate and provide ours instead
            SessionFactoryOptionsBuilder sessionFactoryOptionsBuilder = new SessionFactoryOptionsBuilder(registry, bootstrapContext);
    
            onSessionFactoryOptions(sessionFactoryOptionsBuilder);
    
            metadata.validate();
    
            BytecodeProvider bytecodeProvider = registry.getService(BytecodeProvider.class);
    
            sessionFactoryOptionsBuilder.addSessionFactoryObservers(new SessionFactoryObserverForBytecodeEnhancer(bytecodeProvider));
    
            // We use our SessionFactory implementation here which implements our custom twist for openSession() that registers the instance on threadlocal
            // Not really required unless it is invoked outside of our libraries 
            return new $SessionFactory(metadata, sessionFactoryOptionsBuilder.buildOptions(), HQLQueryPlan::new);
        }
        
    }
    
    @Overridable protected void onBootstrap(BootstrapServiceRegistryBuilder bootstrap) {
        
    }
    
    @Overridable protected void onRegistry(StandardServiceRegistryBuilder registry) {
        
    }
    
    @Overridable protected void onMetadataSources(MetadataSources sources) {
        
    }
    
    @Overridable protected void onMetadata(Metadata metadata) {
        
    }
    
    @Overridable protected void onExport(SchemaExport export) {
        
    }
    
    /**
     * If true will ensure our sessionfactory is in use which always tracks any open session, wether through sessionFactory.getCurrentSession() or sessionFactory.openSession(). 
     * Note that the previous statement is only through for the two method calls as there are other ways to open a session. 
     * When using our API which only has newSession() and requireSession() methods, sessions will be tracked only if you are using any of our
     *   {@link momomo.com.db.$SessionConfigThreadLocalSessionContext} 
     *   {@link momomo.com.db.$SessionConfigThreadLocalSessionContextRecommended} 
     *   {@link momomo.com.db.$SessionConfigThreadLocalSessionContextUnwrapped} 
     * 
     * implementations with
     * 
     *   properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, .....class.getName());
     */
    @Overridable boolean useMomomoSessionFactory() {
        return false;
    }
    
    @Overridable protected void onSessionFactoryOptions(SessionFactoryOptionsBuilder options) {
        if ( Is.Off ) {
            // Leave for reference, not used anymore due to other strategies being employed instead
            options.applyInterceptor(new EmptyInterceptor() {
                @Override
                public void afterTransactionBegin(Transaction tx) {
                    Field          field          = Reflects.getField(TransactionImpl.class, "session");
                    Session        session        = Reflects.getValue(tx, field);
                    SessionFactory sessionFactory = session.getSessionFactory();
                    
                    // We check if the current setup is using our LocalSessionContext which has features for taking care of it.  
                    $SessionConfigThreadLocalSessionContext context = $SessionConfigThreadLocalSessionContext.get(sessionFactory);
                    if ( context != null ) {
                        // Yes, it is ours.
                        context.bind(session);  // We bind it
                    }
                }
                
                @Override
                public void afterTransactionCompletion(Transaction tx) {
                    Field          field          = Reflects.getField(TransactionImpl.class, "session");
                    Session        session        = Reflects.getValue(tx, field);
                    SessionFactory sessionFactory = session.getSessionFactory();
                    
                    // We check if the current setup is using our LocalSessionContext which has features for taking care of it.  
                    $SessionConfigThreadLocalSessionContext context = $SessionConfigThreadLocalSessionContext.get(sessionFactory);
                    if ( context != null ) {
                        // Yes, it is ours.
                        context.unbind(session);  // We bind it
                    }
                }
            });
        }
    }
    
    @Overridable protected void migrate(File sql) {
        // By default we do not impose our implementation by mistake or otherwise 
    }
    
    /////////////////////////////////////////////////////////////////////
    
    private <E extends Exception> void eachClass(String packege, Lambda.V1E<String, E> lambda) throws E {
        eachClass("", packege, lambda);
    }
    
    private <E extends Exception> void eachClass(String indentation, String packege, Lambda.V1E<String, E> lambda) throws E {
        log.info(indentation + "+ " + packege + "/");
        
        IO.Iterate.Url.each(getClass().getClassLoader().getResource(packege), entry -> {
            if ( entry.isDirectory() ) {
                eachClass(indentation + Strings.TAB, packege + "/" + Strings.unslash(entry.getRelativeIterationPath(), false, true), lambda);
            }
            else {
                String klassName = entry.getRelativeIterationPath();
                
                String klass = (packege + "/" + klassName).replaceAll("/", ".").replaceAll(".class", "");
                
                lambda.call(klass);
            }
        });
    }
    
    protected void properties() {
        properties.put(Environment.DIALECT, dialect().getName());
        properties.put(Environment.DRIVER,  driverClass().getName());
        properties.put(Environment.URL, url());
        properties.put(Environment.USER, username());
        properties.put(Environment.PASS, password());
        
        // Nothing visibly better but we leave it
        properties.put(Environment.STATEMENT_BATCH_SIZE, BATCH_SIZE);
        properties.put(Environment.DEFAULT_BATCH_FETCH_SIZE, BATCH_SIZE);
        
        properties.put(Environment.C3P0_ACQUIRE_INCREMENT          , 3  );
        properties.put(Environment.C3P0_MIN_SIZE                   , 3  );
        properties.put(Environment.C3P0_MAX_SIZE                   , 50 );
        properties.put("hibernate.c3p0.maxStatementsPerConnection" , 99 );
        
        // properties.put(Environment.C3P0_MAX_STATEMENTS   , 75);
        // properties.put("hibernate.c3p0.maxConnectionAge", 10  );
        // properties.put("hibernate.c3p0.autoCommitOnClose", true   );
        // properties.put("hibernate.c3p0.unreturnedConnectionTimeout", true   );
        
        properties.put(Environment.ISOLATION, Connection.TRANSACTION_READ_COMMITTED);
        
        {
            properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, $SessionConfigThreadLocalSessionContextRecommended.class.getName());
            // properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, $TTT.class.getName());
            // properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, $ThreadLocalSessionContextUnwrappedCrazySane.class.getName());
            // These options are just examples and requires WildFlyStandAloneJtaPlatform to be on your system
            // properties.put("hibernate.transaction.manager_lookup_class", "org.hibernate.transaction.JBossTransactionManagerLookup");
            // properties.put("hibernate.transaction.factory_class", "org.hibernate.transaction.JTATransactionFactory");
            // properties.put("hibernate.transaction.jta.platform", WildFlyStandAloneJtaPlatform.class);
        }
        
        {   // Cache setup
            properties.put(Environment.USE_SECOND_LEVEL_CACHE, true);
            properties.put(Environment.USE_QUERY_CACHE, true);
            properties.put(Environment.CACHE_REGION_FACTORY, JCacheRegionFactory.class.getName());
            properties.put("hibernate.javax.cache.provider", EhcacheCachingProvider.class.getName());
            // properties.put("hibernate.javax.cache.uri", "App/config/CacheConfig.xml");
        }
        
        if ( false ) {
            // Can be used to debug connections
            properties.put("hibernate.c3p0.unreturnedConnectionTimeout", 5);
            properties.put("hibernate.c3p0.debugUnreturnedConnectionStackTraces", true);
        }
        
        if ( Globals.Configurable.DATABASE_SQL_LOGGING.isTrue() ) {
            properties.put(Environment.FORMAT_SQL       , "true");
            properties.put(Environment.SHOW_SQL         , "true");
            properties.put(Environment.USE_SQL_COMMENTS , "true");
        }
    }
    
}
