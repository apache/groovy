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

/**
 * Pooled actors need to simulate continuations to create stacktrace-less chunks of work (ActorActions) to assign
 * to the threads from the pool. To achieve this ActorActions throw exceptions to terminate the current chuck of work
 * and allow another chunk of work on the same actor to begin.
 * ActorAction is a parent to these exception. It also holds initialized instances of each of the concrete subclasses
 * to avoid need for exception object creation each time.
 *
 * @author Vaclav Pech, Alex Tkachman
 *         Date: Feb 17, 2009
 */
public class ActorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public static final ActorException TERMINATE = new ActorTerminationException();
    public static final ActorException STOP = new ActorStopException();

    ActorException() {
    }
}
