package momomo.com.db.session;

import momomo.com.Reflects;
import momomo.com.annotations.informative.Protected;
import momomo.com.db.$Entity;
import momomo.com.db.$RepositoryPersistence;
import org.hibernate.Session;

import java.io.Serializable;

/**
 * @author Joseph S.
 */
public interface $SessionPersistence extends $RepositoryPersistence, $SessionRepositoryDeclaration {
    
    /////////////////////////////////////////////////////////////////////
    
    private Session session() {
        return repository().session();
    }

    /////////////////////////////////////////////////////////////////////
    
    @Override
    @Protected default <T extends $Entity> void $save$(T entity) {
        // When we have a session we can call saveOrUpdate more safely
        // We should not need to merge in the catch call. That's mostly for EntityManager implementations
        
        session().saveOrUpdate(entity);
    }
    
    
    default <T extends $Entity> void update (T entity ) {
        session().update( entity );
    }
    
    /////////////////////////////////////////////////////////////////////
    
    @Override
    @Protected default <T extends $Entity> void persist(T entity) {
        session().persist(entity);
    }
    
    @Override
    default <T extends $Entity> boolean contains(T entity) {
        return session().contains(entity);
    }
    
    @Override
    default  <T extends $Entity> T merge(T entity) {
        return Reflects.cast(session().merge(entity));
    }
    
    @Override
    default  <T extends $Entity> void refresh(T entity) {
        session().refresh(entity);
    }
    
    @Override
    default <T extends $Entity> T load(Class<T> entityClass, Serializable id) {
        return Reflects.cast(session().load(entityClass, id));
    }
    
    @Override
    default void flush() {
        session().flush();
    }
    
    @Override
    default void clear() {
        session().clear();
    }
    
    /////////////////////////////////////////////////////////////////////
    
    @Override
    default  <T extends $Entity> void delete(T entity) {
        session().delete(
            session().contains(entity) ?
                entity
                :
                merge(entity)
        );
    }
    
    
}
