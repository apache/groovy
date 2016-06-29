/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package groovy.lang;

import org.codehaus.groovy.runtime.IteratorClosureAdapter;

import java.util.List;

import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareEqual;
import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareGreaterThan;
import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareGreaterThanEqual;
import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareLessThan;
import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareLessThanEqual;
import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareNotEqual;
import static org.codehaus.groovy.runtime.dgmimpl.NumberNumberPlus.plus;

/**
 * Class containing step methods similar to what can be done with Ranges but only supporting
 * Numbers and with any Number as the step size not just int and also limiting metaprogramming
 * for speed.
 */
public class StepUtil {
    /**
     * Produce a list of Numbers starting with <code>from</code>
     * and changing by the <code>step</code> value until the <code>toInclusive</code> Number is
     * reached or has been passed.
     *
     * @param from the starting Number
     * @param toInclusive the ending Number
     * @param step the step size
     * @return the resulting list of Numbers
     */
    public static List<Number> step(Number from, Number toInclusive, Number step) {
        IteratorClosureAdapter<Number> adapter = new IteratorClosureAdapter<Number>(null);
        step(from, toInclusive, step, adapter);
        return adapter.asList();
    }

    /**
     * Call a <code>closure</code> passing in a Number. The Numbers start with <code>from</code>
     * and change by the <code>step</code> value until the <code>toInclusive</code> Number is
     * reached or has been passed.
     *
     * @param from the starting Number
     * @param toInclusive the ending Number
     * @param step the step size
     * @param closure the Closure to call passing in the next Number
     */
    public static void step(Number from, Number toInclusive, Number step, Closure closure) {
        boolean notEqual = compareNotEqual(from, toInclusive);
        if (compareEqual(step, 0)) {
            if (notEqual) {
                throw new GroovyRuntimeException("Infinite loop detected due to step size of 0");
            }
            return; // nothing to do
        }
        boolean ascending = compareLessThan(from, toInclusive);
        boolean ascendingStep = compareGreaterThan(step, 0);
        if (notEqual) {
            if (ascending ^ ascendingStep) {
                throw new GroovyRuntimeException("Infinite loop detected due to incorrectly signed step value " +
                        "(can't get from " + from + " to " + toInclusive + " with a " + (ascendingStep ? "posi" :
                        "nega") + "tive step size: " + step + ")");
            }
        } else {
            ascending = ascendingStep;
        }
        Number value = from;
        while (ascending
                ? compareLessThanEqual(value, toInclusive)
                : compareGreaterThanEqual(value, toInclusive)) {
            closure.call(value);
            value = plus(value, step);
        }
    }
}
