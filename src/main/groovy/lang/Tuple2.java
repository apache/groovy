/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.lang;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * Represents a list of 2 typed Objects.
 */
public class Tuple2<T1, T2> extends Tuple {
    public Tuple2(T1 first, T2 second) {
        super(new Object[]{first, second});
    }

    @SuppressWarnings("unchecked")
    public T1 getFirst() {
        return (T1) get(0);
    }

    @SuppressWarnings("unchecked")
    public T2 getSecond() {
        return (T2) get(1);
    }

    public boolean equals(Object that) {
        return that instanceof Tuple2 && equals((Tuple2) that);
    }

    public boolean equals(Tuple2 that) {
        if (size() != that.size()) return false;
        for (int i = 0; i < size(); i++) {
            if (!DefaultTypeTransformation.compareEqual(get(i), that.get(i))) {
                return false;
            }
        }
        return true;
    }
}
