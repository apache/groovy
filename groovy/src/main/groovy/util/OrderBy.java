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
package groovy.util;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A helper class for sorting objects via a closure to return the field
 * or operation on which to sort.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class OrderBy implements Comparator {

    private List closures;

    public OrderBy() {
        this.closures = new ArrayList();
    }

    public OrderBy(Closure closure) {
        this();
        closures.add(closure);
    }

    public OrderBy(List closures) {
        this.closures = closures;
    }

    public void add(Closure closure) {
        closures.add(closure);
    }

    public int compare(Object object1, Object object2) {
        for (Iterator iter = closures.iterator(); iter.hasNext();) {
            Closure closure = (Closure) iter.next();
            Object value1 = closure.call(object1);
            Object value2 = closure.call(object2);

            if (value1 == value2) {
                continue;
            }
            if (value1 == null) {
                return -1;
            }
            if (value1 instanceof Comparable) {
                Comparable c1 = (Comparable) value1;
                int result = c1.compareTo(value2);
                if (result == 0) {
                    continue;
                } else {
                    return result;
                }
            }
            if (value1.equals(value2)) {
                continue;
            }
            return value1.hashCode() - value2.hashCode();
        }
        return 0;
    }
}
