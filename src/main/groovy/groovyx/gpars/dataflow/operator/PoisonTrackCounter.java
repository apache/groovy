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

import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.Promise;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A helper class used by poison messages to count terminated dataflow processors
 *
 * @author Vaclav Pech
 */
final class PoisonTrackCounter {
    private final DataflowVariable<Boolean> termination = new DataflowVariable<Boolean>();
    private final AtomicInteger counter;
    PoisonTrackCounter(final int count) {
        if (count < 0)
            throw new IllegalArgumentException("A counting poison pill can only start with a positive number");
        this.counter = new AtomicInteger(count);
    }

    /**
     * Blocks until the number of terminated operators reaches the number specified in the constructor
     *
     * @throws InterruptedException If the current thread gets interrupted during the blocking
     */
    public void join() throws InterruptedException {
        termination.join();
    }

    /**
     * Blocks until the number of terminated operators reaches the number specified in the constructor
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the {@code timeout} argument
     * @throws InterruptedException If the current thread gets interrupted during the blocking
     */
    public void join(final long timeout, final TimeUnit unit) throws InterruptedException {
        termination.join(timeout, unit);
    }

    public Promise<Boolean> getTermination() {
        return termination;
    }

    void countDown() {
        final int currentValue = counter.decrementAndGet();
        if (currentValue == 0) termination.bind(true);
    }
}
