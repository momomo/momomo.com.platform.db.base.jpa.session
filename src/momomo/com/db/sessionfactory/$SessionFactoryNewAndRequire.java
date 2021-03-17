package momomo.com.db.sessionfactory;

import momomo.com.db.session.$SessionNewAndRequire;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * @author Joseph S.
 */
public interface $SessionFactoryNewAndRequire extends $SessionNewAndRequire, $SessionFactoryRepositoryDeclaration {

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
