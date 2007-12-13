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

import groovy.lang.Closure;
import groovy.lang.MissingPropertyException;

import java.util.Iterator;
import java.util.Map;

public class ConcurrentIterator extends BlockingQueuedIterator {

    public ConcurrentIterator(Iterator delegate, Map args, TransformIterator.Transformer transformer) {
        super(args);
        init(delegate, transformer);
    }

    public ConcurrentIterator(Iterator delegate, Map args, Closure closure) {
        super(args);
        init(delegate, closure);
    }

    public void breakIteration() {
        throw new UnsupportedOperationException("breakIteration doesn't supported by ConcurrentIterator");
    }

    private void init(Iterator delegate, TransformIterator.Transformer transformer) {
        for (int i = 0; i != poolSize; ++i)
            attachProducer(new WorkerThread(delegate, transformer));
    }

    private void init(Iterator delegate, Closure closure) {
        for (int i = 0; i != poolSize; ++i)
            attachProducer(new WorkerThread(delegate, closure));
    }

    private class WorkerThread extends BlockingQueuedIterator.WorkerThread {
        TransformIterator inner;

        public WorkerThread(Iterator delegate, final TransformIterator.Transformer transformer) {
            inner = new InnerTransformIterator(delegate, transformer);
        }

        public WorkerThread(Iterator delegate, final Closure closure) {
            inner = new InnerTransformIterator(delegate, closure);
        }

        public void run() {
            try {
                while (true) {
                    if (!inner.hasNext())
                        break;

                    put(inner.next());
                    while (!inner.isQueueEmpty()) {
                        put(inner.get());
                    }

//                    Thread.yield();
                }
            }
            finally {
                detachProducer();
            }
        }
    }

    private class InnerTransformIterator extends TransformIterator {
        public InnerTransformIterator(Iterator delegate, Transformer transformer) {
            super(new SynchronizedIterator(delegate, transformer.getParamCount()), null, transformer);
        }

        public InnerTransformIterator(Iterator delegate, Closure closure) {
            super(new SynchronizedIterator(delegate, closure.getMaximumNumberOfParameters()), null, closure);
        }

        protected Object prepareParams(Iterator delegate, int paramCount) {
            return delegate.next();
        }

        protected void doCheckNext() {
            try {
                super.doCheckNext();
            }
            catch (Throwable t) {
                hasNext = true;
                stopped = true;
                if (!isQueueEmpty())
                    put(t);
                else
                    lastAcquired = t;
            }
        }

        public Object getProperty(String property) {
            try {
                return super.getProperty(property);
            }
            catch (MissingPropertyException mpe) {
                return ConcurrentIterator.this.getProperty(property);
            }
        }

        public void setProperty(String property, Object newValue) {
            try {
                super.setProperty(property, newValue);
            }
            catch (MissingPropertyException mpe) {
                ConcurrentIterator.this.setProperty(property, newValue);
            }
        }

    }

    private class SynchronizedIterator extends AbstractIterator {
        private final Iterator delegate;
        private final int paramCount;

        int currentInputBlock = -1;

        SynchronizedIterator(Iterator delegate, int paramCount) {
            super(null);
            this.delegate = delegate;
            this.paramCount = paramCount;
        }

        protected void doCheckNext() {
            synchronized (delegate) {
                if (!delegate.hasNext()) {
                    hasNext = false;
                    return;
                }

                hasNext = true;
                lastAcquired = prepareParams(delegate, paramCount);
            }
        }
    }
}
