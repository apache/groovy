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
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.Iterator;
import java.util.Map;

/**
 * Iterator, which filters content of underling interator either with Filter or Closure.
 * <p/>
 * In fact, FilterIterator is special version of TransformIterator. It means put,
 * breakIteration and continueIteration can be used during invokation of Filter.isValid()
 * but in this case return value of Filter.isValid() is ignored.
 * <p/>
 * See TransformIterator for more details
 */
public class FilterIterator extends TransformIterator {

    /**
     * Interface provided to FilterIterator in order to filter objects.
     */
    public static interface Filter extends ParamCountContract {
        boolean isValid(Object params, TransformIterator iter);
    }

    public FilterIterator(Iterator delegate, Map vars, final Filter filter) {
        super(delegate, vars, (Transformer) null);
        setFilter(filter);
    }

    public FilterIterator(Iterator delegate, Map vars, final Closure closure) {
        this(delegate, vars, (Filter) null);
        setFilter(new Filter() {
            Closure myClosure = adoptClosure(closure);

            public boolean isValid(Object object, TransformIterator iter) {
                return DefaultTypeTransformation.booleanUnbox(object instanceof Object[] ? myClosure.call((Object[]) object) : myClosure.call(object));
            }

            public int getParamCount() {
                return myClosure.getMaximumNumberOfParameters();
            }
        });
    }

    private void setFilter(final Filter filter) {
        this.transformer = new Transformer() {
            public Object transform(Object params, TransformIterator iter) throws Throwable {
                boolean res = filter.isValid(params, iter);
                if (!isQueueEmpty()) {
                    return null;
                }

                if (!res)
                    iter.continueIteration();

                if (params instanceof Object[]) {
                    Object[] p = (Object[]) params;
                    for (int i = 0; i != p.length; ++i)
                        put(p[i]);
                    return null;
                }

                return params;
            }

            public int getParamCount() {
                return filter.getParamCount();
            }
        };
    }

}
