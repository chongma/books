package org.bookshop.application.test;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.service.DefaultInjectionPointService;

import javax.enterprise.inject.spi.Annotated;
import javax.persistence.PersistenceUnit;

// enables to consider fields without @inject as cdi bean when hsaInjection returns true
// used for @PersistenceUnit here
public class EEInjectionPointService extends DefaultInjectionPointService {
    public EEInjectionPointService(final WebBeansContext context) {
        super(context);
    }

    @Override
    public boolean hasInjection(final Annotated annotated) {
        return super.hasInjection(annotated) || annotated.isAnnotationPresent(PersistenceUnit.class);
    }
}
