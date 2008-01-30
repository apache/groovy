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

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 *
 * @author Alex Tkachman
 */
public class PogoMetaClassSite extends MetaClassSite {
    public PogoMetaClassSite(CallSite site, MetaClass metaClass) {
        super(site, metaClass);
    }

    public final Object invoke(Object receiver, Object[] args) {
        try {
            return metaClass.invokeMethod(receiver, name, args);
        } catch (MissingMethodException e) {
            GroovyObject groovy = (GroovyObject) receiver;
            if (e.getMethod().equals(name) && receiver.getClass() == e.getType()) {
                return groovy.invokeMethod(name, InvokerHelper.asUnwrappedArray(args));
            }
            throw e;
        }
    }

    public final CallSite acceptCall(Object receiver, Object[] args) {
        if (receiver instanceof GroovyObject && ((GroovyObject)receiver).getMetaClass() == metaClass)
          return this;
        else
          return createCallSite(receiver, args);
    }

    public final CallSite acceptCurrent(Object receiver, Object[] args) {
        if (receiver instanceof GroovyObject && ((GroovyObject)receiver).getMetaClass() == metaClass)
          return this;
        else
          return createCallCurrentSite(receiver, args, array.owner);
    }
}
