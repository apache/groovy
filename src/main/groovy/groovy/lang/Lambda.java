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

package groovy.lang;

import org.apache.groovy.internal.util.UncheckedThrow;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import static org.codehaus.groovy.classgen.asm.sc.StaticTypesLambdaWriter.SAM_NAME;

/**
 * Represents any lambda object in Groovy.
 *
 * @since 3.0.0
 */
public abstract class Lambda<V> extends Closure<V> {
    private Object lambdaObject;

    public Lambda(Object owner, Object thisObject) {
        super(owner, thisObject);
    }

    /**
     * Constructor used when the "this" object for the Lambda is null.
     * This is rarely the case in normal Groovy usage.
     *
     * @param owner the Lambda owner
     */
    public Lambda(Object owner) {
        super(owner);
    }

    @Override
    public V call(Object... args) {
        String methodName;
        try {
            methodName = (String) this.getClass().getField(SAM_NAME).get(lambdaObject);
        } catch (IllegalAccessException e) {
            throw new GroovyBugError("Failed to access field " + SAM_NAME + " of " + this.getClass(), e);
        } catch (NoSuchFieldException e) {
            throw new GroovyBugError("Failed to find field " + SAM_NAME + " in " + this.getClass(), e);
        }

        try {
            return (V) getMetaClass().invokeMethod(lambdaObject, methodName, args);
        } catch (InvokerInvocationException e) {
            UncheckedThrow.rethrow(e.getCause());
            return null; // unreachable statement
        }  catch (Exception e) {
            return (V) throwRuntimeException(e);
        }
    }

    public Object getLambdaObject() {
        return lambdaObject;
    }

    public void setLambdaObject(Object lambdaObject) {
        this.lambdaObject = lambdaObject;
    }

}
