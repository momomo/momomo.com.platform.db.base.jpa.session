package momomo.com.db;

import momomo.com.Reflects;
import momomo.com.reflection.Reflect;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * @author Joseph S.
 */
public interface $Criteria {
    
    /**
     * Creates a sub criteria
     */
    default Criteria criteria(Criteria criteria, String association) {
        return criteria.createCriteria(association, association);
    }
    
    default Criteria criteriaSingle(Criteria criteria) {
        criteria.setMaxResults( 1 );
        criteria.setFetchSize ( 1 );
        return criteria;
    }
    
    default long count(Criteria criteria) {
        return (long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }
    
    // OBS! Note! When delegated should return List<T> where T should be ? extends $Entity, TODO revise
    default List list(Criteria criteria) {
        criteria.setCacheMode(CacheMode.NORMAL); return criteria.list();
    }
    default <T extends $Entity> T listSingle(Criteria criteria) {
        if ( criteria != null ) {
            List list = list(criteria);
            if (list.size() > 0) {
                return Reflects.cast( list.get(0) );
            }
        }
        return null;
    }
    
    default void limit(Criteria criteria, Integer limit, Integer offset) {
        if ( limit != null ) {
            criteria.setMaxResults  ( limit );
        }
        else if ( offset != null ) {
            limit = 0;
        }
        
        // Limit is guranteed to be set here
        if ( offset != null ) {
            criteria.setFirstResult (offset * limit);     // Unfortunately setFirstResult expects an int instead of a long
        }
    }
    
    default Disjunction or(String column, Object... values) {
        Criterion[] criterions = new Criterion[values.length];
        
        int i = -1; while ( ++i < values.length ) {
            criterions[i] = Restrictions.eq( column, values[i] );
        }
        
        return Restrictions.or(criterions);
    }
    
    default Disjunction discriminate(Class<? extends $Entity> ... discriminators) {
        return discriminate((Object[]) discriminators);
    }
    
    default Disjunction discriminate(String ... discriminators) {
        return discriminate((Object[]) discriminators);
    }
    
    private Disjunction discriminate(Object[] discriminators) {
        Criterion[] expressions = new Criterion[discriminators.length];
        
        int i = -1; while ( ++i < discriminators.length ) {
            expressions[i] = Restrictions.eq($Entity.Cons.DISCRIMINATOR_COLUMN, discriminators[i]);
        }
        
        return Restrictions.or(expressions);
    }
    
}
