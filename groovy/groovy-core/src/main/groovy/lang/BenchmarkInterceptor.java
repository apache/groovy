package groovy.lang;

import java.util.*;

/**
 * Interceptor that registers the timestamp of each method call
 * before and after invocation.
 */
public class BenchmarkInterceptor implements Interceptor {

    protected Map calls = new HashMap(); // keys to list of invokation times and before and after


    public Map getCalls() {
        return calls;
    }
    public void reset() {
        calls = new HashMap();
    }

    public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        if (!calls.containsKey(methodName)) calls.put(methodName, new LinkedList());
        ((List) calls.get(methodName)).add(new Long(System.currentTimeMillis()));

        return null;
    }

    public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        ((List) calls.get(methodName)).add(new Long(System.currentTimeMillis()));
        return result;
    }

    public boolean doInvoke() {
        return true;
    }

    /**
     * @return a list of lines, each item is [methodname, numberOfCalls, accumulatedTime]
     */
    public List statistic() {
        List result = new LinkedList();
        for (Iterator iter = calls.keySet().iterator(); iter.hasNext();) {
            Object[] line = new Object[3];
            result.add(line);
            line[0] = (String) iter.next();
            List times = (List) calls.get(line[0]);
            line[1] = new Integer(times.size() / 2);
            int accTime = 0;
            for (Iterator it = times.iterator(); it.hasNext();) {
                Long start = (Long) it.next();
                Long end = (Long) it.next();
                accTime += end.longValue() - start.longValue();
            }
            line[2] = new Long(accTime);
        }
        return result;
    }
}
