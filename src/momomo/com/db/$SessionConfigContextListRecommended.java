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
 * 
 * Currently, with Hibernate there is no way to reuse a session if you required a new one, which for newTransaction(...) would mean that a 
 * subsequent or nested call to requireTransaction would not recognize it, and hence return a new session itself.
 * 
 * This is a flaw in Hibernate's **** API. 
 * It is a shame that major major parts of hibernates API and classes are a) so complex (unnecessarily) , b) declare private fields, methods and classes everywhere making extending them very complicated! 
 *
 * If you require advanced transaction capabilities, ie, nested transactions such as starting a new transaction while you have ongoing one 
 * this at all we suggest you use this one. 
 * 
 * If not, you should be using the almost identical one {@link $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazyInsane} but which prevents Hibernate from wrapping the session instance normally done 
 * at times by Hibernate in {@link org.hibernate.context.internal.ThreadLocalSessionContext} to a wrapped and limited Session as {@link org.hibernate.context.internal.ThreadLocalSessionContext.TransactionProtectionWrapper}. 
 * 
 * Highly problematic, as you can not make certain method calls on the session which are perfectly valid to do, such as doWork(connection -> {}). 
 * 
 * We also provide {@link sessionfactory.$SessionFactory} implementation which also synchronize the main openSession() call as well which the default {@link org.hibernate.internal.SessionFactoryImpl} does not! 
 *
 * All our classes that implement our base class {@link $SessionConfigContextBase} such as
 * 
 *   - {@link $SessionConfigContextListRecommended}
 *   - {@link $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazyInsane}  # Multiple sessions are not allowed. Based on Hibernates limited implementation which rollbacks and terminates any previous existing session on threadlocal while creating / opening a new one. Eats exception should there be one while rolling back and only logs it! Crazy, insane? They even roll you back! And if there is no error you won't know they rolled you! If there is an error, it is basically silent! No bubbling earning it the name Insane.  
 *   - {@link $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazySane}    # Multiple sessions are not allowed. Based on Hibernates limited implementation which rollbacks and terminates any previous existing session on threadlocal while creating / opening a new one. Throws exception should there be one while rolling back. Crazy, sane? We still think rolling back is crazy, so we retain the crazy name, but at least is not is not silent, as we throw the rollback exception so it bubbles up should there be one, earning it the name Sane.  
 *   - {@link $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazyLaxed}   # Multiple sessions are allowed    . Based on Hibernates limited implementation which does not terminate or do anything with previous session, allows it to coexist. No rollback occurs. The developer might still terminate, commit and manually handle any previous transaction created. Still Crazy, since when you close the new session, the previous one can not be accessed, despite the thread actually having one. Crazy, but Laxed.    
 *
 * What all of these does is to keep track of openSession() calls while Hibernate does not. A call to openSession() is required if you need a new Transaction, however a subsequent call to requireTransaction or getCurrentSession() won't get you the one started uing openSession() because it is untracked so you get TWO transactions while you likely expected it to coninue with the already existing one.   
 * 
 * Using implementations based on
 * 
 * {@link $SessionConfigContextBase}
 * 
 * are still ABLE to keep track of openSession() calls in case you are not using our {@link sessionfactory.$SessionFactory} but only if you use our API to call openSession().
 * 
 * If you want to be sure you would need to tell Hiberante to use our {@link sessionfactory.$SessionFactory}. You can do this simply returning true in {@link $SessionConfig#useMomomoSessionFactory()}.   
 * but you still have to use an implementation based on {@link $SessionConfigContextBase}.
 * 
 * You would do this using
 * 
 *   properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, {@link $SessionConfigContextListRecommended}.class.getName());
 * 
 * which is already default in our {@link $SessionConfig#properties()}
 * 
 * You could also use one of the others
 * 
 *   properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, {@link $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazyInsane}.class.getName());
 *   properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, {@link $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazySane}.class.getName());
 *   properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, {@link $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazyLaxed}.class.getName());
 *   
 * If you do not like any of this, and you do not care about registering openSession() calls, or nested or the ability to inherite any open transaction, you can still opt for Hibernate to track your threads as you've done in the past perhaps as you do not want to risk it (no risk really). 
 * We still however recommend you to use our version which simply does not wrap to a limited {@link org.hibernate.context.internal.ThreadLocalSessionContext.TransactionProtectionWrapper} which we can not work with in our transaction API if you want more features.
 * To still be able to use some features of ours but not nested transactions, consider using: 
 *
 *   properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, {@link $SessionConfigThreadLocalSessionContextUnwrappedUntrackedNotRecommended}.class.getName());
 *   
 * This only tells the superclass {@link org.hibernate.context.internal.ThreadLocalSessionContext} not to wrap the session. Luckily needsWrapping method in superclass is not private, nor final! Thank you Hibernate for this terrible oversight! Next year they will hide it!
 * 
 * This version does not track openSession calls however. If you want the same but also want to register openSession() calls for nested or inheritable transactions regardless, you should be using
 *   properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, {@link $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazyInsane}.class.getName());
 *  
 *  Only difference between {@link $SessionConfigThreadLocalSessionContextUnwrappedUntrackedNotRecommended} and {@link $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazyInsane} is that the latter will also track openSession calls. 
 *  
 * @author Joseph S.
 */
public class $SessionConfigContextListRecommended extends $SessionConfigContextBase {
    private static final ThreadLocal<Map<SessionFactory, SessionsTracker>> THREAD_LOCAL = ThreadLocal.withInitial(HashMap::new);
    
    public $SessionConfigContextListRecommended(SessionFactoryImplementor factory) {
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
