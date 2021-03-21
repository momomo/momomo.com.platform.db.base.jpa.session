/* Copyright(C) 2014 - 2020 Momomo LTD. Proprietary and confidential. Usage of this file on any medium without a written consent by Momomo LTD. is strictly prohibited. All Rights Reserved. */

package momomo.com.db;

import momomo.com.Reflects;
import momomo.com.db.entities.$EntityId;

/**
 * @author Joseph S.
 */
public abstract class $Service<T extends $EntityId> implements $ServiceEntityClass<T>, $ServiceEntity<T> {
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
}
