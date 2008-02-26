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

package groovy.mock;

import groovy.lang.GroovyObject;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

import com.mockobjects.Verifiable;
import com.mockobjects.dynamic.*;

/**
 * 
 * @author Joe Walnes
 * @author Chris Stevenson
 * @deprecated  As of Groovy 1.5 replaced by MockFor (or use an external mocking package)
 *              {@link groovy.mock.interceptor.MockFor}
 */
public class GroovyMock extends GroovyObjectSupport implements Verifiable {

    private CallBag calls = new CallBag();
    private CallFactory callFactory = new DefaultCallFactory();
    private Mock mock = new Mock(I.class);

    interface I {
    }

    private GroovyObject instance = new GroovyObjectSupport() {
        public Object invokeMethod(String name, Object args) {
            return callMethod(name, args);
        }
    };

    public Object invokeMethod(String name, Object args) {
        if (name.equals("verify")) {
            verify();
        }
        else {
            expectMethod(name, args);
        }
        return null;
    }

    public GroovyObject getInstance() {
        return instance;
    }

    public static GroovyMock newInstance() {
        return new GroovyMock();
    }

    private void expectMethod(String name, Object args) {
        ConstraintMatcher constraintMatcher = createMatcher(args);
        calls.addExpect(
            callFactory.createCallExpectation(
                callFactory.createCallSignature(name, constraintMatcher, callFactory.createVoidStub())));
    }

    private ConstraintMatcher createMatcher(Object args) {
        if(args.getClass().isArray()) {
            Object argArray[] = (Object[]) args;
            if (argArray[0] instanceof Closure) {
                Closure closure = (Closure) argArray[0];
                return C.args(new ClosureConstraintMatcher(closure));
            }
        }
        return C.args(C.eq(args));
    }

    private Object callMethod(String name, Object args) {
        try {
            return calls.call(mock, name, new Object[] { args });
        }
        catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public void verify() {
        calls.verify();
    }

}
