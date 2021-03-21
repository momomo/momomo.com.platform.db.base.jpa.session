package momomo.com.db.session;

import momomo.com.db.$RepositoryOther;
import momomo.com.db.entities.$Entity;
import org.hibernate.Session;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionImplementor;

/**
 * @author Joseph S.
 */
public interface $SessionOther extends $RepositoryOther, $SessionRepositoryDeclaration {
    
    private Session session() {
        return repository().session();
    }
    
    default SessionImplementor sessionImplementor() {
        return (SessionImplementor) session();
    }
    
    default PersistenceContext persistenceContext() {
        return sessionImplementor().getPersistenceContext();
    }
    
    @Override
    default boolean isNew($Entity entity) {
        return !repository().contains(entity) && repository().persistenceContext().getEntry(entity) == null;
    }
}
