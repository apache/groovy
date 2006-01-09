/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.grails.web.metaclass;

import groovy.lang.MissingMethodException;
import groovy.lang.Closure;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import grails.util.OpenRicoBuilder;

import java.io.IOException;

import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.ControllerExecutionException;

/**
 * A controller dynamic method that takes a closure to create a OpenRico response using the OpenRicoBuilder syntax
 *
 * @author Graeme Rocher
 * @since Jan 9, 2006
 */
public class RicoDynamicMethod extends AbstractDynamicControllerMethod {
    private static final String METHOD_SIGNATURE = "rico";

    public RicoDynamicMethod(HttpServletRequest request, HttpServletResponse response) {
        super(METHOD_SIGNATURE, request, response);
    }

    public Object invoke(Object target, Object[] arguments) {
        if(arguments.length == 0)
            throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);

        if(arguments[0] instanceof Closure) {
            OpenRicoBuilder orb = null;
            try {
                orb = new OpenRicoBuilder(response);
            } catch (IOException e) {
                throw new ControllerExecutionException("I/O error building ajax response in call ["+METHOD_SIGNATURE+"] on target ["+target.getClass()+"]");
            }
            orb.invokeMethod("ajax", arguments[0]);
            return null;
        }
        else {
            throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);
        }
    }
}
