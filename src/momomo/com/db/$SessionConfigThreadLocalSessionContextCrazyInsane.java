/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package momomo.com.db;

import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * This matches the default hibernate implementation step by step except for one particular case, 
 * It does not wrap the session into a very limited proxy on currentSession() calls. 
 * The limited proxy prevents us doing many many things and only implements a subset of method available on Session normally. 
 * No idea why this is wrapped in the first place and why someone would think it is neccessary, and from what. No reason to use it. 
 * Just crippling.   
 * 
 * Another part of it is that if you open a new session through bind which is likely very rare occurrence, 
 * then hiberante will just rollback the previous session on thread local and even eat any exceptions and just log them.
 * {@link $SessionConfigThreadLocalSessionContextCrazySane.InsanityLevel#CRAZY_INSANE} seemed fitting.
 *
 * We do not recommend this for our transaction API either. 
 *
 * Use {@link momomo.com.db.tmp.$ThreadLocalSessionContextUnwrappedStacked}
 * 
 * But our transaction API will make the neccessary checks to ensure you get the desired behaviour. 
 * But we can not workd with Hibernates implementation as it wrappes and limits access to the underlying session. 
 * 
 * If you inist on wanting to use  
 *   properties.put('hibernate.current_session_context_class', 'thread')
 *      or 
 *   properties.put('hibernate.current_session_context_class', 'org.hibernate.context.internal.ThreadLocalSessionContext')
 *   
 * we recommend you use our almost identical copy {@link $SessionConfigThreadLocalSessionContextCrazyInsane}
 * as it is identical in behaviour with only difference in that it keeps track of registered sessionfactories using it, 
 * as well not wrapping sessions at all. But really, dont.   
 *   
 * @author Joseph S.
 */
public class $SessionConfigThreadLocalSessionContextCrazyInsane extends $SessionConfigThreadLocalSessionContextCrazySane {
    public $SessionConfigThreadLocalSessionContextCrazyInsane(SessionFactoryImplementor factory) {
        super(factory, InsanityLevel.CRAZY_INSANE);
    }
}
