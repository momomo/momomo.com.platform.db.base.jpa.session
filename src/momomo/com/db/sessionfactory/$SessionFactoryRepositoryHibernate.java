package momomo.com.db.sessionfactory;

/**
 * To be used when Hibernate is being used rather than when Spring is in use.
 * @author Joseph S.
 */
public interface $SessionFactoryRepositoryHibernate extends $SessionFactoryRepository, $SessionFactoryRepositoryHibernateDeclaration {
    @Override
    default $SessionFactoryRepositoryHibernate repository() {
        return this;
    }
}
