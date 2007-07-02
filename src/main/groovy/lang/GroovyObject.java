/*
 * Copyright 2003-2007 the original author or authors.
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

package groovy.lang;

/**
 * The interface implemented by all Groovy objects.
 *
 * Especially handy for using Groovy objects when in the Java world.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public interface GroovyObject {

    /** 
     * Invokes the given method
     */
    Object invokeMethod(String name, Object args);
    
    /**
     * @return the given property
     */
    Object getProperty(String property);

    /**
     * Sets the given property to the new value
     */
    void setProperty(String property, Object newValue);
        
    /**
     * @return the metaClass of this instance
     */
    MetaClass getMetaClass();
    
    /**
     * Allows the MetaClass to be replaced with a derived implementation
     */
    void setMetaClass(MetaClass metaClass);
}
