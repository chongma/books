package org.bookshop.application.test;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Transactional;
import java.io.Serializable;

import static javax.transaction.Transactional.TxType.REQUIRED;

// was not sure how the persist was supposed to work in the original ex so did it with @Transactional
@Dependent
@Interceptor
@Transactional(REQUIRED)
@Priority(Interceptor.Priority.LIBRARY_AFTER)
public class LocalTransactionalInterceptor implements Serializable {
    @Inject
    private EntityManager entityManager;

    @AroundInvoke
    public Object withTransaction(final InvocationContext context) throws Exception {
        final EntityTransaction tx = entityManager.getTransaction();
        final boolean existing = tx.isActive();
        if (!existing) {
            tx.begin();
        }
        try {
            final Object result = context.proceed();
            if (!existing) {
                tx.commit();
            }
            return result;
        } catch (final Exception | Error ex) {
            tx.rollback();
            throw ex;
        }
    }
}
