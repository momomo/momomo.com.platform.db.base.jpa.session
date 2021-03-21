package momomo.com.db.sessionfactory;

import momomo.com.db.entities.$Entity;
import momomo.com.db.session.$SessionRepository;
import org.hibernate.SessionFactory;

/**
 * @author Joseph S.
 */
public interface $SessionFactoryRepository extends $SessionRepository, $SessionFactoryPersistence, $SessionFactoryQuery, $SessionFactoryValidate, $SessionFactoryCriteria, $SessionFactoryNewRequire, $SessionFactoryOther, $SessionFactoryRepositoryDeclaration {

    SessionFactory sessionFactory();

    @Override
    default $SessionFactoryRepository repository() {
        return this;
    }

    @Override
    default <T extends $Entity> T findByField(Class<T> entityClass, String property, Object value) {
        return $SessionFactoryCriteria.super.findByField(entityClass, property, value);
    }

}
