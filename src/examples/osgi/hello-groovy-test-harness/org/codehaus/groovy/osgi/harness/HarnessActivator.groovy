/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            GroovyGreeter service = (GroovyGreeter) serviceHandle
            service.sayHello()
        }
    }

    public void stop(BundleContext context) {
    }
}
