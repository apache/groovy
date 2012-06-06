/*
 * Copyright 2007 the original author or authors.
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
package org.codehaus.groovy.binding;

import groovy.lang.Closure;

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 */
public class ClosureSourceBinding implements SourceBinding {

    Closure closure;
    Object[] arguments;

    public ClosureSourceBinding(Closure closure) {
        this(closure, new Object[0]);
    }

    public ClosureSourceBinding(Closure closure, Object[] arguments) { //TODO in Groovy 2.0 use varargs?
        this.closure = closure;
        this.arguments = arguments;
    }

    public Closure getClosure() {
        return closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    public Object getSourceValue() {
        return closure.call(arguments);
    }

    public void setClosureArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public void setClosureArgument(Object argument) {
        this.arguments = new Object[] {argument};
    }
}
