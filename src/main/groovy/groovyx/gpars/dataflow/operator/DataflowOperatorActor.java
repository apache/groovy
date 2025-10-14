// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2011, 2013  The original author or authors
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
import groovyx.gpars.dataflow.DataflowReadChannel;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.group.PGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An operator's internal actor. Repeatedly polls inputs and once they're all available it performs the operator's body.
 * <p>
 * Iteratively waits for enough values from inputs.
 * Once all required inputs are available (received as messages), the operator's body is run.
 * </p>
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"RawUseOfParameterizedType", "unchecked"})
class DataflowOperatorActor extends DataflowProcessorActor {
    private Map values = new HashMap(10);

    DataflowOperatorActor(final DataflowOperator owningOperator, final PGroup group, final List outputs, final List inputs, final Closure code) {
        super(owningOperator, group, outputs, inputs, code);
    }

    @Override
    @SuppressWarnings({"UnusedDeclaration"})
    final void afterStart() {
        super.afterStart();
        queryInputs(true);
    }

    private void queryInputs(final boolean initialRun) {
        for (int i = 0; i < inputs.size(); i++) {
            final DataflowReadChannel input = (DataflowReadChannel) inputs.get(i);
            if (initialRun || !(input instanceof DataflowVariable)) {
                input.getValAsync(i, this);
            } else {
                try {
                    values.put(i, input.getVal());
                } catch (InterruptedException e) {
                    throw new IllegalStateException("couldn't read the value of a DataflowVariable inside an operator.", e);
                }
            }
        }
    }

    @Override
    public final void onMessage(final Object message) {
        if (message instanceof StopGently) {
            stoppingGently = true;
            return;
        }
        final Map msg = (Map) message;
        Object result = msg.get("result");
        final Object attachment = msg.get("attachment");

        if (isControlMessage(result)) {
            result = fireMessageArrived(result, (Integer) attachment, true);
            checkPoison(result);
            if (isControlMessage(result)) return;
        }

        final Object verifiedValue = fireMessageArrived(result, (Integer) attachment, false);

        values.put(attachment, verifiedValue);
        if (values.size() > inputs.size())
            throw new IllegalStateException("The DataflowOperatorActor is in an inconsistent state. values.size()=" + values.size() + ", inputs.size()=" + inputs.size());
        if (values.size() == inputs.size()) {
            final List<Map.Entry<Comparable, Object>> arrivedMessages = new ArrayList<Map.Entry<Comparable, Object>>(values.entrySet());
            Collections.sort(arrivedMessages, new Comparator<Map.Entry<Comparable, Object>>() {
                @Override
                public int compare(final Map.Entry<Comparable, Object> o1, final Map.Entry<Comparable, Object> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });

            final List<Object> arrivedValues = new ArrayList<Object>(arrivedMessages.size());
            for (final Map.Entry entry : arrivedMessages) {
                arrivedValues.add(entry.getValue());
            }

            final List<Object> verifiedValues = owningProcessor.fireBeforeRun(arrivedValues);

            startTask(verifiedValues);
            values = new HashMap(values.size());
            if (stoppingGently) {
                stop();
            }
            if (!hasBeenStopped()) queryInputs(false);
        }
    }

    @SuppressWarnings({"CatchGenericClass"})
    void startTask(final List<Object> results) {
        try {
            code.call(results.toArray(new Object[results.size()]));
        } catch (Throwable e) {
            reportException(e);
        } finally {
            owningProcessor.fireAfterRun(results);
        }
    }
}
