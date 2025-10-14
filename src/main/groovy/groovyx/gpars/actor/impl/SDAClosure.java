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
import groovyx.gpars.actor.StaticDispatchActor;

/**
 * Creates a SDA closure to invoke appropriate actor's message handlers based on message compile-type type
 *
 * @author Vaclav Pech
 */
public abstract class SDAClosure {

    private SDAClosure() {
    }

    /**
     * Creates the closure for a given SDA
     *
     * @param dda The StaticDispatchActor to dispatch messages on
     * @return The closure to use for static dispatch to the given actor
     */
    public static <T> Closure createSDAClosure(final StaticDispatchActor<T> dda) {
        return new Closure(dda) {
            @Override
            public Object call(final Object arguments) {
                final T message = (T) arguments;
                dda.onMessage(message != null ? message : null);
                return null;
            }
        };
    }
}
