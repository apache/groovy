/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;

/**
 * Wrapper for a method for a property get or set.
 * WARNING: This class is for internal use only, don't use it for your APIs
 */
public class MethodMetaProperty extends MetaProperty {
    private final MetaMethod method;
    
    public MethodMetaProperty(String name, MetaMethod method) {
        super(name, Object.class);
        this.method = method;
    }

    @Override
    public Object getProperty(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProperty(Object object, Object newValue) {
        throw new UnsupportedOperationException();
    }
    
    public MetaMethod getMetaMethod() {
        return method;
    }
    
    /**
     * Wrapper for a method realizing the property get.
     * WARNING: This class is for internal use only, don't use it for your APIs
     */
    public static class GetMethodMetaProperty extends MethodMetaProperty {

        public GetMethodMetaProperty(String name, MetaMethod theMethod) {
            super(name, theMethod);
        }

        @Override
        public Object getProperty(Object object) {
            return getMetaMethod().doMethodInvoke(object, new Object[]{name});
        }
    }
    
    /**
     * Wrapper for a method realizing the property getter.
     * WARNING: This class is for internal use only, don't use it for your APIs
     */
    public static class GetBeanMethodMetaProperty extends MethodMetaProperty {
        public GetBeanMethodMetaProperty(String name, MetaMethod theMethod) {
            super(name, theMethod);
        }

        @Override
        public Object getProperty(Object object) {
            return getMetaMethod().doMethodInvoke(object, MetaClassImpl.EMPTY_ARGUMENTS);
        }
    }

}
