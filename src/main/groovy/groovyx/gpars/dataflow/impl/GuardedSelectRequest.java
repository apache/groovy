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

package groovyx.gpars.dataflow.impl;

import java.util.List;

/**
 * The base implementation of the SelectRequest interface, providing a useful default masking (guarding) functionality.
 * Whenever invoking a select, guards can be specified, reducing the set of all input channels of the Select to consider in the given request.
 *
 * @author Vaclav Pech
 *         Date: 30th Sep 2010
 */
public abstract class GuardedSelectRequest<T> implements SelectRequest<T> {
    private final List<Boolean> mask;

    /**
     * @param mask The list of boolean flags indicating which position should be matched against. All indexes match against a null mask
     */
    @SuppressWarnings({"ConstructorNotProtectedInAbstractClass", "AssignmentToCollectionOrArrayFieldFromParameter"})
    public GuardedSelectRequest(final List<Boolean> mask) {
        this.mask = mask;
    }

    /**
     * Detects, whether the channel at the given index is guarded or not.
     *
     * @param index The index of the input channel to check for guard
     * @return True, if the channel's guard is true or no guards were set and so the channel should be included in the current select operation
     */
    @Override
    public boolean matchesMask(final int index) {
        if (mask == null) return true;
        return mask.get(index);
    }
}
