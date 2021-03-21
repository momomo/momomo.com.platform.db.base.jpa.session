/* Copyright(C) 2014 - 2020 Momomo LTD. Proprietary and confidential. Usage of this file on any medium without a written consent by Momomo LTD. is strictly prohibited. All Rights Reserved. */

package momomo.com.db;

import momomo.com.Reflects;
import momomo.com.db.entities.$EntityId;
import momomo.com.db.sessionfactory.$SessionFactoryRepositoryDeclaration;
import org.hibernate.Criteria;
import org.hibernate.metadata.ClassMetadata;

import java.io.Serializable;
import java.util.List;

public interface $ServiceEntityClass<T extends $EntityId>  extends $SessionFactoryRepositoryDeclaration {
    
    Class<T> entityClass();
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    default T create() {
        return Reflects.newInstance(entityClass());
    }
    
    default ClassMetadata metadata() {
        return repository().metadata(entityClass());
    }
    
    default T load(Serializable id) {
        return repository().load(entityClass(), id);
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    default List<T> list() {
        return repository().list(
            repository().criteria( entityClass() )
        );
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    default T findByField(String property, Object value) {
        return repository().findByField(entityClass(), property, value);
    }
    
    default T findByProperty(String property, Object value) {
        return repository().findByProperty(entityClass(), property, value);
    }
    
    default List<T> findAllByProperty(String propertyName, Object value) {
        return repository().findAllByProperty(entityClass(), propertyName, value);
    }
    default List<T> findByProperty(String propertyName, Object... values) {
        return repository().findAllInProperty(entityClass(), propertyName, values);
    }
    default List<T> findByProperty(String propertyName, List<Object> values) {
        return repository().findAllInProperty(entityClass(), propertyName, values);
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    default long count() {
        return repository().count(entityClass());
    }
    
    default boolean isEmpty() {
        return repository().isEmpty(entityClass());
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    default Criteria criteria() {
        return repository().criteria(entityClass());
    }
    
    default Criteria criteriaSingle() {
        return repository().criteriaSingle(entityClass());
    }
    
    default Criteria criteriaSingle(Criteria criteria) {
        return repository().criteriaSingle(criteria);
    }
    
    default Criteria criteria(Integer limit, Integer offset) {
        return repository().criteria(entityClass(), limit, offset);
    }
    
    
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~
    
}
