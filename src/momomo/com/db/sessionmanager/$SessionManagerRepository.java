package momomo.com.db.sessionmanager;

import momomo.com.db.sessionfactory.$SessionFactoryRepository;

/**
 * Basically the same as {@link momomo.com.db.sessionfactory.$SessionFactoryRepository} but with some things modified with regards to the session. 
 * @author Joseph S.
 */
public interface $SessionManagerRepository extends $SessionFactoryRepository, $SessionManagerNewRequire, $SessionManagerRepositoryDeclaration {

    @Override
    default $SessionManagerRepository repository() {
        return this;
    }
    
}
