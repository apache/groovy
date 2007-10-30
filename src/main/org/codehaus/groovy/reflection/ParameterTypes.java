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
package org.codehaus.groovy.reflection;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.MetaClassHelper;

import java.lang.reflect.Array;

public class ParameterTypes
{
  protected Class [] nativeParamTypes;
  protected CachedClass [] parameterTypes;

    public ParameterTypes () {
    }

    public ParameterTypes(Class pt []) {
        nativeParamTypes = pt;
    }

    public ParameterTypes(CachedClass[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public CachedClass[] getParameterTypes() {
      if (parameterTypes == null) {
        synchronized (this) {
          if (parameterTypes != null)
            return parameterTypes;

          Class [] npt = nativeParamTypes == null ? getPT() : nativeParamTypes;

          CachedClass [] pt = new CachedClass [npt.length];
          for (int i = 0; i != npt.length; ++i)
            pt[i] = ReflectionCache.getCachedClass(npt[i]);

          nativeParamTypes = npt;
          parameterTypes = pt;
        }
      }

      return parameterTypes;
  }

    public Class[] getNativeParameterTypes() {
        if (nativeParamTypes == null) {
          synchronized (this) {
            if (nativeParamTypes != null)
              return nativeParamTypes;

            Class [] npt;
            if (parameterTypes != null) {
                npt = new Class [parameterTypes.length];
                for (int i = 0; i != parameterTypes.length; ++i) {
                    npt[i] = parameterTypes[i].getCachedClass();
                }
            }
            else
              npt = getPT ();
            nativeParamTypes = npt;
          }
        }
        return nativeParamTypes;
    }

    Class[] getPT() { return null; }

    public boolean isVargsMethod(Object[] arguments) {
        getParameterTypes();
        if (parameterTypes.length == 0) return false;
        final int lenMinus1 = parameterTypes.length - 1;
        if (!parameterTypes[lenMinus1].isArray) return false;
        // -1 because the varg part is optional
        if (lenMinus1 == arguments.length) return true;
        if (lenMinus1 > arguments.length) return false;
        if (arguments.length > parameterTypes.length) return true;

        // only case left is arguments.length == parameterTypes.length
        Object last = arguments[arguments.length - 1];
        if (last == null) return true;
        Class clazz = last.getClass();
        return !clazz.equals(parameterTypes[lenMinus1].getCachedClass());

    }

    public Object[] coerceArgumentsToClasses(Object[] argumentArray) {
        getParameterTypes();
        // correct argumentArray's length
        if (argumentArray == null) {
            argumentArray = MetaClassHelper.EMPTY_ARRAY;
        } else if (parameterTypes.length == 1 && argumentArray.length == 0) {
            if (isVargsMethod(argumentArray)) {
                argumentArray = new Object[]{Array.newInstance(parameterTypes[0].getCachedClass().getComponentType(), 0)};
            }
            else
                argumentArray = MetaClassHelper.ARRAY_WITH_NULL;
        } else if (isVargsMethod(argumentArray)) {
            argumentArray = fitToVargs(argumentArray, parameterTypes);
        }

        //correct Type
        for (int i = 0; i < argumentArray.length; i++) {
            Object argument = argumentArray[i];
            if (argument == null) continue;
            CachedClass parameterType = parameterTypes[i];
            if (ReflectionCache.isAssignableFrom(parameterType.getCachedClass(), argument.getClass())) continue;

            argument = parameterType.coerceGString(argument);
            argument = parameterType.coerceNumber(argument);
            argument = parameterType.coerceArray(argument);
            argumentArray[i] = argument;
        }
        return argumentArray;
    }

    /**
     * this method is called when the number of arguments to a method is greater than 1
     * and if the method is a vargs method. This method will then transform the given
     * arguments to make the method callable
     *
     * @param argumentArray the arguments used to call the method
     * @param paramTypes    the types of the paramters the method takes
     */
    private static Object[] fitToVargs(Object[] argumentArray, CachedClass[] paramTypes) {
        Class vargsClass = ReflectionCache.autoboxType(paramTypes[paramTypes.length - 1].getCachedClass().getComponentType());

        if (argumentArray.length == paramTypes.length - 1) {
            // the vargs argument is missing, so fill it with an empty array
            Object[] newArgs = new Object[paramTypes.length];
            System.arraycopy(argumentArray, 0, newArgs, 0, argumentArray.length);
            Object vargs = MetaClassHelper.makeArray(null, vargsClass, 0);
            newArgs[newArgs.length - 1] = vargs;
            return newArgs;
        } else if (argumentArray.length == paramTypes.length) {
            // the number of arguments is correct, but if the last argument
            // is no array we have to wrap it in a array. if the last argument
            // is null, then we don't have to do anything
            Object lastArgument = argumentArray[argumentArray.length - 1];
            if (lastArgument != null && !lastArgument.getClass().isArray()) {
                // no array so wrap it
                Object vargs = MetaClassHelper.makeArray(lastArgument, vargsClass, 1);
                System.arraycopy(argumentArray, argumentArray.length - 1, vargs, 0, 1);
                argumentArray[argumentArray.length - 1] = vargs;
                return argumentArray;
            } else {
                // we may have to box the arguemnt!
                return argumentArray;
            }
        } else if (argumentArray.length > paramTypes.length) {
            // the number of arguments is too big, wrap all exceeding elements
            // in an array, but keep the old elements that are no vargs
            Object[] newArgs = new Object[paramTypes.length];
            // copy arguments that are not a varg
            System.arraycopy(argumentArray, 0, newArgs, 0, paramTypes.length - 1);
            // create a new array for the vargs and copy them
            int numberOfVargs = argumentArray.length - paramTypes.length;
            Object vargs = MetaClassHelper.makeCommonArray(argumentArray, paramTypes.length - 1, vargsClass);
            newArgs[newArgs.length - 1] = vargs;
            return newArgs;
        } else {
            throw new GroovyBugError("trying to call a vargs method without enough arguments");
        }
    }
}
