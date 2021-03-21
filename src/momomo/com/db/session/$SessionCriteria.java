package momomo.com.db.session;

import momomo.com.Is;
import momomo.com.db.entities.$Entity;
import momomo.com.db.entities.$EntityId;
import momomo.com.db.$Criteria;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;
import java.util.List;

/**
 * @author Joseph S.
 */
public interface $SessionCriteria extends $Criteria, $SessionRepositoryDeclaration {
    
    /////////////////////////////////////////////////////////////////////
    
    private Session session() {
        return repository().session();
    }
    
    /////////////////////////////////////////////////////////////////////
    
    default <T extends $Entity> Criteria criteria(Class<T> entityClass) {
        return criteria(entityClass, entityClass.getName().toLowerCase());
    }
    
    default <T extends $EntityId<ID>, ID extends Serializable> T findById(Class<T> entityClass, ID value) {
        return findByProperty(entityClass, $EntityId.Cons.id, value);
    }
    
    
    default <T extends $Entity> Criteria criteria(Class<T> entityClass, String name) {
        return session().createCriteria(entityClass, name).setCacheMode(CacheMode.GET);
    }
    
    default <T extends $Entity> Criteria criteria(Class<T> entityClass, Integer limit, Integer offset) {
        Criteria criteria = criteria( entityClass );
        
        limit(criteria, limit, offset);
        
        return criteria;
    }
    
    default <T extends $Entity> Criteria criteriaSingle(Class<T> entityClass) {
        return criteriaSingle(criteria(entityClass));
    }
    
    
    default <T extends $Entity> T findByProperty(Class<T> entityClass, String propertyName, Object value) {
        Criteria criteria = criteriaSingle(entityClass).add( Restrictions.eq(propertyName, value) );
        return listSingle(criteria);
    }
    default <T extends $Entity> List<T> findAllByProperty(Class<T> entityClass, String propertyName, Object value) {
        Criteria criteria = criteria(entityClass).add(Restrictions.eq(propertyName, value) );
        
        return list(criteria);
    }
    
    default <T extends $Entity> List<T> findAllInProperty(Class<T> entityClass, String property, Object ... values) {
        Criteria criteria = criteria(entityClass).add(
            Restrictions.in(property, values)
        );
        
        return list(criteria);
    }
    
    default <T extends $Entity> long count(Class<T> entityClass) {
        return count(criteria(entityClass));
    }
    
    default <T extends $Entity> boolean isEmpty(Class<T> entityClass) {
        return !Is.Ok(list(criteriaSingle(entityClass)));
    }
    
}
