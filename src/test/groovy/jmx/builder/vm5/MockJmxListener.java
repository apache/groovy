package groovy.jmx.builder.vm5;

import javax.management.Notification;
import javax.management.NotificationListener;

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
    public String getObjectName();

    public void makeObject();
}