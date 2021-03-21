/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package momomo.com.db;

import momomo.com.collections.$IndexedLinkedHashSet;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores a 'stack' of active sessions rather than just one per thread. 
 *
 * Anytime get currentSession() is called, the last one to have registered is the one to use. 
 * This is to ensure that calls to openSession() which for our Transactional API means, start a new transaction, will also be registered. 
 * Currently, with Hibernate there is no way to reuse a session if you required a new one, which for newTransaction(...) would mean that a 
 * subsequent or nested call to requireTransaction would not recognize it, and hence return a new session itself. 
 * This is a flaw in Hibernate's **** API. It is a shame that major major parts of hibernates API and classes are 
 * a) so complex (unneccerarily) , b) declared private fields, methods and classes everywhere. 
 *
 * If you do not require this at all we suggest you use the one without a Stack, and almost idential to the one Hibernate provides 
 * but a) synchronizes openSession as well to the ThreadLocal, which the default SessionFactoryImpl does not! 
 * and b) does not wrap the Session to {@link org.hibernate.context.internal.ThreadLocalSessionContext.TransactionProtectionWrapper}
 * in {@link org.hibernate.context.internal.ThreadLocalSessionContext#wrap(org.hibernate.Session)} which we quite frankly doe not **** understand! 
 *
 * The wrapped session is a proxy for the first and b severely limits what  you can do with it and only allows you to call a handful of methods. 
 *
 * We removed the wrapping completely, but we also retained the oppurtonity to register calls when we do sessionFactory.openSession() as well.
 * This was still possible before, but Hibernate has not left an easy way to detect what SessionContext is in use since it is private in sessionFactory! 
 *
 * @author Joseph S.
 */
public class $SessionConfigThreadLocalSessionContextRecommended extends $SessionConfigThreadLocalSessionContext {
    private static final ThreadLocal<Map<SessionFactory, SessionsTracker>> THREAD_LOCAL = ThreadLocal.withInitial(HashMap::new);
    
    public $SessionConfigThreadLocalSessionContextRecommended(SessionFactoryImplementor factory) {
        super( factory );
    }
    
    @Override
    protected Session getCurrentSession() {
        return getSessionLast();
    }
    
    /////////////////////////////////////////////////////////////////////
    
    protected Session getSessionLast() {
        return getSessionLast(this.factory());
    }
    protected static Session getSessionLast(SessionFactory factory) {
        SessionsTracker tracker = getSessionTracker(factory);
        if ( tracker == null ) {
            return null;
        }
        return tracker.last();
    }
    
    /////////////////////////////////////////////////////////////////////
    
    protected SessionsTracker getSessionTracker() {
        return getSessionTracker(factory());
    }
    protected static SessionsTracker getSessionTracker(SessionFactory factory) {
        return getThreadLocalSessionFactories().get(factory);
    }
    
    /////////////////////////////////////////////////////////////////////
    
    protected static Map<SessionFactory, SessionsTracker> getThreadLocalSessionFactories() {
        return THREAD_LOCAL.get();
    }
    
    /////////////////////////////////////////////////////////////////////
    
    public void bind(Session session) {
        SessionFactory        factory = session.getSessionFactory();
        SessionsTracker tracker = getSessionTracker(factory);
        
        if ( tracker == null ) {
            getThreadLocalSessionFactories().put(factory, tracker = new SessionsTracker());
        }
        
        tracker.bind(session);
    }
    
    /////////////////////////////////////////////////////////////////////
    
    @Override
    public void unbind(Session session) {
        SessionFactory  sessionFactory = session.getSessionFactory();
        SessionsTracker tracker        = getSessionTracker(sessionFactory);
        
        if ( tracker == null ) return;
        
        tracker.unbind(session);
        
        if ( tracker.isEmpty() ) {
            Map<SessionFactory, SessionsTracker> map = getThreadLocalSessionFactories();
            
            // Once there is no more sessions for this factory for this thread we remove the key on this thread for this factory
            map.remove(sessionFactory);
            
            // There is no more sessionFactories either, no point really, but what the hell
            if ( map.isEmpty() ) {
                THREAD_LOCAL.remove();
            }
        }
        
    }
    
    /**
     * @author Joseph S.
     */
    public static final class SessionsTracker {
        /////////////////////////////////////////////////////////////////////
        // Currently simple in nature, but we could add a much more complex handling of new sessions / transaction handling. 
        // For instance, we could allow a sub transction of a parent transaction in case it rolls back, also roll back the parent. 
        // As of now, new transactions that are created while an ongoing transaction is ongoing lives separately and rolls back separately. 
        // This is good, but at times, you might want to introduce a `newDependantTransaction(...)` which creates a new one, but is tied to the 
        // any potential parent transaction. If the parent rolls back, then you do too. If you rollback, the parent might or might not also rollback. 
        /////////////////////////////////////////////////////////////////////
        
        private final $IndexedLinkedHashSet<Session> sessions = new $IndexedLinkedHashSet<>();
        
        public Session last() {
            return sessions.last();
        }
        
        public void bind(Session session) {
            sessions.removeAdd(session);    // Ensures 
        }
        
        public void unbind(Session session) {
            sessions.remove(session);
        }
        
        public boolean isEmpty() {
            return sessions.isEmpty();
        }
    }
    
}
