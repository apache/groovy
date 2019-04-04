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
package org.codehaus.groovy.classgen

// TODO this generator template has drifted apart from the now modified generated classes
// Is it worth keeping this template and/or getting it back up to date?

println """
package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;

public class ArrayOperations {
  ${genInners()}
}
"""

def genInners () {
    def res = ''

    final Map primitives = [
            'boolean': 'Boolean',
            'byte': 'Byte',
            'char': 'Character',
            'short': 'Short',
            'int': 'Integer',
            'long': 'Long',
            'float': 'Float',
            'double': 'Double'
    ]

    primitives.each {primName, clsName ->
        res += """
         public static class ${clsName}ArrayGetAtMetaMethod extends ArrayGetAtMetaMethod {
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(${primName}[].class);

            public Class getReturnType() {
                return ${clsName}.class;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final ${primName}[] objects = (${primName}[]) object;
                return objects[normaliseIndex((Integer) args[0], objects.length)];
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object invoke(Object receiver, Object[] args) {
                            final ${primName}[] objects = (${primName}[]) receiver;
                            return objects[normaliseIndex((Integer) args[0], objects.length)];
                        }

                        public Object callBinop(Object receiver, Object arg) {
                            if ((receiver instanceof ${primName}[] && arg instanceof Integer)
                                    && checkMetaClass()) {
                                final ${primName}[] objects = (${primName}[]) receiver;
                                return objects[normaliseIndex((Integer) arg, objects.length)];
                            }
                            else
                              return super.callBinop(receiver,arg);
                        }

                        public Object invokeBinop(Object receiver, Object arg) {
                            final ${primName}[] objects = (${primName}[]) receiver;
                            return objects[normaliseIndex((Integer) arg, objects.length)];
                        }
                    };
            }
         }


        public static class ${clsName}ArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
            private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
            private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(${primName}[].class);
            private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

            public ${clsName}ArrayPutAtMetaMethod() {
                parameterTypes = PARAM_CLASS_ARR;
            }

            public final CachedClass getDeclaringClass() {
                return ARR_CLASS;
            }

            public Object invoke(Object object, Object[] args) {
                final ${primName}[] objects = (${primName}[]) object;
                final int index = normaliseIndex((Integer) args[0], objects.length);
                Object newValue = args[1];
                if (!(newValue instanceof ${clsName})) {
                    Number n = (Number) newValue;
                    objects[index] = ((Number)newValue).${primName}Value();
                }
                else
                  objects[index] = ((${clsName})args[1]).${primName}Value();
                return null;
            }

            public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
                if (!(args [0] instanceof Integer) || !(args [1] instanceof ${clsName}))
                  return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
                else
                    return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                        public Object call(Object receiver, Object[] args) {
                            if ((receiver instanceof ${primName}[] && args[0] instanceof Integer && args[1] instanceof ${clsName} )
                                    && checkMetaClass()) {
                                final ${primName}[] objects = (${primName}[]) receiver;
                                objects[normaliseIndex((Integer) args[0], objects.length)] = ((${clsName})args[1]).${primName}Value();
                                return null;
                            }
                            else
                              return super.call(receiver,args);
                        }
                    };
            }
        }

       """
    }

    res
}
