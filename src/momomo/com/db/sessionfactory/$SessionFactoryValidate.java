package momomo.com.db.sessionfactory;

import momomo.com.db.entities.$Entity;
import momomo.com.db.session.$SessionValidate;

/**
 * @author Joseph S.
 */
public interface $SessionFactoryValidate extends $SessionValidate, $SessionFactoryRepositoryDeclaration {

    default <T extends $Entity> T findByField(Class<T> entityClass, String property, Object value) {
        return repository().findByField(entityClass, property, value);
    }

}
