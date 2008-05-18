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
package org.codehaus.groovy.runtime.callsite;

import groovy.lang.MetaClass;

/**
 * POJO call site
 *   meta class - cached
 *   method - not cached
 *
 * @author Alex Tkachman
*/
public class PojoMetaClassSite extends MetaClassSite{
    public PojoMetaClassSite(CallSite site, MetaClass metaClass) {
        super(site, metaClass);
    }

    public Object call(Object receiver, Object[] args) {
        if(checkCall(receiver))
          return metaClass.invokeMethod(receiver, name, args);
        else
          return CallSiteArray.defaultCall(this, receiver, args);
    }

    protected final boolean checkCall(Object receiver) {
        return receiver.getClass() == metaClass.getTheClass();
    }
}
