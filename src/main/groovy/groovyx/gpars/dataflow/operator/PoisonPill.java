// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2012  The original author or authors
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

package groovyx.gpars.dataflow.operator;

/**
 * Represents the poison for dataflow operators.
 * After receiving the poison a dataflow operator will send the poisson to all its output channels and terminate.
 *
 * Non=immediate poison will allow selectors to keep processing all remaining inputs until these get also poisoned.
 * There§s no difference in behavior between immediate and non=immediate poison pills when obtained bz an operator.
 *
 * @author Vaclav Pech
 *         Date: Oct 6, 2010
 */
public class PoisonPill implements ControlMessage {
    private static final PoisonPill ourInstance = new PoisonPill();
    private static final PoisonPill immediateInstance = new PoisonPill(true);
    private final boolean immediate;

    /**
     * Retrieves a non-immediate poison pill instance
     * @return The shared singleton non-immediate poison pill instance
     */
    public static PoisonPill getInstance() {
        return ourInstance;
    }

    /**
     * Retrieves an immediate poison pill instance
     * @return The shared singleton immediate poison pill instance
     */
    public static PoisonPill getImmediateInstance() {
        return immediateInstance;
    }

    PoisonPill() {
        this(false);
    }

    PoisonPill(final boolean immediate) {
        this.immediate = immediate;
    }

    public final boolean isImmediate() {
        return immediate;
    }

    void countDown() {
    }
}
