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

package groovyx.gpars.dataflow.impl;

import groovy.lang.Closure;
import groovyx.gpars.dataflow.SelectableChannel;

/**
 * A closure registered with all the input channels on the wheneverBound() event to inform the Select
 * about a value being available in a particular channel.
 *
 * @author Vaclav Pech
 *         Date: 30th Sep 2010
 */
public final class SelectCallback<T> extends Closure {
    private static final long serialVersionUID = 5953873495199115151L;
    private final int index;
    private final SelectableChannel<? extends T> channel;

    /**
     * @param owner   The SelectBase instance to notify
     * @param index   The index of the channel this SelectCallback instance represents
     * @param channel The channel represented by this SelectCallback instance
     */
    public SelectCallback(final Object owner, final int index, final SelectableChannel<? extends T> channel) {
        super(owner);
        this.index = index;
        this.channel = channel;
    }

    /**
     * Invoked by the channel when a value has been bound to it and is available for consumption
     *
     * @param args Holds the value, but we do not work with the value here
     * @return A dummy string, since the caller doesn't check the return value
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public Object call(final Object[] args) {
        try {
            ((SelectBase<T>) getOwner()).boundNotification(index, channel);
        } catch (InterruptedException ignore) {
        }
        return "SelectCallback";
    }
}
