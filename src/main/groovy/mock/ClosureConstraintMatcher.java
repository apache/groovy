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

package groovy.mock;

import groovy.lang.Closure;
import com.mockobjects.constraint.Constraint;

/**
 * 
 * @author Joe Walnes
 * @author Chris Stevenson
 * @version $Revision$
 */
public class ClosureConstraintMatcher implements Constraint {
    private Closure closure;
    private String message = "closure";

    public ClosureConstraintMatcher(Closure closure) {
        this.closure = closure;
    }

    public boolean eval(Object object) {
        try {
            closure.call((Object[])object);
            return true;
        }
        catch (AssertionError e) {
            message = e.getMessage();
            return false;
        }
    }

    public String toString() {
        return message;
    }

}
