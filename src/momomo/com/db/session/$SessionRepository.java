package momomo.com.db.session;

import momomo.com.db.$Repository;

/**
 * @author Joseph S.
 */
public interface $SessionRepository extends $Repository, $SessionPersistence, $SessionQuery, $SessionValidate, $SessionCriteria, $SessionNewAndRequire, $SessionOther, $SessionRepositoryDeclaration {

    @Override
    default $SessionRepository repository() {
        return this;
    }

}
