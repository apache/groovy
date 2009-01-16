package org.codehaus.groovy.osgi

import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration;

/**
* This is the OSGi Activator for the Groovy example bundles. 
* Two things happen when the container starts this bundle: 
*   1) a message is printed to standard out
*   2) a service of type GroovyGreeter is added to the context 
* The service is unregistered when the bundle is stopped. 
* 
* @author Hamlet D'Arcy
*/ 
public class Activator implements BundleActivator {

    ServiceRegistration registration
  
    public void start(BundleContext context) {
        println "Groovy BundleActivator started"
  
        // Normally, the classloader code would not need to be run when 
        // adding a service to the context. However, this is required when
        // adding a Groovy service because of the way Groovy uses class 
        // loaders and reflection. 
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader()
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader())
            GroovyGreeter myService = new GroovyGreeterImpl()
            registration = context.registerService(GroovyGreeter.class.getName(), myService, null)
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader)
        }
    }
  
    public void stop(BundleContext context) {
        println "Groovy BundleActivator stopped"
        registration.unregister();
    }
}
