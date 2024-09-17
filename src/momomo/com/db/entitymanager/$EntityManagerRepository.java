package momomo.com.db.entitymanager;

import momomo.com.db.sessionfactory.$SessionFactoryRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.EntityManager;

/**
 * We do not really use the EntityManager or Spring anymore, so the old functionaltiy that relied on Spring and EntityManager was made generic by basing it on the underlying sessionfactory which our core libraries instead uses. To get to the SessionFactory and Session we instead unwrap the EntityManager that Spring would normally inject for us, and needs to inject itself in order for thing to work. 
 * 
 * We have yet to discover a way to inject the SessionFactory or Session rather than the EntityManager when using Spring as @PersistenceContext(name="...") will get us an EntityManger while @PersistenceUnit(name="") will get us the EntityManagerFactory but that in addition does not work well. 
 * 
 * Injection the SessionFactory using @Resource has the same effect as Injecting using @PersistenceUnit. We get an instance, but it is incapable of saving to the database, while at the same not throwing any exceptions for such failures. 
 * 
 * If you know how to inject a capable Session rather than EntityManager, then we would not need the unwrapping below. Likely the injected EntityManager is able to communicate with the PlatformTransactionManager while the other can't. Spring would likely set up some sync between those two.   
 * 
 * @author Joseph S.
 */
public interface $EntityManagerRepository extends $SessionFactoryRepository, $EntityManagerRepositoryDeclaration {

    EntityManager entityManager();
    
    @Override
    default Session session() {
        return entityManager().unwrap(Session.class);
    }
    
    @Override
    default SessionFactory sessionFactory() {
        return entityManager().getEntityManagerFactory().unwrap(SessionFactory.class);
    }

    @Override
    default $EntityManagerRepository repository() {
        return this;
    }
}
