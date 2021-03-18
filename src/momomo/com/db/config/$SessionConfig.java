/* Copyright(C) 2014 - 2020 Momomo LTD. Proprietary and confidential. Usage of this file on any medium without a written consent by Momomo LTD. is strictly prohibited. All Rights Reserved. */
package momomo.com.db.config;

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
import momomo.com.db.$Database;
import momomo.com.sources.Globals;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cache.jcache.internal.JCacheRegionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.transaction.jta.platform.internal.WildFlyStandAloneJtaPlatform;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import java.io.File;
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
    
    public    final DATABASE   database;
    protected final boolean    existed;      // At times, someone might want to know if the database already existed before or not
    protected final Properties properties;
    protected final File       schema;
    
    protected abstract String[] packages();
    
    protected $SessionConfig(DATABASE database) {
        this.database   = database;
        this.existed    = existsDB();
        this.properties = newProperties();
        this.schema     = newSchema();
    }
    
    @Overridable
    protected Properties newProperties() {
        return new Properties();
    }
    
    @Overridable
    protected File newSchema() {
        return IO.tmpFileCreate();
    }
    
    @Overridable
    protected boolean drop() {
        return false;
    }
    
    @Overridable
    protected void ensure() {
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
    @Overridable
    protected void dropDB() {
        // Instead of dropping all of the database, instead we drop the tables, since it behaves better with 'pgadmin', 'intellij database' and so forth.
        // A dropped database, requires a new connection and lost context so in development .
        if ( Is.Development() ) {
            database.tablesDrop(null);
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
    protected void migrate() {
        
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
    
    /**
     * Has to be called after call to create()
     */
    protected final String getSQL() {
        return IO.text(schema);
    }
    
    @Accessors(chain = true, fluent = true) @Setter public static final class Params {
        final Export export = new Export();
        
        @Accessors(chain = true, fluent = true) @Setter public static final class Export {
            String              delimiter   = ";";
            String              outputfile  = null;
            boolean             onErrorHalt = false;
            TargetType          script      = TargetType.SCRIPT;
            SchemaExport.Action create      = SchemaExport.Action.BOTH;
        }
    }
    public SessionFactory create(Params params) {
        properties();
        
        StandardServiceRegistryBuilder registry        = new StandardServiceRegistryBuilder().applySettings(properties);
        MetadataSources                metadataSources = new MetadataSources(registry.build());
        
        for (String packege : packages()) {
            eachClass(packege, klassName -> {
                Class<Object> klass = Reflects.getClass(klassName);
                
                metadataSources.addAnnotatedClass(klass);
            });
        }
        
        ensure();
        
        // On this call here, the database has to exists for hibernate c3p0 to connect to it properly unfortunately
        Metadata metadata = metadataSources.buildMetadata();
        
        SchemaExport export = new SchemaExport();
        export.setOutputFile(schema.getAbsolutePath());
        export.setDelimiter(params.export.delimiter);
        export.setHaltOnError(params.export.onErrorHalt);
        export.execute(EnumSet.of(params.export.script), params.export.create, metadata);
        
        migrate();
        
        return metadata.buildSessionFactory();
    }
    
    
    private <E extends Exception> void eachClass(String packege, Lambda.V1E<String, E> lambda) throws E {
        eachClass("", packege, lambda);
    }
    
    private <E extends Exception> void eachClass(String indentation, String packege, Lambda.V1E<String, E> lambda) throws E {
        System.out.println(indentation + "+ " + packege + "/");
        
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
            String transactions = "thread";
            properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, transactions);
            if ("jta".equals(transactions)) {
                // This option is just an example and requires WildFlyStandAloneJtaPlatform to be on your system
                properties.put("hibernate.transaction.jta.platform", WildFlyStandAloneJtaPlatform.class);
            }
        }
        
        {   // Cache setup
            properties.put(Environment.USE_SECOND_LEVEL_CACHE, true);
            properties.put(Environment.USE_QUERY_CACHE, true);
            properties.put(Environment.CACHE_REGION_FACTORY, JCacheRegionFactory.class.getName());
            properties.put("hibernate.javax.cache.provider", EhcacheCachingProvider.class.getName());
            properties.put("hibernate.javax.cache.uri", "App/config/CacheConfig.xml");
        }
        
        if ( false ) {
            // Can be used to debug connections
            properties.put("hibernate.c3p0.unreturnedConnectionTimeout", 5);
            properties.put("hibernate.c3p0.debugUnreturnedConnectionStackTraces", true);
        }
    
        if ( Globals.SQL_LOGGING ) {
            properties.put(Environment.FORMAT_SQL, "true");
            properties.put(Environment.SHOW_SQL, "true");
            properties.put(Environment.USE_SQL_COMMENTS, "true");
        }
    }
    
}
