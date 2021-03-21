package momomo.com.db.sessionfactory;

import momomo.com.db.$SessionConfigThreadLocalSessionContext;
import momomo.com.db.$SessionConfigThreadLocalSessionContextRecommended;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.internal.SessionFactoryImpl;

/**
 * We only override one method to ensure an openSession call is also added to threadlocal so that a subsequent 
 * requireTransaction call can get you that same session.
 * 
 * The default hibernate 'thread' or {@link org.hibernate.context.internal.ThreadLocalSessionContext} implementation is severely flawed and limited. 
 * 
 * @author Joseph S.
 */
public class $SessionFactory extends SessionFactoryImpl {
    private final $SessionConfigThreadLocalSessionContext context;
    
    public $SessionFactory(MetadataImplementor metadata, SessionFactoryOptions options, QueryPlanCache.QueryPlanCreator queryPlanCacheFunction) {
        super(metadata, options, queryPlanCacheFunction);
    
        // The super one is private, but if it was ours we've registered it and can get it as such
        this.context = $SessionConfigThreadLocalSessionContextRecommended.get(this); 
    }
    
    @Override
    public Session openSession() throws HibernateException {
        if ( this.context != null ) {
            return this.context.openSession();
        }
        else {
            return super.openSession();
        }
    }
}
