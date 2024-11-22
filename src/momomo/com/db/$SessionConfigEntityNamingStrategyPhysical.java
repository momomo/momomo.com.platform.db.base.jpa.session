package momomo.com.db;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Allows us to perform "surgery" on a column name as it is being generated for hibernate entity tables.
 * 
 * @author Joseph S.
 */
public class $SessionConfigEntityNamingStrategyPhysical extends PhysicalNamingStrategyStandardImpl implements Serializable {

    public static final String             UNDERSCORE = "_";
    public static final ArrayList<Surgery> SURGERIES  = new ArrayList<>();

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment context) {

        // MOMOMO.COM
        String name = identifier.getText();
        for (Surgery surgery : SURGERIES) {
            if ( surgery.matches(name) ) {
                identifier = new Identifier(surgery.operate(name), identifier.isQuoted()); break;
            }
        }

        return super.toPhysicalColumnName(identifier, context);
    }

    public abstract static class Surgery {
        protected boolean matches(String name) {
            return false;
        }

        protected String operate(String name) {
            return name;
        }
    }

}