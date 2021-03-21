/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package momomo.com.db;

import org.hibernate.Session;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * Just disabled wrapping of the session, since it is a very limited session and can not be used for intelligent things.
 * 
 * We do not recommend hibernate standard handling (this) since it does not handle openSession() calls properly. \
 * Nagging about this exists throughout our code comments. 
 * 
 * If you really like Hibernates but would like our handling we recommend you use
 * {@link $SessionConfigThreadLocalSessionContextUnwrappedNotRecommended}
 * 
 * but really we recommend
 * 
 * {@link momomo.com.db.$SessionConfigThreadLocalSessionContextRecommended}
 * 
 * which uses a linked list to manage and remove sessions. 
 * 
 * @author Joseph S.
 */
public class $SessionConfigThreadLocalSessionContextUnwrappedNotRecommended extends ThreadLocalSessionContext {
    
    public $SessionConfigThreadLocalSessionContextUnwrappedNotRecommended(SessionFactoryImplementor factory) {
        super(factory);
    }
    
    @Override
    protected Session wrap(Session session) {
        return session;
    }
}
