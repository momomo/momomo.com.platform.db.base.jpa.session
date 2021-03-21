/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package momomo.com.db;

import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * This version ignores any potentially "old" sessions when new one is replacing it on the threadlocal. 
 * 
 * This seems more fitting than rolling it back, since the user might still manually handle the session themselves.
 * 
 * We however do not recommend this for our transaction API either. 
 * 
 * Use {@link momomo.com.db.tmp.$ThreadLocalSessionContextUnwrappedStacked}
 * 
 * @author Joseph S.
 */
public class $SessionConfigThreadLocalSessionContextCrazyLaxed extends $SessionConfigThreadLocalSessionContextCrazySane {
    public $SessionConfigThreadLocalSessionContextCrazyLaxed(SessionFactoryImplementor factory) {
        super( factory, InsanityLevel.CRAZY_EASYGOING);
    }
}
