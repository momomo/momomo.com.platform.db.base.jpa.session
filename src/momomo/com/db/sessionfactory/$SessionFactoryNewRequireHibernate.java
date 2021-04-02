package momomo.com.db.sessionfactory;

import momomo.com.db.$SessionConfigContextBase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * @author Joseph S.
 */
public interface $SessionFactoryNewRequireHibernate extends $SessionFactoryNewRequire, $SessionFactoryRepositoryDeclaration {
    
    /**
     * We override it for Hibernate! 
     */
    @Override
    default Session newSession() {
        return newSession(repository().sessionFactory());
    }
    
    /**
     * This puts the openSession() session on the thread local stack as well which is not default in Hibernate  
     */
    static Session newSession(SessionFactory sessionFactory) {
        if ( sessionFactory instanceof $SessionFactory) {
            // Already handled internally without our $SessionFactory class!
            return sessionFactory.openSession();
        }
        
        // Not our sessionFactory. 
        
        // We check if the current setup is using our LocalSessionContext which has features for taking care of it.  
        $SessionConfigContextBase context = $SessionConfigContextBase.get(sessionFactory);
        if ( context != null ) {
            // Yes, it is ours. 
            return context.openSession();
        }
        
        // Nothing we can really do here since the developer is using some other session manager. 
        // It will work, but not as well as ours which offers even nested capabilities. 
        // The issue at hand is that the default 'thread' does not register openSession() calls to threadLocal but only 
        // those accessed though getCurrentSession(). 
        // This makes calls like openSession(); then requireSession() not reuse the first one, 
        // because hibernate never register it. It is free. Ours do register it. 
        // Hibernate also won't allow you session.doWork consistently on the transaction since it wraps sessions at times allowing 
        // only a handful of methods on it which limitations that make no sense!      
        return sessionFactory.openSession();
    }
}







