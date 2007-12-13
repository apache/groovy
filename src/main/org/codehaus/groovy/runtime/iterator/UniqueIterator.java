/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.runtime.iterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UniqueIterator extends FilterIterator {
    final Set processed = new HashSet();

    public UniqueIterator(Iterator self) {
        super(self, null, new Filter() {
            public boolean isValid(Object object, TransformIterator iter) {
                return false;
            }

            public int getParamCount() {
                return 1;
            }
        });
    }

    protected void doCheckNext() {
        super.doCheckNext();
        if (hasNext)
            processed.add(lastAcquired);
        else
            processed.clear(); // no reasons to keep
    }
}
