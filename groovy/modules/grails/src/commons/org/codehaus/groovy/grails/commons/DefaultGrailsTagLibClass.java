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
 * @author Graeme Rocher
 * @since 14-Jan-2006
 *
 * Default implementation of a tag lib class
 *
 */
public class DefaultGrailsTagLibClass extends AbstractInjectableGrailsClass implements GrailsTagLibClass {
    protected static final String TAG_LIB = "TagLib";
    private static final String APPLICATION_TAG_LIB = "Application";

    /**
     * <p>Default contructor
     *
     * @param clazz        the tag library class
     */
    public DefaultGrailsTagLibClass(Class clazz) {
        super(clazz, TAG_LIB);
    }

    public boolean supportsController(GrailsControllerClass controllerClass) {
        if(controllerClass == null)
            return false;
        if(getName().equals(APPLICATION_TAG_LIB))
            return true;
        else {
             if(getName().equals(controllerClass.getName()))
                return true;
        }
        return false;
    }
}
