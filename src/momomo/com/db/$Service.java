/* Copyright(C) 2014 - 2020 Momomo LTD. Proprietary and confidential. Usage of this file on any medium without a written consent by Momomo LTD. is strictly prohibited. All Rights Reserved. */

package momomo.com.db;

import momomo.com.Reflects;
import momomo.com.db.entities.$EntityId;
import momomo.com.db.sessionfactory.$SessionFactoryDeclaration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * @author Joseph S.
 */
public abstract class $Service<T extends $EntityId> implements $ServiceEntityClass<T>, $ServiceEntity<T>, $SessionFactoryDeclaration {
    public final Class<T> entityClass;
    
    /**
     * We can choose the automatic detection of the generic signature or
     */
    public $Service() {
        this.entityClass = Reflects.getGenericClass(this);
    }
    
    /**
     * We can supply the generic type directly from a subclass
     */
    public $Service(Class<T> entityClass) {
        this.entityClass = entityClass; 
    }
    
    /////////////////////////////////////////////////////////////////////
    
    /**
     * We implement the abstract method from {@link $ServiceEntityClass} 
     */
    @Override
    public final Class<T> entityClass() {
        return entityClass;
    }
    
    /////////////////////////////////////////////////////////////////////
    
    @Override
    public Session session() {
        return repository().session();
    }
    
    @Override
    public SessionFactory sessionFactory() {
        return repository().sessionFactory();   // We provide this implementation because one library service requires it, such as Hibernate where Spring does not. But no cost either way. 
    }
    
    /////////////////////////////////////////////////////////////////////
}
