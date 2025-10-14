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

import groovy.lang.Closure;

/**
 * Represents the DDA closure to invoke appropriate message handlers based on message runtime type
 *
 * @author Vaclav Pech
 */
public final class DefaultActorClosure extends Closure {

    private final DefaultActor myActor;
    private static final long serialVersionUID = 3009666814957486672L;

    DefaultActorClosure(final DefaultActor actor) {
        super(actor);
        this.myActor = actor;
    }

    @Override
    public Object call(final Object arguments) {
        myActor.onMessage(arguments);
        return null;
    }
}
