package momomo.com.db.session;

import momomo.com.annotations.informative.Private;
import org.hibernate.Session;

/**
 * @author Joseph S.
 */
public interface $SessionNewRequire extends $SessionRepositoryDeclaration {
    @Private boolean DEFAULT_OPEN = false;
    
    Session newSession    ();
    Session requireSession();
    
    default Session session() {
        return session(DEFAULT_OPEN);
    }
    
    default Session session(boolean open) {
        return open ? newSession() : requireSession();
    }
}
