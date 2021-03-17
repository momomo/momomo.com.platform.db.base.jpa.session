package momomo.com.db.session;

import momomo.com.db.$SqlOperations;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * @author Joseph S.
 */
public interface $SessionQuery extends $SqlOperations, $SessionRepositoryDeclaration {
    
    /////////////////////////////////////////////////////////////////////
    
    private Session session() {
        return repository().session();
    }
    
    /////////////////////////////////////////////////////////////////////
    
    @Override
    default List sqlList(String query) {
        return sqlList(sqlQuery(query));
    }
    @Override
    default int sqlUpdate(String query) {
        return sqlUpdate(sqlQuery(query));
    }
    
    /////////////////////////////////////////////////////////////////////
    
    default Query sqlQuery(String query) {
        return session().createNativeQuery(query);
    }
    
    default List sqlList(Query query) {
        return query.list();
    }
    
    default int sqlUpdate(Query query) {
        return query.executeUpdate();
    }
    
    /////////////////////////////////////////////////////////////////////
    
}
