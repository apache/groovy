package org.codehaus.groovy.osgi.harness

import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration
import org.osgi.framework.ServiceReference
import org.codehaus.groovy.osgi.GroovyGreeter

/**
* This OSGi Activator finds all registered services of type GroovyGreeter
* and then invokes the sayHello() method on any that it finds.
* 
* @author Hamlet D'Arcy
*/ 
public class HarnessActivator implements BundleActivator {

    public void start(BundleContext context) {
        String serviceName = GroovyGreeter.class.getName()
        ServiceReference[] references = context.getAllServiceReferences(serviceName, null)
        
        println "${ references ? references.size() : 0 } GroovyGreeter services found."

        references?.each { ServiceReference ref -> 
            Object serviceHandle = context.getService(ref)
            GroovyGreeter service =(GroovyGreeter)serviceHandle
            service.sayHello()
        }
    }
	
    public void stop(BundleContext context) {
    }
}
