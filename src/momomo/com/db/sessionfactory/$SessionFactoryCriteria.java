package momomo.com.db.sessionfactory;

import momomo.com.Is;
import momomo.com.Reflects;
import momomo.com.annotations.$Exclude;
import momomo.com.db.entities.$Entity;
import momomo.com.db.session.$SessionCriteria;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Joseph S.
 */
public interface $SessionFactoryCriteria extends $SessionCriteria, $SessionFactoryRepositoryDeclaration {

    // TODO add a way to pass exclusion keys
    default <T extends $Entity> T findByEntity(T entity, Set<String> ignoreFields) {
        Criteria criteria = buildCriteriaObject(entity, ignoreFields);
        if (Is.Ok(criteria)) {
            return listSingle(criteriaSingle(criteria));
        }
        return null;
    }

    default <T extends $Entity> T findByEntity(T entity) {
        return findByEntity(entity, null);
    }

    /**
     * Much more dynamic than findByProperty in that field can be channel.name for instance, and value can be an entity or a value
     **/
    default <T extends $Entity> T findByField(Class<T> entityClass, String field, Object value) {
        Criteria criteria = criteriaSingle(entityClass);
        if (buildCriteriaField(criteria, null, field, value)) {
            return listSingle(criteria);
        }
        return null;
    }

    default <T extends $Entity> List<T> findAllInProperty(Class<T> entityClass, String property, List<Object> values) {
        return findAllByProperty(entityClass, property, values.toArray());
    }

    private Criteria buildCriteriaObject(Object object, Set<String> ignoreFields) {
        Class<? extends $Entity> klass = Reflects.cast(object.getClass());
        Criteria baseCriteria = criteria(klass);

        ClassMetadata metadata = repository().metadata(object);
        if (metadata.hasIdentifierProperty()) {
            String id = metadata.getIdentifierPropertyName();

            if (id != null) {

                if (ignoreFields == null || !ignoreFields.contains(id)) {

                    @SuppressWarnings("deprecation") Serializable val = metadata.getIdentifier(object);
                    if (val != null) {
                        return baseCriteria.add(
                                Restrictions.eq(id, val)
                        );
                    }

                }
            }
        }

        if (buildCriteriaFields(baseCriteria, null, object, ignoreFields)) {
            return baseCriteria;
        }
        return null;
    }

    private Criteria buildCriteriaObject(Object object) {
        return buildCriteriaObject(object, null);
    }

    private boolean buildCriteriaFields(Criteria criteria, String embedded, Object object, Set<String> ignoreFields) {
        boolean valid = false;

        Class<?>    clazz  = object.getClass();
        List<Field> fields = Reflects.getFields(clazz);

        // Walk over all fields for the value class to look for properties that are set
        for (Field field : fields) {

            if (ignoreFields == null || !ignoreFields.contains(field.getName())) {

                Object value = Reflects.getValue(object, field);

                // If set, then use this value in the criteria recursively
                if (value != null && !Reflects.isStatic(field) && !$Exclude.$.has(field, $Entity.Cons.FIND_BY_ENTITY) && !field.isAnnotationPresent(javax.persistence.Version.class)) {

                    if (buildCriteriaField(criteria, embedded, field.getName(), value)) {
                        valid = true;  // One valid is enough to set to true
                    }

                }
            }

        }

        return valid;
    }

    /**
     * This one build a criteria recursively for all set properties on an entity instance and all its sub instances
     * If this value lacks an id, all properties on that value will be used recursively to build a full query.
     * 
     * TODO When building the criteria query, look first for unique fields, suchs @Column(unique=true) to build using only that field without using more field or deeper nesting.
     * TODO This comment is about buildCriteria, which should therefore be faster, but no need to optimize this part quite yet
     * TODO Look for annotation that says that field should be excluded from deep search
     */
    private boolean buildCriteriaField(Criteria criteria, String embedded, String name, Object val) {

        if (repository().isAssociation(val)) {

            ClassMetadata metadata = repository().metadata(val);
            if (metadata.hasIdentifierProperty()) {
                String id = metadata.getIdentifierPropertyName();

                if (id != null) {
                    @SuppressWarnings("deprecation") Serializable v = metadata.getIdentifier(val);
                    if (v != null) {
                        criteria(criteria, name).add(
                                Restrictions.eq(id, v)
                        );

                        return true;
                    }
                }
            }

            return buildCriteriaFields(criteria(criteria, name), null, val, null);
        } else if (repository().isEmbedded(val)) {
            return buildCriteriaFields(criteria, name, val, null);
        }
        else if (embedded != null) {
            criteria.add(Restrictions.eq(embedded + "." + name, val)); return true;

        } else if (val instanceof Map) {
            boolean valid = false;

            for (Map.Entry entry : ((Map<?, ?>) val).entrySet()) {
                if (buildCriteriaField(criteria(criteria, entry.getKey().toString()), null, entry.getKey().toString(), entry.getValue())) {
                    valid = true;
                }
            }

            return valid;
        } else if (val instanceof Collection) {
            if (Is.Ok(val)) {
                // TODO
                                /*valid = true;
                                criteria.add( Restrictions.in(propertyName,(Collection) propertyValue) );*/
            }

            return false;
        } else {
            String[] split = name.split(Pattern.quote("."), 2);

            if (split.length > 1) {
                return buildCriteriaField(criteria(criteria, split[0]), null, split[1], val);
            } else {
                criteria.add(Restrictions.eq(name, val));
                return true;
            }
        }
    }

}
