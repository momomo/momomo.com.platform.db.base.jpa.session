package momomo.com.db.sessionfactory;

import momomo.com.db.session.$SessionNewRequire;
import org.hibernate.Session;

/**
 * @author Joseph S.
 */
public interface $SessionFactoryNewRequire extends $SessionNewRequire, $SessionFactoryDeclaration {

    @Override
    default Session newSession() {
        return sessionFactory().openSession();
    }
    
    @Override
    default Session requireSession() {
        return sessionFactory().getCurrentSession();
    }
    
}







