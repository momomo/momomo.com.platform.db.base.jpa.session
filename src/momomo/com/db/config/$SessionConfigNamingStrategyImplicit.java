package momomo.com.db.config;

/**
 * An ImplicitNamingStrategy implementation which uses full composite paths
 * extracted from AttributePath, as opposed to just the terminal property part.
 *
 * Mainly a port of the older DefaultComponentSafeNamingStrategy class implementing
 * the no longer supported NamingStrategy contract
 * 
 * Just for easier reference as evidenced. 
 *
 * @author Joseph S.
 */
public class $SessionConfigNamingStrategyImplicit extends org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl {

}
