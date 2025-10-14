// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
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

package groovyx.gpars.actor.impl;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovyx.gpars.actor.BlockingActor;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Arrays;

/**
 * Utility class to implement a blocking actor backed by any Runnable (including Closure)
 *
 * @author Alex Tkachman, Vaclav Pech
 */
public class RunnableBackedBlockingActor extends BlockingActor {
    private static final long serialVersionUID = 8992135845484038961L;

    private Runnable action;

    public RunnableBackedBlockingActor(final Runnable handler) {
        setAction(handler);
    }

    protected final void setAction(final Runnable handler) {
        if (handler == null) {
            action = null;
        } else {
            if (handler instanceof Closure) {
                final Closure cloned = (Closure) ((Closure) handler).clone();
                if (cloned.getOwner() == cloned.getDelegate()) {
                    // otherwise someone else already took care for setting delegate for the closure
                    cloned.setDelegate(this);
                    cloned.setResolveStrategy(Closure.DELEGATE_FIRST);
                } else {
                    cloned.setDelegate(new RunnableBackedPooledActorDelegate(cloned.getDelegate(), this));
                }
                action = cloned;
            } else {
                action = handler;
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected final void act() {
        if (action != null) {
            if (action instanceof Closure) {
                //noinspection RawUseOfParameterizedType
                GroovyCategorySupport.use(Arrays.<Class>asList(), (Closure) action);
            } else {
                action.run();
            }
        }
    }

    private static final class RunnableBackedPooledActorDelegate extends GroovyObjectSupport {
        private final Object first;
        private final Object second;

        RunnableBackedPooledActorDelegate(final Object first, final Object second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public Object invokeMethod(final String name, final Object args) {
            try {
                return InvokerHelper.invokeMethod(first, name, args);
            } catch (MissingMethodException ignore) {
                return InvokerHelper.invokeMethod(second, name, args);
            }
        }

        @Override
        public Object getProperty(final String property) {
            try {
                return InvokerHelper.getProperty(first, property);
            } catch (MissingPropertyException ignore) {
                return InvokerHelper.getProperty(second, property);
            }
        }

        @Override
        public void setProperty(final String property, final Object newValue) {
            try {
                InvokerHelper.setProperty(first, property, newValue);
            } catch (MissingPropertyException ignore) {
                InvokerHelper.setProperty(second, property, newValue);
            }
        }
    }
}
