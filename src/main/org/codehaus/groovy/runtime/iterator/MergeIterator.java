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

import java.util.Iterator;

public class MergeIterator extends AbstractIterator {
    protected final Iterator first, second;
    boolean isSecond = false;

    public MergeIterator(Iterator first, Iterator second) {
        super();
        this.first = first;
        this.second = second;
    }

    protected void doCheckNext() {
        if (isSecond) {
            if (!second.hasNext()) {
                hasNext = first.hasNext();
                if (hasNext)
                    lastAcquired = first.next();
            } else {
                hasNext = true;
                lastAcquired = second.next();
            }
            isSecond = false;
        } else {
            if (!first.hasNext()) {
                hasNext = second.hasNext();
                if (hasNext)
                    lastAcquired = second.next();
            } else {
                hasNext = true;
                lastAcquired = first.next();
            }
            isSecond = true;
        }
    }
}
