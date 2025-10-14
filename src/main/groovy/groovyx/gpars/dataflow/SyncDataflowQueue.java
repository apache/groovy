// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2011, 2013  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.dataflow;

import groovy.lang.Closure;
import groovyx.gpars.dataflow.operator.ChainWithClosure;
import groovyx.gpars.dataflow.operator.CopyChannelsClosure;
import groovyx.gpars.group.PGroup;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Represents a thread-safe synchronous data flow stream. Values or DataflowVariables are added using the '&lt;&lt;' operator
 * and safely read once available using the 'val' property.
 * The iterative methods like each(), collect(), iterator(), any(), all() or the for loops work with snapshots
 * of the stream at the time of calling the particular method.
 * For actors and Dataflow Operators the asynchronous non-blocking variants of the getValAsync() methods can be used.
 * They register the request to read a value and will send a message to the actor or operator once the value is available.
 * <p>
 * Unlike DataflowQueue, which exchanges data asynchronously, SyncDataflowQueue blocks the writer until a reader is ready to consume the message.
 * </p>
 *
 * @author Vaclav Pech
 *         Date: Jun 5, 2009
 */
@SuppressWarnings({"ClassWithTooManyMethods"})
public final class SyncDataflowQueue<T> extends DataflowQueue<T> {

    /**
     * Creates a new variable to perform the next data exchange
     *
     * @return The newly created SyncDataflowVariable instance with exactly one expected reader
     */
    @Override
    protected DataflowVariable<T> createVariable() {
        return new SyncDataflowVariable<T>(1);
    }

    @Override
    public <V> DataflowReadChannel<V> chainWith(final PGroup group, final Closure<V> closure) {
        final SyncDataflowQueue<V> result = new SyncDataflowQueue<V>();
        group.operator(this, result, new ChainWithClosure<V>(closure));
        return result;
    }

    @Override
    public DataflowReadChannel<T> tap(final PGroup group, final DataflowWriteChannel<T> target) {
        final SyncDataflowVariable<T> result = new SyncDataflowVariable<T>();
        group.operator(asList(this), asList(result, target), new ChainWithClosure(new CopyChannelsClosure()));
        return result;
    }

    @Override
    public <V> DataflowReadChannel<V> merge(final PGroup group, final List<DataflowReadChannel<Object>> others, final Closure<V> closure) {
        final SyncDataflowQueue<V> result = new SyncDataflowQueue<V>();
        final List<DataflowReadChannel<?>> inputs = new ArrayList<DataflowReadChannel<?>>();
        inputs.add(this);
        inputs.addAll(others);
        group.operator(inputs, asList(result), new ChainWithClosure(closure));
        return result;
    }

    @Override
    public String toString() {
        return "SyncDataflowQueue(queue=" + new ArrayList<DataflowVariable<T>>(getQueue()).toString() + ')';
    }
}
