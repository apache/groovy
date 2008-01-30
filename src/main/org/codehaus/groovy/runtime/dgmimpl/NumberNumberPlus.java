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
package org.codehaus.groovy.runtime.dgmimpl;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.typehandling.NumberMath;

public class NumberNumberPlus extends NumberNumberMetaMethod {
    public String getName() {
        return "plus";
    }

    public Object invoke(Object object, Object[] arguments) {
        return NumberMath.add((Number) object, (Number) arguments[0]);
    }

    /**
     * Add two numbers and return the result.
     *
     * @param left  a Number
     * @param right another Number to add
     * @return the addition of both Numbers
     */
    public static Number plus(Number left, Number right) {
        return NumberMath.add(left, right);
    }

    public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        if (receiver instanceof Integer) {
            if (args[0] instanceof Integer)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Integer(((Integer) receiver).intValue() + ((Integer) args[0]).intValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Integer(((Integer) receiver).intValue() + ((Integer) arg).intValue());
                    }
                };

            if (args[0] instanceof Long)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Long(((Integer) receiver).longValue() + ((Long) args[0]).longValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Long(((Integer) receiver).longValue() + ((Long) arg).longValue());
                    }
                };

            if (args[0] instanceof Float)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Integer) receiver).doubleValue() + ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Integer) receiver).doubleValue() + ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Integer) receiver).doubleValue() + ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Integer) receiver).doubleValue() + ((Double) arg).doubleValue());
                    }
                };
            }

        if (receiver instanceof Long) {
            if (args[0] instanceof Integer)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Long(((Long) receiver).longValue() + ((Integer) args[0]).longValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Long(((Long) receiver).longValue() + ((Integer) arg).longValue());
                    }
                };

            if (args[0] instanceof Long)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Long(((Long) receiver).longValue() + ((Long) args[0]).longValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Long(((Long) receiver).longValue() + ((Long) arg).longValue());
                    }
                };

            if (args[0] instanceof Float)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Long) receiver).doubleValue() + ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Long) receiver).doubleValue() + ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Long) receiver).doubleValue() + ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Long) receiver).doubleValue() + ((Double) arg).doubleValue());
                    }
                };
            }

        if (receiver instanceof Float) {
            if (args[0] instanceof Integer)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() + ((Integer) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() + ((Integer) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Long)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() + ((Long) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() + ((Long) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Float)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() + ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() + ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Float) receiver).doubleValue() + ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Float) receiver).doubleValue() + ((Double) arg).doubleValue());
                    }
                };
            }

        if (receiver instanceof Double) {
            if (args[0] instanceof Integer)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() + ((Integer) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() + (double)((Integer) arg).intValue());
                    }
                };

            if (args[0] instanceof Long)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() + ((Long) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() + ((Long) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Float)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() + ((Float) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() + ((Float) arg).doubleValue());
                    }
                };

            if (args[0] instanceof Double)
                return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
                    public final Object invoke(Object receiver, Object[] args) {
                        return new Double(((Double) receiver).doubleValue() + ((Double) args[0]).doubleValue());
                    }

                    public final Object invokeBinop(Object receiver, Object arg) {
                        return new Double(((Double) receiver).doubleValue() + ((Double) arg).doubleValue());
                    }
                };
            }

        return new NumberNumberCallSite (site, metaClass, metaMethod, params, (Number)receiver, (Number)args[0]){
            public final Object invoke(Object receiver, Object[] args) {
                return math.addImpl((Number)receiver,(Number)args[0]);
            }

            public final Object invokeBinop(Object receiver, Object arg) {
                return math.addImpl((Number)receiver,(Number)arg);
            }
        };
    }
}
