package momomo.com.db.session;

import org.hibernate.Session;

/**
 * @author Joseph S.
 */
public interface $SessionNewRequire extends $SessionRepositoryDeclaration {
    
    Session newSession    ();
    Session requireSession();
    
    default Session session() {
        return session(false);
    }
    
    default Session session(boolean open) {
        return open ? newSession() : requireSession();
    }
}
