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

package groovyx.gpars.dataflow.operator.component;

import groovyx.gpars.dataflow.operator.DataflowEventAdapter;
import groovyx.gpars.dataflow.operator.DataflowProcessor;

import java.util.List;

/**
 * A listener to dataflow operators and selectors allowing them to be paused and resumed.
 *
 * @author Vaclav Pech
 */
public final class ProcessorPauseResume extends DataflowEventAdapter {
    private boolean paused = false;

    @Override
    public synchronized List<Object> beforeRun(final DataflowProcessor processor, final List<Object> messages) {
        try {
            //noinspection WhileLoopSpinsOnField
            while (paused) //noinspection WaitOrAwaitWithoutTimeout,SynchronizeOnThis
                this.wait();
        } catch (InterruptedException e) {
            throw new IllegalStateException("The operator thread has been interrupted", e);
        }
        return messages;
    }

    /**
     * Pauses the operator so that it blocks the next time it attempts to process a message.
     * The operator's thread will be blocked, so care should be taken in order not to exhaust all available operator threads
     * when pausing multiple operators.
     */
    public synchronized void pause() {
        if (paused) throw new IllegalStateException("The operator has already been paused.");
        paused = true;
    }

    /**
     * Resumes the operator so that it can start processing messages again.
     */
    public synchronized void resume() {
        if (!paused) throw new IllegalStateException("The operator has not been paused.");
        paused = false;
        //noinspection SynchronizeOnThis
        this.notifyAll();
    }
}
