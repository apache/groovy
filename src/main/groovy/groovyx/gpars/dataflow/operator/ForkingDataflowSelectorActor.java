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

import groovy.lang.Closure;
import groovyx.gpars.group.PGroup;
import groovyx.gpars.scheduler.Pool;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * An selector's internal actor. Repeatedly polls inputs and once they're all available it performs the selector's body.
 * The selector's body is executed in as a separate task, allowing multiple copies of the body to be run concurrently.
 * The maxForks property guards the maximum number or concurrently run copies.
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"RawUseOfParameterizedType"})
final class ForkingDataflowSelectorActor extends DataflowSelectorActor {
    private final Semaphore semaphore;
    private final Pool threadPool;
    private final int maxForks;

    @SuppressWarnings({"ConstructorWithTooManyParameters"})
    ForkingDataflowSelectorActor(final DataflowSelector owningOperator, final PGroup group, final List outputs, final List inputs, final Closure code, final int maxForks) {
        super(owningOperator, group, outputs, inputs, code);
        this.maxForks = maxForks;
        this.semaphore = new Semaphore(maxForks);
        this.threadPool = group.getThreadPool();
    }

    @Override
    void startTask(final int index, final Object result) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new IllegalStateException(CANNOT_OBTAIN_THE_SEMAPHORE_TO_FORK_OPERATOR_S_BODY, e);
        }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ForkingDataflowSelectorActor.super.startTask(index, result);
                } finally {
                    semaphore.release();
                }
            }
        });
    }

    @Override
    protected void forwardPoisonPill(final Object data) {
        try {
            semaphore.acquire(maxForks);
        } catch (InterruptedException e) {
            owningProcessor.reportError(e);
        } finally {
            super.forwardPoisonPill(data);
            semaphore.release(maxForks);
        }
    }
}
