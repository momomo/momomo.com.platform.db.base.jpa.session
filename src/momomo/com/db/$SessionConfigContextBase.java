/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package momomo.com.db;

import org.hibernate.ConnectionReleaseMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.spi.AbstractCurrentSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import javax.transaction.Synchronization;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Joseph S.
 */
public abstract class $SessionConfigContextBase extends AbstractCurrentSessionContext {
    
    /**
     * Currently the best way to figure out if a session factory is configured with this {@link org.hibernate.context.spi.CurrentSessionContext}
     * Hibernate / red hat likely intentionally make things private in super classes so both currentSessionContext and buildCurrentSessionContext() are private 
     * and can not be accessed other than through reflection. 
     * We register ours instead and so we can a
     */
    private static final HashMap<SessionFactory, $SessionConfigContextBase> SESSION_FACTORIES = new HashMap<>();
    
    protected $SessionConfigContextBase(SessionFactoryImplementor factory) {
        super(factory);
    
        SESSION_FACTORIES.put(factory, this);
    }
    
    /**
     * Returns null if there is no contexts for this factory
     */
    public static $SessionConfigContextBase get(SessionFactory factory) {
        return SESSION_FACTORIES.get(factory);
    }
    
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    
    protected abstract Session getCurrentSession(               );
    public    abstract void    bind             (Session session);
    public    abstract void    unbind           (Session session);
    
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    
    @Override
    public Session currentSession() throws HibernateException {
        Session session = getCurrentSession();
        if ( session == null ) {
            session = openSession();
        }
        else {
            super.validateExistingSession( session );
        }
        return session;
    }
    
    public Session openSession() {
        Session session = buildOrObtainSession();
        
        session.getTransaction().registerSynchronization( new CleanupSession(this, session) );
        
        bind( session );
        
        return session;
    }
    
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    
    protected static class CleanupSession implements Synchronization, Serializable {
        protected final $SessionConfigContextBase context;
        private   final Session                    session;
    
        public CleanupSession($SessionConfigContextBase context, Session session) {
            this.context = context;
            this.session = session;
        }
        
        @Override
        public void beforeCompletion() {
            
        }
        
        @Override
        public void afterCompletion(int i) {
            context.unbind(session);
        }
    }
    
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    // Originals below
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    
    @SuppressWarnings("deprecation")
    protected Session buildOrObtainSession() {
        return baseSessionBuilder()
            .autoClose( isAutoCloseEnabled() )
            .connectionReleaseMode( getConnectionReleaseMode() )
            .flushBeforeCompletion( isAutoFlushEnabled() )
            .openSession()
        ;
    }
    
    protected boolean isAutoCloseEnabled() {
        return true;
    }
    
    protected boolean isAutoFlushEnabled() {
        return true;
    }
    
    protected ConnectionReleaseMode getConnectionReleaseMode() {
        return factory().getSettings().getConnectionReleaseMode();
    }
}
