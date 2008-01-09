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

package org.codehaus.groovy.vmplugin.v5;

import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.Arrays;
import java.lang.reflect.Method;

public class PluginDefaultGroovyMethods {
    private static final Object[] NO_ARGS = new Object[0];

    /**
     * This method is called by the ++ operator for enums. It will invoke
     * Groovy's default next behaviour for enums do not have their own
     * next method.
     *
     * @param self an Enum
     * @return the next defined enum from the enum class
     */
    public static Object next(Enum self) {
        final Method[] methods = self.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals("next") && method.getParameterTypes().length == 0) {
                return InvokerHelper.invokeMethod(self, "next", NO_ARGS);
            }
        }
        Object[] values = (Object[]) InvokerHelper.invokeStaticMethod(self.getClass(), "values", NO_ARGS);
        int index = Arrays.asList(values).indexOf(self);
        return values[index < values.length - 1 ? index + 1 : 0];
    }

    /**
     * This method is called by the -- operator for enums. It will invoke
     * Groovy's default previous behaviour for enums that do not have
     * their own previous method.
     *
     * @param self an Enum
     * @return the previous defined enum from the enum class
     */
    public static Object previous(Enum self) {
        final Method[] methods = self.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals("previous") && method.getParameterTypes().length == 0) {
                return InvokerHelper.invokeMethod(self, "previous", NO_ARGS);
            }
        }
        Object[] values = (Object[]) InvokerHelper.invokeStaticMethod(self.getClass(), "values", NO_ARGS);
        int index = Arrays.asList(values).indexOf(self);
        return values[index > 0 ? index - 1 : values.length - 1];
    }


}
