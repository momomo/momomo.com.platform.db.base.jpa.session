package momomo.com.db;

import momomo.com.sources.RegexReplacor;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.SQLProjection;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.type.Type;

import java.util.regex.Pattern;

/**
 * @author Joseph S.
 */
public final class $CriteriaSqlProjection extends SQLProjection {
    private static final long serialVersionUID = 1L;
    private static final Pattern DOLLAR = Pattern.compile("\\$\\{(.*?)\\}");
    
    private String sql;
    private String groupBy;
    
    public $CriteriaSqlProjection(String sql ) {
        this(sql, null, new String[]{}, new Type[]{});
    }
    
    public $CriteriaSqlProjection(String sql, String[] columnAliases, Type[] types ) {
        this(sql, null, columnAliases, types);
    }
    
    public $CriteriaSqlProjection(String sql, String groupBy, String[] columnAliases, Type[] types ) {
        super(sql, groupBy, columnAliases, types);
        this.sql = sql;
        this.groupBy = groupBy;
    }
    
    @Override
    public String toSqlString(Criteria criteria, int loc, CriteriaQuery criteriaQuery ) throws HibernateException {
        return replaceDollars(sql, criteria, criteriaQuery);
    }
    
    @Override
    public String toGroupSqlString( Criteria criteria, CriteriaQuery criteriaQuery ) throws HibernateException {
        return replaceDollars(groupBy, criteria, criteriaQuery);
    }
    
    /**
     * Takes   : "distinct on(${mux}.polarization) active,channel,code,enabled,favorite,ilikeName,ilikeTags,keepAlive,mux,source,url";
     * Returns : "distinct on(tvmux2_.polarization) active,channel,code,enabled,favorite,ilikeName,ilikeTags,keepAlive,mux,source,url";
     */
    private String replaceDollars( String sql, Criteria criteria, CriteriaQuery criteriaQuery ) {
        return new RegexReplacor<RuntimeException>(DOLLAR, sql) {
            
            @Override
            public void match() throws RuntimeException {
                String alias = this.group(1);
                
                out.append(criteriaQuery.getSQLAlias(((CriteriaQueryTranslator) criteriaQuery).getCriteria(alias)));
            }
            
        }.go().toString();
    }
}
