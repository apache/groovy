// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.actor;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * @author Alex Tkachman, Vaclav Pech
 */
final class ForwardingDelegate extends GroovyObjectSupport {

    private final Object first;
    private final Object second;

    ForwardingDelegate(final Object first, final Object second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public Object invokeMethod(final String name, final Object args) {
        try {
            return InvokerHelper.invokeMethod(first, name, args);
        }
        catch (MissingMethodException ignore) {
            return InvokerHelper.invokeMethod(second, name, args);
        }
    }

    @Override
    public Object getProperty(final String property) {
        try {
            return InvokerHelper.getProperty(first, property);
        }
        catch (MissingPropertyException ignore) {
            return InvokerHelper.getProperty(second, property);
        }
    }

    @Override
    public void setProperty(final String property, final Object newValue) {
        try {
            InvokerHelper.setProperty(first, property, newValue);
        }
        catch (MissingPropertyException ignore) {
            InvokerHelper.setProperty(second, property, newValue);
        }
    }
}
