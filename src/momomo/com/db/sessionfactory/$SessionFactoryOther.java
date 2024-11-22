package momomo.com.db.sessionfactory;

import momomo.com.db.entities.$Entity;
import momomo.com.db.session.$SessionOther;
import org.hibernate.metadata.ClassMetadata;

/**
 * @author Joseph S.
 */
public interface $SessionFactoryOther extends $SessionOther, $SessionFactoryDeclaration {
    
    /////////////////////////////////////////////////////////////////////
    
    default ClassMetadata metadata(Object entity) {
        return metadata(entity.getClass());
    }
    
    default ClassMetadata metadata(Class<?> entityClass) {
        return sessionFactory().getClassMetadata(entityClass);
    }
    
    @Override
    default Object getId($Entity entity) {
        return metadata(entity).getIdentifier(entity, sessionImplementor());
    }
    
    default boolean hasId($Entity entity) {
        return metadata(entity).hasIdentifierProperty();
    }
    
}
