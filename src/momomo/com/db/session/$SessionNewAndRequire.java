package momomo.com.db.session;

import momomo.com.annotations.informative.Private;
import org.hibernate.Session;

/**
 * @author Joseph S.
 */
public interface $SessionNewAndRequire extends $SessionRepositoryDeclaration {
    @Private boolean DEFAULT_OPEN = false;
    
    Session newSession    ();
    Session requireSession();
    
    default Session session() {
        return session(DEFAULT_OPEN);
    }
    
    default Session session(boolean open) {
        if ( open ) {
            // Will always create a new session
            return newSession();
        }
        else {
            // Will open one if there is none, otherwise will use the same
            return requireSession();
        }
    }
}
