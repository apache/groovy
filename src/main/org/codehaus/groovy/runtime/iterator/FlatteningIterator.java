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

import java.util.*;

public class FlatteningIterator extends AbstractIterator {

    ArrayList stack = new ArrayList();

    public FlatteningIterator(Iterator delegate) {
        stack.add(delegate);
    }

    protected void doCheckNext() {
        while (true) {
            int lastIndex = stack.size() - 1;
            Iterator iter = (Iterator) stack.get(lastIndex);
            hasNext = iter.hasNext();
            if (!hasNext) {
                stack.remove(lastIndex);
                if (stack.size() > 0)
                    continue;
                else
                    return;
            }

            Object next = iter.next();

            Iterator flattened = flatten(next);
            if (flattened != null) {
                stack.add(flattened);
                continue;
            }

            this.lastAcquired = next;
            break;
        }
    }

    protected Iterator flatten(Object next) {
        return null;
    }

    public static class FlatteningAllLevelsIterator extends FlatteningIterator {

        public static final int MAPVALUES = 1;
        public static final int MAPKEYS = 2;
        private final int strategy;

        public FlatteningAllLevelsIterator(Iterator delegate, int strategy) {
            super(delegate);
            this.strategy = strategy;
        }

        public FlatteningAllLevelsIterator(Iterator delegate) {
            this(delegate, 0);
        }

        protected Iterator flatten(Object next) {
            if (next instanceof Iterator)
                return (Iterator) next;

            if (next instanceof Collection)
                return ((Collection) next).iterator();

            if (next instanceof Map) {
                Map map = (Map) next;
                switch (strategy) {
                    case MAPVALUES:
                        return map.values().iterator();

                    case MAPKEYS:
                        return map.keySet().iterator();

                    default:
                        return map.entrySet().iterator();
                }
            }

            if (next instanceof Enumeration)
                return new EnumerationIterator((Enumeration) next);

            return null;
        }
    }

    public static class FlatteningOneLevelIterator extends FlatteningAllLevelsIterator {

        public FlatteningOneLevelIterator(Iterator delegate, int strategy) {
            super(delegate, strategy);
        }

        public FlatteningOneLevelIterator(Iterator delegate) {
            super(delegate);
        }

        protected Iterator flatten(Object next) {
            if (stack.size() == 1)
                return super.flatten(next);

            return null;
        }
    }
}
