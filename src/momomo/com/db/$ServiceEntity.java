/* Copyright(C) 2014 - 2020 Momomo LTD. Proprietary and confidential. Usage of this file on any medium without a written consent by Momomo LTD. is strictly prohibited. All Rights Reserved. */

package momomo.com.db;

import momomo.com.Reflects;
import momomo.com.db.entities.$Entity;
import momomo.com.db.entities.$EntityId;
import momomo.com.db.session.$SessionQuery;
import momomo.com.db.sessionfactory.$SessionFactoryRepositoryDeclaration;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Set;

public interface $ServiceEntity<T extends $EntityId> extends $SessionQuery, $SessionFactoryRepositoryDeclaration {

    default $EntityErrors validate(T entity) {
        return repository().validate(entity);
    }

    default $RepositoryValidation.ValidateSaveResult<T> validateSave(T entity) {
        return repository().validateSave(entity);
    }

    default $RepositoryValidation.ValidateSaveResult<T> validateSave(T entity, $EntityErrors errors) {
        return repository().validateSave(entity, errors);
    }


    default void validate(T entity, $EntityErrors errors) {
        repository().validate(entity, errors);
    }

    default void delete(T entity) {
        repository().delete(entity);
    }
    default void delete(T entity, boolean flush) {
        repository().delete(entity, flush);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
     * TODO
     *
     * Class<T> simply do not work in Java. A subclass can't call this method, discriminate(SubClassOfT.class).
     * Therefore we are only binding it on MoEEEntity, but really the intention is only subclasses of T.
     */
    default Disjunction discriminate(Class<? extends $Entity>... discriminators) {
        return repository().discriminate(discriminators);
    }

    default Disjunction discriminate(String... discriminators) {
        return repository().discriminate(discriminators);
    }

    default Disjunction or(String column, Object... values) {
        return repository().or(column, values);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~

    default T findByEntity(T entity) {
        return repository().findByEntity(entity);
    }
    default T findByEntity(T entity, Set<String> ignoreFields) {
        return repository().findByEntity(entity, ignoreFields);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~

    default boolean isNew(T entity) {
        return repository().isNew(entity);
    }

    default boolean isAssociation(Object obj) {
        return repository().isAssociation(obj);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~

    default T saveIfNew(T entity) {
        return repository().saveIfNew(entity);
    }

    default T save(T entity) {
        return repository().save(entity);
    }

    default T merge(T entity) {
        return repository().merge(entity);
    }

    default void delete(List<T> entities) {
        repository().delete(entities);
    }

    default void delete(List<T> entities, boolean flush) {
        repository().delete(entities, flush);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~

    default void refresh(T entity) {
        repository().refresh(entity);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~

    default Long count(Criteria criteria) {
        return repository().count(criteria);
    }

    default List<T> list(Criteria criteria) {
        return Reflects.cast(repository().list(criteria));
    }

    default Criteria criteria(Criteria criteria, String association) {
        return repository().criteria(criteria, association);
    }
    default T listSingle(Criteria criteria) {
        return repository().listSingle(criteria);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~

    default void limit(Criteria criteria, Integer limit, Integer offset) {
        repository().limit(criteria, limit, offset);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~
    @Override
    default List sqlList(String query) {
        return repository().sqlList(query);
    }
    @Override
    default int sqlUpdate(String query) {
        return repository().sqlUpdate(query);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    default Query sqlQuery(String query) {
        return repository().sqlQuery(query);
    }
    @Override
    default List sqlList(Query query) {
        return repository().sqlList(query);
    }
    @Override
    default int sqlUpdate(Query query) {
        return repository().sqlUpdate(query);
    }
    // ~~~~~~~~~~~~~~~~~~~~~~~~~
    
}
