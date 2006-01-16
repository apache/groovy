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

/**
 * <p>Represents a Grails tab library class</p>
 *
 * @author Graeme Rocher
 * @since Jan 14, 2006
 */
public interface GrailsTagLibClass extends GrailsClass {

    /**
     * The name of the application (ie global) tag library appropriate for all controller classes
     */
    String APPLICATION_TAG_LIB = "ApplicationTagLib";
    String REQUEST_TAG_LIB = "grailsRequestTagLib";

    /**
     * Whether this tag library supports the specified controller
     * @param controllerClass The controllerClass to check
     * @return True if the controller is supported
     */
    boolean supportsController(GrailsControllerClass controllerClass);
}
