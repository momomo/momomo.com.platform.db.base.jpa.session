package momomo.com.db;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.spi.TypedValue;

/**
 * Also works on detached criterias.
 * 
 * @author Joseph S.
 */
public class $CriteriaLimit implements Criterion {
    private int limit;
    
    public $CriteriaLimit(int limit) {
        this.limit = limit;
    }
    
    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
        criteria.setMaxResults(limit); return "1 = 1";
    }
    
    @Override
    public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) {
        return new TypedValue[0];
    }
}
