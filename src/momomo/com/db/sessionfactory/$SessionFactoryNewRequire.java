package momomo.com.db.sessionfactory;

import momomo.com.db.session.$SessionNewRequire;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * @author Joseph S.
 */
public interface $SessionFactoryNewRequire extends $SessionNewRequire, $SessionFactoryRepositoryDeclaration {

    private SessionFactory sessionFactory() {
        return repository().sessionFactory();
    }
    
    @Override
    default Session newSession() {
        return sessionFactory().openSession();
    }
    
    @Override
    default Session requireSession() {
        return sessionFactory().getCurrentSession();
    }
    
}







