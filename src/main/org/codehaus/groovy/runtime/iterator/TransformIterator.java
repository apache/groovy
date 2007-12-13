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
import groovy.lang.GroovyRuntimeException;

import java.util.Iterator;
import java.util.Map;

/**
 * Iterator, which takes one or several sequential elements of input iterator
 * and transforms to one or several output elements.
 * <p/>
 * Real transformation delegated to Transformer provided during initialization.
 * Number of input elements defined by transformer.getParamCount ()
 * <p/>
 * Number of output elements is defined by transformer according to following rule
 * During invocation of transformer.transform(...)
 * - if transformer calls iter.put (theObject)
 * , theObject will be added to output sequence
 * put () can be called as many times as needed
 * - if transformer calls iter.continueIteration ()
 * all already outputed elements will be kept in output sequences but this step of transformation will be finished.
 * NOTE: call to continueIteration will generate internal exception
 * - if transformer calls iter.breakIteration ()
 * all already outputed elements will be kept in output sequences but after them the iteration will be finished.
 * NOTE: call to breakIteration will generate internal exception
 * - if transformer calls iter.breakIterationImmidiately ()
 * all already outputed elements (if any) will be dropped out of output sequences and the iteration will be finished.
 * NOTE: call to breakIterationImidiately will generate internal exception
 * - if non-of the above happen
 * return value of transformer.transform(...) will be added to output sequence
 */
public class TransformIterator extends QueuedIterator {

    protected final Iterator delegate;
    protected Transformer transformer;
    protected boolean stopped;

    /**
     * Interface provided to TransformIterator in order to make transformation.
     */
    public static interface Transformer extends ParamCountContract {

        /**
         * Makes transformation
         *
         * @param object element of underling iterator
         *               or array of sequential elements if getParamCount () returns value greater than 1
         * @param iter   iterator requesting transformation
         * @return result of transformation. Ignored if myIter.put was called
         * @throws Throwable
         */
        Object transform(Object object, TransformIterator iter) throws Throwable;

    }

    public TransformIterator(java.util.Iterator delegate, Map vars, Transformer transformer) {
        super(vars);
        this.delegate = delegate;
        this.transformer = transformer;
    }

    public TransformIterator(java.util.Iterator delegate, Map vars, final Closure closure) {
        super(vars);
        this.delegate = delegate;
        transformer = new ClosureTransformer(adoptClosure(closure));
    }

    protected void doCheckNext() {
        while (true) {
            if (!isQueueEmpty()) {
                hasNext = true;
                lastAcquired = get();
                break;
            }

            if (stopped) {
                hasNext = false;
                break;
            }

            hasNext = delegate.hasNext();
            if (!hasNext) {
                break;
            }

            try {
                Object res;
                res = transformer.transform(prepareParams(delegate, transformer.getParamCount()), this);
                if (!isQueueEmpty())
                    lastAcquired = get();
                else
                    lastAcquired = res;

                break;
            }
            catch (GroovyRuntimeException g) {
                GroovyRuntimeException orig = g;
                while (g instanceof GroovyRuntimeException && g.getCause() != null && g.getCause() != g && g.getCause() instanceof GroovyRuntimeException)
                    g = (GroovyRuntimeException) g.getCause();
                if (g instanceof BreakIterationException) {
                    break;
                }
                if (g instanceof ContinueIterationException) {
                    continue;
                }
                throw orig;
            }
            catch (Throwable t) {
                if (!(t instanceof GroovyRuntimeException)) {
                    throw new GroovyRuntimeException(t);
                }
                throw (GroovyRuntimeException) t;
            }
        }
    }

    static class KeysIterator extends TransformIterator {
        public KeysIterator(java.util.Iterator delegate, Map vars) {
            super(delegate, vars, new KeysTransformer());
        }

        private static class KeysTransformer implements Transformer {
            public Object transform(Object object, TransformIterator iter) {
                return ((Map.Entry) object).getKey();
            }

            public int getParamCount() {
                return 1;
            }
        }
    }

    static class ValuesIterator extends TransformIterator {
        public ValuesIterator(java.util.Iterator delegate, Map vars) {
            super(delegate, vars, new ValuesTransformer());
        }

        private static class ValuesTransformer implements Transformer {
            public Object transform(Object object, TransformIterator iter) {
                return ((Map.Entry) object).getValue();
            }

            public int getParamCount() {
                return 1;
            }
        }
    }

    /**
     * Standard implementation of Closure based Transformer
     */
    static class ClosureTransformer implements Transformer, ParamCountContract {
        private final Closure closure;

        public ClosureTransformer(Closure closure) {
            this.closure = closure;
        }

        public Object transform(Object object, TransformIterator iter) {
            return callClosure(closure, object);
        }

        public int getParamCount() {
            return closure.getMaximumNumberOfParameters();
        }
    }

    public static abstract class OneParamTransformer implements Transformer {
        public int getParamCount() {
            return 1;
        }
    }

    public void breakIteration() {
        stopped = true;
        hasNext = !isQueueEmpty();
        if (hasNext)
            lastAcquired = get();
        throw new BreakIterationException();
    }

    public void breakIterationImmidiately() {
        clearQueue();
        breakIteration();
    }

    protected Map findVar(String property) {
        Map result = super.findVar(property);
        if (result == null && delegate instanceof AbstractIterator)
            return ((AbstractIterator) delegate).findVar(property);
        return result;
    }
}
