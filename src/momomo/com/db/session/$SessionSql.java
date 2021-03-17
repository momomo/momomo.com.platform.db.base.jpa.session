package momomo.com.db.session;

import momomo.com.Lambda;
import momomo.com.Reflects;
import momomo.com.db.$Sql;
import momomo.com.db.$SqlQuery;
import momomo.com.db.$SqlResultSet;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.internal.ScrollableResultsImpl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Map;

/**
 * @author Joseph S.
 */
public abstract class $SessionSql extends $Sql<$SessionSql> implements $SqlQuery, $SessionRepositoryDeclaration {
    
    @Override
    public <R, E extends Exception> R query(Lambda.R1E<R, $SqlResultSet, E> lambda) throws E {
        org.hibernate.query.Query query = repository().sqlQuery(super.get());
        
        setNamedParameters(query);
        
        try (ScrollableResults scroll = query.scroll(ScrollMode.FORWARD_ONLY); $SqlResultSet rs = query(scroll); ) {
            return lambda.call( rs );
        }
    }
    
    private void setNamedParameters(org.hibernate.query.Query query) {
        for (Map.Entry<String, Val> entry : VALS.entrySet()) {
            query.setParameter( entry.getKey(), entry.getValue().val );
        }
    }
    
    /**
     * Uses reflection. Might become probelematic in the future when Hibernate version > 5
     */
    private static $SqlResultSet query(ScrollableResults scroll) {
        Field field  = Reflects.getField(ScrollableResultsImpl.class, "resultSet");
        
        return new $SqlResultSet( (ResultSet) Reflects.getValue(scroll, field) );
    }
    
}
