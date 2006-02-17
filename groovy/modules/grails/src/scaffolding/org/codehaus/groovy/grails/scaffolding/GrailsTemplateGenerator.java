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
package org.codehaus.groovy.grails.scaffolding;

import org.codehaus.groovy.grails.commons.GrailsDomainClass;

/**
 * An interface that defines methods for generating Grails artifacts from a domain class
 *
 * @author Graeme Rocher
 * @since 09-Feb-2006
 */
public interface GrailsTemplateGenerator {

     /**
     * Generates the necessary views for the supplied domain class
     * @param domainClass
     */
    void generateViews(GrailsDomainClass domainClass, String destDir);

    /**
     * Generates a controller for the supplied domain class
     * @param domainClass
     */
    void generateController(GrailsDomainClass domainClass, String destDir);

    /**
     * Whether the generator should overwrite existing files (defaults to false)
     *
     * @param shouldOverwrite
     */
    void setOverwrite(boolean shouldOverwrite);
}
