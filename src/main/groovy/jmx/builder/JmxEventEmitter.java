package groovy.jmx.builder;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import java.util.concurrent.atomic.AtomicLong;

public class JmxEventEmitter extends NotificationBroadcasterSupport implements JmxEventEmitterMBean {
    private String event;
    private String message;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long send(Object data) {
        long seq = NumberSequencer.getNextSequence();
        Notification note = new Notification(
                this.getEvent(),
                this,
                seq,
                System.currentTimeMillis(),
                "Event notification " + this.getEvent()
        );
        note.setUserData(data);
        super.sendNotification(note);
        return seq;
    }

    private static class NumberSequencer {
        private static AtomicLong num;

        static {
            num = new AtomicLong(0);
        }

        public static long getNextSequence() {
            return num.incrementAndGet();
        }
    }
}
