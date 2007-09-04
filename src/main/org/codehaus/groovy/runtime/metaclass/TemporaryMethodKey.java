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
package org.codehaus.groovy.runtime.metaclass;

import org.codehaus.groovy.runtime.MethodKey;
import org.codehaus.groovy.runtime.MetaClassHelper;


/**
 * A temporary implementation of MethodKey used to perform a fast lookup
 * for a method using a set of arguments to a method
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class TemporaryMethodKey extends MethodKey {

    private final Object[] parameterValues;

    public TemporaryMethodKey(Class sender, String name, Object[] parameterValues, boolean isCallToSuper) {
        super(sender, name, isCallToSuper);
        if (parameterValues == null) {
            parameterValues = MetaClassHelper.EMPTY_ARRAY;
        }
        this.parameterValues = parameterValues;
    }

    public int getParameterCount() {
        return parameterValues.length;
    }

    public Class getParameterType(int index) {
        Object value = parameterValues[index];

        if (value != null ) {
            Class type = (Class)((value.getClass() == java.lang.Class.class) ?
                    value :
                    value.getClass());
            return type;
        }

        return Object.class;
    }
}
