/*
 * Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.servlet;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsBootstrapClass;
import org.codehaus.groovy.grails.commons.GrailsConfigUtils;
import org.codehaus.groovy.grails.commons.spring.SpringConfig;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springmodules.beans.factory.drivers.xml.XmlWebApplicationContextDriver;

/**
 * <p>Servlet that handles incoming requests for Grails.
 * <p/>
 * <p>This servlet loads the Spring configuration based on the Grails application
 * in the parent application context.
 *
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class GrailsDispatcherServlet extends DispatcherServlet {

    public GrailsDispatcherServlet() {
        super();
        setDetectAllHandlerMappings(false);
    }

    protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) throws BeansException {
        // use config file locations if available
        getServletContext().setAttribute(GrailsApplicationAttributes.PARENT_APPLICATION_CONTEXT,parent);
        ApplicationContext grailsContext = (ApplicationContext)getServletContext().getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT);
        GrailsApplication application;
        WebApplicationContext webContext;

        if(grailsContext != null) {
            XmlWebApplicationContext xmlContext = new XmlWebApplicationContext();
            xmlContext.setParent(grailsContext);
            webContext = xmlContext;
            application = (GrailsApplication) webContext.getBean(GrailsApplication.APPLICATION_ID, GrailsApplication.class);

        }
        else {
            String[] locations = null;
            if (null != getContextConfigLocation()) {
                locations = StringUtils.tokenizeToStringArray(
                        getContextConfigLocation(),
                        ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS);
            }
            // construct the SpringConfig for the container managed application
            application = (GrailsApplication) parent.getBean(GrailsApplication.APPLICATION_ID, GrailsApplication.class);
            SpringConfig springConfig = new SpringConfig(application);
            // return a context that obeys grails' settings
            webContext = new XmlWebApplicationContextDriver().getWebApplicationContext(
                    springConfig.getBeanReferences(),
                    parent,
                    getServletContext(),
                    getNamespace(),
                    locations);
            getServletContext().setAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT,webContext );
        }

        // configure scaffolders
        GrailsConfigUtils.configureScaffolders(application, webContext);

        SessionFactory sessionFactory = (SessionFactory)webContext.getBean("sessionFactory");

        if(sessionFactory != null) {
            Session session = null;
            boolean participate = false;
            // single session mode
            if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
                // Do not modify the Session: just set the participate flag.
                participate = true;
            }
            else {
                logger.debug("Opening single Hibernate session in GrailsDispatcherServlet");
                session = SessionFactoryUtils.getSession(sessionFactory,true);
                session.setFlushMode(FlushMode.AUTO);
                TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
            }
            // init the Grails application
            try {
                GrailsBootstrapClass[] bootstraps =  application.getGrailsBootstrapClasses();
                for (int i = 0; i < bootstraps.length; i++) {
                    bootstraps[i].callInit(  getServletContext() );
                }
            }

            finally {
                if (!participate) {
                    // single session mode
                    TransactionSynchronizationManager.unbindResource(sessionFactory);
                    logger.debug("Closing single Hibernate session in GrailsDispatcherServlet");
                    try {
                        SessionFactoryUtils.releaseSession(session, sessionFactory);
                    }
                    catch (RuntimeException ex) {
                        logger.error("Unexpected exception on closing Hibernate Session", ex);
                    }
                }
            }
            
        }

        return webContext;
    }

    public void destroy() {
        WebApplicationContext webContext = getWebApplicationContext();
        GrailsApplication application = (GrailsApplication) webContext.getBean(GrailsApplication.APPLICATION_ID, GrailsApplication.class);

        GrailsBootstrapClass[] bootstraps =  application.getGrailsBootstrapClasses();
        for (int i = 0; i < bootstraps.length; i++) {
            bootstraps[i].callDestroy();
        }
        // call super
        super.destroy();
    }

}
