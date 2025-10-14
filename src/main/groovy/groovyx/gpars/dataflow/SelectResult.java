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

package groovyx.gpars.dataflow;

/**
 * A bean representing the result of the select operation on a Select.
 * It holds the index of the input channel, which was read, and the obtained value.
 *
 * @author Vaclav Pech
 *         Date: 30th Sep 2010
 */
public final class SelectResult<T> {
    private final int index;
    private final T value;

    /**
     * Stores the result of a select operation
     *
     * @param index The index of the read input channel
     * @param value The value read
     */
    SelectResult(final int index, final T value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SelectResult{" +
                "index=" + index +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        @SuppressWarnings({"rawtypes", "RawUseOfParameterizedType"}) final SelectResult other = (SelectResult) obj;

        //noinspection AccessingNonPublicFieldOfAnotherObject
        if (index != other.index) return false;
        //noinspection AccessingNonPublicFieldOfAnotherObject
        return !(value != null ? !value.equals(other.value) : other.value != null);

    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
