package groovy.swt.impl;

import groovy.lang.Closure;
import groovy.swt.ClosureSupport;

import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;

/**
 * This implementation adds a ProgressListener to a browser widget
 * 
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 */
public class ProgressListenerImpl implements ProgressListener, ClosureSupport {

    private Closure closure;

    private String type;

    /**
     * @param type
     */
    public ProgressListenerImpl(String type) {
        this.type = type;
    }

    public Closure getClosure() {
        return closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    /*
     * @see org.eclipse.swt.browser.ProgressListener#changed(org.eclipse.swt.browser.ProgressEvent)
     */
    public void changed(ProgressEvent event) {

        if (closure == null) { 
            throw new NullPointerException(
                "No closure has been configured for this Listener"); 
        }

        if ("changed".equals(type)) {
            closure.setProperty("event", new CustomProgressEvent(event));
            closure.call(event);
        }

    }

    /*
     * @see org.eclipse.swt.browser.ProgressListener#completed(org.eclipse.swt.browser.ProgressEvent)
     */
    public void completed(ProgressEvent event) {
        if (closure == null) { 
            throw new NullPointerException(
                "No closure has been configured for this Listener"); 
        }

        if ("completed".equals(type)) {
            closure.setProperty("event", new CustomProgressEvent(event));
            closure.call(event);
        }

    }

    public class CustomProgressEvent {

        private ProgressEvent event;

        public CustomProgressEvent(ProgressEvent event) {
            this.event = event;
        }

        public int getCurrent() {
            return event.current;
        }

        public int getTotal() {
            return event.total;
        }
    }

}