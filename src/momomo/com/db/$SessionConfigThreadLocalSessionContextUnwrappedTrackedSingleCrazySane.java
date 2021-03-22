package momomo.com.db;

import momomo.com.log;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joseph S.
 */
public class $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazySane extends $SessionConfigThreadLocalSessionContextUnwrappedTracked {
    
    /////////////////////////////////////////////////////////////////////
    
    /**
     * Stupid is the default level for this class. 
     * Do not even use this class but it is provided as a means as staying almost identical in behaviour to {@link org.hibernate.context.internal.ThreadLocalSessionContext}
     * for those that do not want to take any risks of introducing change to a system currently dealing with the old way. 
     */
    public enum InsanityLevel {
        CRAZY_INSANE, CRAZY_SANE, CRAZY_LAXED
    }
    
    /////////////////////////////////////////////////////////////////////
    
    private static final ThreadLocal<Map<SessionFactory,Session>> THREAD_LOCAL = ThreadLocal.withInitial(HashMap::new);
    private        final InsanityLevel insanityLevel;
    
    public $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazySane(SessionFactoryImplementor factory) {
        this(factory, InsanityLevel.CRAZY_SANE);
    }
    
    protected $SessionConfigThreadLocalSessionContextUnwrappedTrackedSingleCrazySane(SessionFactoryImplementor factory, InsanityLevel insanityLevel) {
        super( factory );
        
        this.insanityLevel = insanityLevel;
    }
    
    @Override
    protected Session getCurrentSession() {
        return getSession(this.factory());
    }
    
    /////////////////////////////////////////////////////////////////////
    
    protected Session getSession() {
        return getSession(factory());
    }
    public static Session getSession(SessionFactory factory) {
        return getThreadLocalSessionFactories().get(factory);
    }
    
    /////////////////////////////////////////////////////////////////////
    
    protected static Map<SessionFactory, Session> getThreadLocalSessionFactories() {
        return THREAD_LOCAL.get();
    }
    
    @Override
    public void bind(Session session) {
        Session previous = getThreadLocalSessionFactories().put(session.getSessionFactory(), session);
        
        /**
         * Note, the doBind method in {@link org.hibernate.context.internal.ThreadLocalSessionContext#doBind} also closes down orphans, 
         * while we don't see why that should have to be. If you require a new transaction, then why does the old one has to finish, or rollback?
         * Sure, without a stack we can not get back to it by completing this, and then requireTransaction again. 
         * That is why we recommend you use {@link $SaneThreadLocalSessionContextStack} over this one. 
         **/
        if ( previous != null ) {
            switch ( this.insanityLevel ) {
                case CRAZY_INSANE -> {
                    crazyInsane_terminateOrphanedSession(previous);
                }
                case CRAZY_SANE -> {
                    crazySane_terminateOrphanedSession(previous);
                }
                case CRAZY_LAXED -> {
                    relaxedTerminateSoCalledOrphanedSession(previous);
                }
            }
        }
    }
    
    @Override
    public void unbind(Session session) {
        getThreadLocalSessionFactories().remove(session.getSessionFactory());
    }
    
    
    /////////////////////////////////////////////////////////////////////
    /// Again, stupid hibernate makes thing private
    /////////////////////////////////////////////////////////////////////
    
    /**
     * Very similar to {@link org.hibernate.context.internal.ThreadLocalSessionContext#terminateOrphanedSession(org.hibernate.Session)}
     * but who is unfortunately private so we repeat a similar implementation
     *
     * This is completely off the balls. Not only does a bind lead to the termination of the old session, rather than result in an error, 
     * it will do so silentely. Not notifying you other than in the event of a ROLLBACK error.
     *
     * Completely off the chains. At least fail instead of disregarding the data silently! 
     */
    protected static void crazyInsane_terminateOrphanedSession(Session previous) {
        try {
            Transaction transaction = previous.getTransaction();
            
            if ( transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE ) {
                
                try {
                    transaction.rollback();
                }
                catch( Throwable t ) {
                    log.debug( "Unable to rollback transaction for orphaned session", t );
                }
            }
        }
        finally {
            try {
                previous.close();
            }
            catch( Throwable t ) {
                log.debug( "Unable to close orphaned session", t );
            }
        }
    }
    
    /**
     * Differs from the previous one simply that it does not catch the exceptions should there be none
     */
    protected static void crazySane_terminateOrphanedSession(Session previous) {
        try (previous) {
            Transaction transaction = previous.getTransaction();
        
            if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
                transaction.rollback();
            }
        }
    }
    
    private void relaxedTerminateSoCalledOrphanedSession(Session previous) {
        // We do nothing with the now so called orphaned session. 
        // We let it live and close by its own. We attach a new session to the threadlocal and the old one gets pooof gone. 
        // Note, that even when we eventually close down this new session, the old one which poofed, won't get back in line! 
        // So you would have a session out there untracked. We figure this is at least better than the default other stupid modes
        // 
    }
    
}
