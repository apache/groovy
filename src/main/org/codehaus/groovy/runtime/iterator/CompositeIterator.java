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

/**
 * Iterator, which compose content of two iterators.
 * <p/>
 * [a,b] compose with [c,d] becomes [a,b,c,d]
 */
public class CompositeIterator extends AbstractIterator {
    private final Iterator first, second;
    boolean isSecond = false;

    public CompositeIterator(Iterator first, Iterator second) {
        this.first = first;
        this.second = second;
    }

    protected void doCheckNext() {
        if (first.hasNext()) {
            hasNext = true;
            lastAcquired = first.next();
        } else {
            hasNext = second.hasNext();
            if (hasNext)
                lastAcquired = second.next();
        }
    }
}
