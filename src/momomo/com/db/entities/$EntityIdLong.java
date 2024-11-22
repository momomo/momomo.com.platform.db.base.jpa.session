/* Copyright(C) 2014 - 2020 Momomo LTD. Proprietary and confidential. Usage of this file on any medium without a written consent by Momomo LTD. is strictly prohibited. All Rights Reserved. */
package momomo.com.db.entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * @author Joseph S.
 */
@MappedSuperclass 
public abstract class $EntityIdLong implements $EntityId<Long> {
    
    public static class Cons extends $EntityId.Cons {
        public static final String GENERATOR_ID       = "momomo.com.SequenceStyleGenerator";
        public static final String GENERATOR_STRATEGY = "org.hibernate.id.enhanced.SequenceStyleGenerator";
        public static final long   INDEX_START        = 1L;
    }
    
    @GenericGenerator(
        /**
         * This one works as GenerationType.IDENTITY but works for table per subclass inheritance as well!
        **/
        name     = Cons.GENERATOR_ID,
        strategy = Cons.GENERATOR_STRATEGY,
        parameters = {
            @Parameter(name = SequenceStyleGenerator.INITIAL_PARAM,                     value = "" + Cons.INDEX_START),
            @Parameter(name = SequenceStyleGenerator.CONFIG_PREFER_SEQUENCE_PER_ENTITY, value = "true"  ),
            @Parameter(name = SequenceStyleGenerator.OPT_PARAM,                         value = "hilo"  )
        }
    )
    @GeneratedValue(generator = Cons.GENERATOR_ID)
    @Column(name = Cons.id)
    @Id protected Long id;
    
    @Override public Long getId() {
        return this.id;
    }
    
    /************************ Just some notes on past tests and failures. ************************************

        @GenericGenerator(strategy = "uuid", name= MoEE_IDENTITY_GENERATOR)
        public static final String SHARED_SEQUENCE = "mo_sequence";
        
        http://alvinalexander.com/java/jwarehouse/hibernate/hibernate-core/src/matrix/java/org/hibernate/test/annotations/id/generationmappings/NewGeneratorMappingsTest.java.shtml
        @GenericGenerator(
            name = SHARED_SEQUENCE,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = SequenceStyleGenerator.INITIAL_PARAM,  value = "1000"),
                @Parameter(name = SequenceStyleGenerator.CONFIG_PREFER_SEQUENCE_PER_ENTITY, value="true" ),
                @Parameter(name = SequenceStyleGenerator.OPT_PARAM, value = "hilo")
            }
        )
        @Id
        @GeneratedValue(generator = SHARED_SEQUENCE)
        private long id;
        
        // http://www.postgresql.org/docs/9.1/static/sql-altersequence.html
        
        @GeneratedValue(strategy = GenerationType.IDENTITY) doesn't work with table per subclass, why we override it here!
        
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        public Long getId() {
            return super.getId();
        }
        
        // All generators: http://docs.jboss.org/hibernate/core/3.6/reference/en-US/html/mapping.html#d0e5294
        
        http://stackoverflow.com/questions/6356834/using-hibernate-uuidgenerator-via-annotations
        @Id
        @GeneratedValue(generator="system-uuid")
        @GenericGenerator(name="system-uuid", strategy = "uuid")
        @Column(name = "uuid", unique = true)
        private String uuid;
     **********************************************************************************************************/
}
