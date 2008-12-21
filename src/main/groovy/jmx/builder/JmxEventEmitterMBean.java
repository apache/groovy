package groovy.jmx.builder;

/**
 * Created by IntelliJ IDEA.
 * User: vvivien
 * Date: Dec 2, 2008
 * Time: 9:57:48 AM
 * To change this template use File | Settings | File Templates.
 */
public interface JmxEventEmitterMBean {
    String getEvent();

    void setEvent(String event);

    long send(Object data);
}
