/* Copyright(C) 2014 - 2020 Momomo LTD. Proprietary and confidential. Usage of this file on any medium without a written consent by Momomo LTD. is strictly prohibited. All Rights Reserved. */
package momomo.com.db.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class $EntityIdUUIDString implements $EntityId<String> {
    
    @Id
    @GeneratedValue(generator = "generator-name-is-uuid2")
    @GenericGenerator(name = "generator-name-is-uuid2", strategy = "uuid2")
    @Column(name = "uuid")
    private String id;
    
    @Override
    public String getId() {
        return id;
    }
    
    /************************ Just some notes on past tests and failures. ************************************
 
     http://stackoverflow.com/questions/6356834/using-hibernate-uuidgenerator-via-annotations
     
     @Id
     @GeneratedValue(generator="system-uuid")
     @GenericGenerator(name="system-uuid", strategy = "uuid")
     @Column(name = "uuid", unique = true)
 
     @Id
     @GeneratedValue(generator = "uuid2")
     @GenericGenerator(name = "uuid2", strategy = "uuid2")
     @Column(length = 36)
     private String uuid;
 
     @Override
     @Final public UUID getId() {
     return this.id;
     }
 
     @Override
     @Final public UUID setId(UUID id) {
     return this.id = id;
     }
 
     @Id
     @GeneratedValue(generator = "uuid2")
     @GenericGenerator(name = "uuid2", strategy = "uuid2")
     @Column(length = 36)
     private String id;
 
     @Id
     @GeneratedValue(generator = "mmm-uuid")
     @GenericGenerator(name = "mmm-uuid", strategy = "uuid")
     @Column(name = "uuid")
     private String id;
     
     **********************************************************************************************************/
    
}
