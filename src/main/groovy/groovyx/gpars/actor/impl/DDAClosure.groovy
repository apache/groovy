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

package groovyx.gpars.actor.impl

import groovyx.gpars.actor.DynamicDispatchActor
import org.codehaus.groovy.runtime.NullObject

/**
 * Creates a DDA closure to invoke appropriate actor's message handlers based on message runtime type
 *
 * @author Vaclav Pech
 */
public abstract class DDAClosure {

    private DDAClosure() { }

    /**
     * Creates the closure for a given DDA
     * @param dda The DynamicDispatchActor to dispatch messages on
     * @return The closure to use for dynamic dispatch to the given actor
     */
    public static Closure createDDAClosure(final DynamicDispatchActor dda) {
        {msg ->
            return dda.onMessage(msg != null ? msg : NullObject.nullObject)  //Groovy truth won't let us use Elvis for numbers, strings and collections correctly)
        }
    }
}
