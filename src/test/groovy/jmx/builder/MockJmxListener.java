package groovy.jmx.builder;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * Created by IntelliJ IDEA.
 * User: VVivien
 * Date: Dec 19, 2008
 * Time: 1:19:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockJmxListener implements MockJmxListenerMBean, NotificationListener {

    public void handleNotification(Notification note, Object handback) {
        System.out.println("Notification " + note.toString() + " received");
    }

    public String getObjectName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void makeObject() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

interface MockJmxListenerMBean {
    public String getObjectName();

    public void makeObject();
}