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

package org.codehaus.groovy.runtime.iterator;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Iterator;

public class IteratorsOperations {
    public static TransformIterator plus(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "plus");
    }

    public static TransformIterator plus(Object self, final Iterator it) {
        return callMethodOnIterator(it, self, "plus");
    }

    public static TransformIterator minus(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "minus");
    }

    public static TransformIterator minus(Object self, final Iterator it) {
        return callMethodOnIterator(it, self, "minus");
    }

    public static TransformIterator multiply(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "multiply");
    }

    public static TransformIterator multiply(Object self, final Iterator it) {
        return callMethodOnIterator(it, self, "multiply");
    }

    public static TransformIterator power(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "power");
    }

    public static TransformIterator div(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "div");
    }

    public static TransformIterator div(Object self, final Iterator it) {
        return callMethodOnIterator(it, self, "div");
    }

    public static TransformIterator mod(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "mod");
    }

    public static TransformIterator mod(Object self, final Iterator it) {
        return callMethodOnIterator(it, self, "mod");
    }

    public static TransformIterator or(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "or");
    }

    public static TransformIterator or(Object self, final Iterator it) {
        return callMethodOnIterator(it, self, "or");
    }

    public static TransformIterator and(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "and");
    }

    public static TransformIterator and(Object self, final Iterator it) {
        return callMethodOnIterator(it, self, "and");
    }

    public static TransformIterator xor(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "xor");
    }

    public static TransformIterator xor(Object self, final Iterator it) {
        return callMethodOnIterator(it, self, "xor");
    }

    public static TransformIterator getAt(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "getAt");
    }

    public static TransformIterator getAt(Object self, final Iterator it) {
        return callMethodOnIterator(it, self, "getAt");
    }

    public static TransformIterator leftShift(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "leftShift");
    }

    public static TransformIterator leftShift(Object self, final Iterator it) {
        return callMethodOnIterator(it, self, "leftShift");
    }

    public static TransformIterator rightShift(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "rightShift");
    }

    public static TransformIterator rightShift(Object self, final Iterator it) {
        return callMethodOnIterator(it, self, "rightShift");
    }

    public static TransformIterator bitwiseNegate(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "bitwiseNegate");
    }

    public static TransformIterator unaryMinus(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "unaryMinus");
    }

    public static TransformIterator unaryPlus(Iterator self, final Object o) {
        return callIteratorMethod(self, o, "unaryPlus");
    }


    private static TransformIterator callIteratorMethod(Iterator self, final Object o, final String methodName) {
        if (o instanceof Closure) {
            final Closure closure = (Closure) ((Closure) o).clone();
            closure.setDelegate(self);
            return new TransformIterator(self, null, new TransformIterator.OneParamTransformer() {
                public Object transform(Object object, TransformIterator iter) {
                    return InvokerHelper.invokeMethod(object, methodName, closure.call(object));
                }
            });
        } else
            return new TransformIterator(self, null, new TransformIterator.OneParamTransformer() {
                public Object transform(Object object, TransformIterator iter) {
                    return InvokerHelper.invokeMethod(object, methodName, o);
                }
            });
    }


    private static TransformIterator callMethodOnIterator(Iterator self, final Object o, final String methodName) {
        if (o instanceof Closure) {
            final Closure closure = (Closure) ((Closure) o).clone();
            closure.setDelegate(self);
            return new TransformIterator(self, null, new TransformIterator.OneParamTransformer() {
                public Object transform(Object object, TransformIterator iter) {
                    return InvokerHelper.invokeMethod(closure.call(object), methodName, object);
                }
            });
        } else
            return new TransformIterator(self, null, new TransformIterator.OneParamTransformer() {
                public Object transform(Object object, TransformIterator iter) {
                    return InvokerHelper.invokeMethod(o, methodName, object);
                }
            });
    }
}
