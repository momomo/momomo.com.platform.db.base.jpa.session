package momomo.com.db.session;

import momomo.com.db.$Repository;

/**
 * @author Joseph S.
 */
public interface $SessionRepository extends $Repository, $SessionPersistence, $SessionQuery, $SessionValidate, $SessionCriteria, $SessionNewRequire, $SessionOther, $SessionRepositoryDeclaration {

    @Override
    default $SessionRepository repository() {
        return this;
    }

}
