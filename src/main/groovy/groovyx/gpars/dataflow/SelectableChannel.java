package groovyx.gpars.dataflow;

import groovy.lang.Closure;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.expression.DataflowExpression;

/**
 * Created with IntelliJ IDEA.
 * User: Vaclav
 * Date: 5.7.13
 * Time: 22:17
 * To change this template use File | Settings | File Templates.
 */
public interface SelectableChannel<T> {
    /**
     * Send all pieces of data bound in the future to the provided stream when it becomes available.     *
     *
     * @param closure closure to execute when data becomes available. The closure should take at most one argument.
     */
    <V> void wheneverBound(Closure<V> closure);

    /**
     * Send all pieces of data bound in the future to the provided stream when it becomes available.
     *
     * @param stream stream where to send result
     */
    void wheneverBound(MessageStream stream);

    /**
     * Retrieves the value at the head of the buffer. Returns null, if no value is available.
     *
     * @return The value bound to the DFV at the head of the stream or null
     * @throws InterruptedException If the current thread is interrupted
     */
    @SuppressWarnings({"ClassReferencesSubclass"}) DataflowExpression<T> poll() throws InterruptedException;
}
