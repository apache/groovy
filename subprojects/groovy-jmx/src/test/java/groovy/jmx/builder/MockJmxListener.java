package groovy.jmx.builder;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * Created by IntelliJ IDEA.
 * User: paulk
 * Date: 18/07/2010
 * Time: 12:16:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockJmxListener implements MockJmxListenerMBean, NotificationListener {

    public void handleNotification(Notification note, Object handback) {
        System.out.println("Notification " + note.toString() + " received");
    }

    public String getObjectName() {
        return null;
    }

    public void makeObject() {
        // do nothing
    }
}

interface MockJmxListenerMBean {
    String getObjectName();

    void makeObject();
}