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

package groovyx.gpars.actor;

import groovyx.gpars.actor.impl.SDAClosure;


/**
 * Offers a statically dispatched and thus slightly faster alternative to the DynamicDispatchActor class.
 * Message handlers are discovered at compile-time.
 */
@SuppressWarnings({"ThisEscapedInObjectConstruction"})
public abstract class StaticDispatchActor<T> extends AbstractLoopingActor {
    private static final long serialVersionUID = 2709208258556647529L;

    /**
     * Creates a new instance
     */
    protected StaticDispatchActor() {
        initialize(SDAClosure.createSDAClosure(this));
    }

    /**
     * Handles the incoming messages. Needs to be implemented in sub-classes.
     *
     * @param message The message at the head of the mail box
     */
    public abstract void onMessage(final T message);
}
