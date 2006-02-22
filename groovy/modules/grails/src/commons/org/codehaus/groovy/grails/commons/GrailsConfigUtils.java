/* Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.commons;

import org.codehaus.groovy.grails.scaffolding.GrailsScaffolder;
import org.codehaus.groovy.grails.scaffolding.ScaffoldDomain;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * A common class where shared configurational methods can reside
 *
 * @author Graeme Rocher
 * @since 22-Feb-2006
 */
public class GrailsConfigUtils {

    public static void configureScaffolders(GrailsApplication application, ApplicationContext appContext) {
        GrailsControllerClass[] controllerClasses = application.getControllers();
        for (int i = 0; i < controllerClasses.length; i++) {
            GrailsControllerClass controllerClass = controllerClasses[i];
            if(controllerClass.isScaffolding()) {
                try {
                    GrailsScaffolder gs = (GrailsScaffolder)appContext.getBean(controllerClass.getFullName() + "Scaffolder");
                    if(gs != null) {
                        ScaffoldDomain sd = gs.getScaffoldRequestHandler()
                                                .getScaffoldDomain();

                        GrailsDomainClass dc = application.getGrailsDomainClass(sd.getPersistentClass().getName());
                        if(dc != null) {
                            sd.setIdentityPropertyName(dc.getIdentifier().getName());
                            sd.setValidator(dc.getValidator());
                        }
                    }
                } catch (NoSuchBeanDefinitionException e) {
                    // ignore
                }
            }
        }
    }
}
