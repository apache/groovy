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

package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.MetaClass;

import java.util.ArrayList;
import java.util.List;

/**
 * A closure which stores calls in a List so that method calls 
 * can be iterated over in a 'yield' style way
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class IteratorClosureAdapter<T> extends Closure {

    private final List<T> list = new ArrayList<T>();
    private MetaClass metaClass = InvokerHelper.getMetaClass(getClass());
    
    public IteratorClosureAdapter(Object delegate) {
        super(delegate);
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }
    
    public List<T> asList() {
        return list;
    }

    protected Object doCall(T argument) {
        list.add(argument);
        return null;
    }
}
