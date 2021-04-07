package momomo.com.db.sessionmanager;

import momomo.com.db.sessionfactory.$SessionFactoryRepository;

/**
 * @author Joseph S.
 */
public interface $SessionManagerRepository extends $SessionFactoryRepository, $SessionManagerNewRequire, $SessionManagerRepositoryDeclaration {

    @Override
    default $SessionManagerRepository repository() {
        return this;
    }
    
}
