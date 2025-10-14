// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2013  The original author or authors
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
import groovyx.gpars.dataflow.SelectResult;
import groovyx.gpars.group.PGroup;

import java.util.Arrays;
import java.util.List;

/**
 * An selector's internal actor. Repeatedly polls inputs and once they're all available it performs the selector's body.
 * <p>
 * Iteratively waits for values on the inputs.
 * Once all a value is available (received as a message), the selector's body is run.
 * </p>
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"RawUseOfParameterizedType"})
class DataflowSelectorActor extends DataflowProcessorActor {
    protected final boolean passIndex;

    DataflowSelectorActor(final DataflowSelector owningOperator, final PGroup group, final List outputs, final List inputs, final Closure code) {
        super(owningOperator, group, outputs, inputs, code);
        passIndex = code.getMaximumNumberOfParameters() == 2;
    }

    @Override
    @SuppressWarnings({"UnusedDeclaration"})
    final void afterStart() {
        super.afterStart();
        ((DataflowSelector) owningProcessor).doSelect();
    }

    @Override
    public final void onMessage(final Object message) {
        if (message instanceof StopGently) {
            stoppingGently = true;
            return;
        }
        final SelectResult msg = (SelectResult) message;
        final int index = msg.getIndex();
        Object value = msg.getValue();

        if (isControlMessage(value)) {
            value = fireMessageArrived(value, index, true);
            if (value instanceof PoisonPill) handlePoisonPillInSelector(index, value);
            if (isControlMessage(value)) return;
        }

        final Object verifiedValue = fireMessageArrived(value, index, false);
        final List<Object> verifiedValues = owningProcessor.fireBeforeRun(Arrays.asList(verifiedValue));

        startTask(index, verifiedValues.get(0));
        if (stoppingGently) {
            stop();
        }
        if (!hasBeenStopped()) ((DataflowSelector) owningProcessor).doSelect();
    }

    private void handlePoisonPillInSelector(final int index, final Object value) {
        if (((PoisonPill) value).isImmediate()) {
            checkPoison(value);
        } else {
            final DataflowSelector selector = (DataflowSelector) owningProcessor;
            selector.setGuard(index, false);
            if (selector.allGuardsClosed()) checkPoison(value);
            else {
                if (!hasBeenStopped()) ((DataflowSelector) owningProcessor).doSelect();
            }
        }
    }

    @SuppressWarnings({"CatchGenericClass"})
    void startTask(final int index, final Object result) {
        try {
            if (passIndex) {
                code.call(new Object[]{result, index});
            } else {
                code.call(result);
            }
        } catch (Throwable e) {
            reportException(e);
        } finally {
            owningProcessor.fireAfterRun(Arrays.asList(result));
        }
    }
}
