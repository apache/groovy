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

package groovyx.gpars.util;

import groovy.lang.Closure;

import java.util.concurrent.Semaphore;

/**
 * Extends Semaphore with a handy withSemaphore(Closure) method to safely acquire and release the Semaphore
 * for the passed-in closure.
 * Use:
 * def extendedSemaphore = new ExtendedSemaphore()
 * extendedSemaphore.withSemaphore() {*      //semaphore acquired here
 * }*
 *
 * @author Vaclav Pech
 *         Date: Jan 8, 2009
 */
public class EnhancedSemaphore extends Semaphore {
    private static final long serialVersionUID = 7582324169075000859L;

    /**
     * Creates a new EnhancedSemaphore, delegating to the Semaphore class constructor.
     *
     * @param permits Maximum number of concurrently accepted threads.
     */
    public EnhancedSemaphore(final int permits) {
        super(permits);
    }

    /**
     * Performs the passed-in closure with the Semaphore acquired and releases the Semaphore automatically
     * after the closure finishes.
     *
     * @param cl The closure to perform with the Semaphore acquired
     * @throws InterruptedException If the current thread gets interrupted
     */
    public void withSemaphore(final Closure cl) throws InterruptedException {
        acquire();
        try {
            cl.call();
        } finally {
            release();
        }
    }
}
