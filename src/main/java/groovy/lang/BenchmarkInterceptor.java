/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.lang;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Interceptor that registers the timestamp of each method call
 * before and after invocation. The timestamps are stored internally
 * and can be retrieved through the with the <pre>getCalls()</pre> 
 * and <pre>statistic()</pre> API.
 * <p>
 * Example usage:
 * <pre>
 * def proxy = ProxyMetaClass.getInstance(ArrayList.class)
 * proxy.interceptor = new BenchmarkInterceptor()
 * proxy.use {
 *     def list = (0..10000).collect{ it }
 *     4.times { list.size() }
 *     4000.times { list.set(it, it+1) }
 * }
 * proxy.interceptor.statistic()
 * </pre>
 * Which produces the following output: 
 * <pre>
 * [[size, 4, 0], [set, 4000, 21]]
 * </pre>
 */
public class BenchmarkInterceptor implements Interceptor {

    protected Map calls = new LinkedHashMap(); // keys to list of invocation times and before and after

    /**
    * Returns the raw data associated with the current benchmark run. 
    */ 
    public Map getCalls() {
        return calls;
    }
    
    /**
    * Resets all the benchmark data on this object. 
    */
    public void reset() {
        calls = new HashMap();
    }
    /**
     * This code is executed before the method is called.
     * @param object        receiver object for the method call
     * @param methodName    name of the method to call
     * @param arguments     arguments to the method call
     * @return null
     * relays this result.
     */
    public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        if (!calls.containsKey(methodName)) calls.put(methodName, new LinkedList());
        ((List) calls.get(methodName)).add(System.currentTimeMillis());

        return null;
    }
    /**
     * This code is executed after the method is called.
     * @param object        receiver object for the called method
     * @param methodName    name of the called method
     * @param arguments     arguments to the called method
     * @param result        result of the executed method call or result of beforeInvoke if method was not called
     * @return result
     */
    public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        ((List) calls.get(methodName)).add(System.currentTimeMillis());
        return result;
    }

    /**
     * The call should be invoked separately
     * @return true
     */
    public boolean doInvoke() {
        return true;
    }

    /**
     * Returns benchmark statistics as a List&lt;Object[]&gt;. 
     * AccumulateTime is measured in milliseconds and is as accurate as
     * System.currentTimeMillis() allows it to be. 
     * @return a list of lines, each item is [methodname, numberOfCalls, accumulatedTime]
     */
    public List statistic() {
        List result = new LinkedList();
        for (Object o : calls.keySet()) {
            Object[] line = new Object[3];
            result.add(line);
            line[0] = o;
            List times = (List) calls.get(line[0]);
            line[1] = times.size() / 2;
            int accTime = 0;
            for (Iterator it = times.iterator(); it.hasNext(); ) {
                Long start = (Long) it.next();
                Long end = (Long) it.next();
                accTime += end - start;
            }
            line[2] = (long) accTime;
        }
        return result;
    }
}
