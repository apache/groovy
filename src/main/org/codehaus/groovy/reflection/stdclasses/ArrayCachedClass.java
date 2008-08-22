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
package org.codehaus.groovy.reflection.stdclasses;

import groovy.lang.GString;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.math.BigDecimal;

/**
 * @author Alex.Tkachman
 */
public class ArrayCachedClass extends CachedClass {
    public ArrayCachedClass(Class klazz) {
        super(klazz);
    }

    public Object coerceArgument(Object argument) {
        Class argumentClass = argument.getClass();
        if (argumentClass.getName().charAt(0) != '[') return argument;
        Class argumentComponent = argumentClass.getComponentType();

        Class paramComponent = getTheClass().getComponentType();
        if (paramComponent.isPrimitive()) {
            if (paramComponent == boolean.class && argumentClass == Boolean[].class) {
                argument = DefaultTypeTransformation.convertToBooleanArray(argument);
            } else if (paramComponent == byte.class && argumentClass == Byte[].class) {
                argument = DefaultTypeTransformation.convertToByteArray(argument);
            } else if (paramComponent == char.class && argumentClass == Character[].class) {
                argument = DefaultTypeTransformation.convertToCharArray(argument);
            } else if (paramComponent == short.class && argumentClass == Short[].class) {
                argument = DefaultTypeTransformation.convertToShortArray(argument);
            } else if (paramComponent == int.class && argumentClass == Integer[].class) {
                argument = DefaultTypeTransformation.convertToIntArray(argument);
            } else if (paramComponent == long.class &&
                    (argumentClass == Long[].class || argumentClass == Integer[].class)) {
                argument = DefaultTypeTransformation.convertToLongArray(argument);
            } else if (paramComponent == float.class &&
                    (argumentClass == Float[].class || argumentClass == Integer[].class)) {
                argument = DefaultTypeTransformation.convertToFloatArray(argument);
            } else if (paramComponent == double.class &&
                    (argumentClass == Double[].class || argumentClass == Float[].class
                            || BigDecimal[].class.isAssignableFrom(argumentClass))) {
                argument = DefaultTypeTransformation.convertToDoubleArray(argument);
            }
        } else if (paramComponent == String.class && argument instanceof GString[]) {
            GString[] strings = (GString[]) argument;
            String[] ret = new String[strings.length];
            for (int i = 0; i < strings.length; i++) {
                ret[i] = strings[i].toString();
            }
            argument = ret;
        } else if (paramComponent==Object.class && argumentComponent.isPrimitive()){
            argument = DefaultTypeTransformation.primitiveArrayBox(argument);
        }
        return argument;
    }

}
