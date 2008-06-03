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

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A useful base class for Java objects wishing to be Groovy objects
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class GroovyObjectSupport implements GroovyObject {

	// never persist the MetaClass
    private transient MetaClass metaClass;

    public GroovyObjectSupport() {
        this.metaClass = InvokerHelper.getMetaClass(this.getClass());
    }
    
    public Object getProperty(String property) {
        return getMetaClass().getProperty(this, property);
    }

    public void setProperty(String property, Object newValue) {
         getMetaClass().setProperty(this, property, newValue);
    }

    public Object invokeMethod(String name, Object args) {
        return getMetaClass().invokeMethod(this, name, args);
    }
    
    public MetaClass getMetaClass() {
        if (metaClass==null) {
            metaClass = InvokerHelper.getMetaClass(getClass());
        }
        return metaClass;
    }
    
    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }
}
