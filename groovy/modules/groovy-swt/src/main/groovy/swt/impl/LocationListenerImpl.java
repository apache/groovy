package groovy.swt.impl;

import groovy.lang.Closure;
import groovy.swt.ClosureSupport;

import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;

/**
 * This implementation adds a LocationListener to a browser widget
 * 
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 */
public class LocationListenerImpl implements LocationListener, ClosureSupport {

    private String type;

    private Closure closure;

    /**
     * @param type
     */
    public LocationListenerImpl(String type)
    {
        this.type = type;
    }

    public Closure getClosure()
    {
        return closure;
    }

    public void setClosure(Closure closure)
    {
        this.closure = closure;
    }

    /*
     * @see org.eclipse.swt.browser.LocationListener#changing(org.eclipse.swt.browser.LocationEvent)
     */
    public void changing(LocationEvent event)
    {
        if (closure == null) { throw new NullPointerException(
                "No closure has been configured for this Listener"); 
        }

        if ("changing".equals(type))
        {
            closure.setProperty("event", new CustomLocationEvent(event));
            closure.call(event);
        }

    }

    /*
     * @see org.eclipse.swt.browser.LocationListener#changed(org.eclipse.swt.browser.LocationEvent)
     */
    public void changed(LocationEvent event)
    {
        if (closure == null) { 
            throw new NullPointerException(
                "No closure has been configured for this Action"); 
        }

        if ("changed".equals(type))
        {
            closure.setProperty("event", new CustomLocationEvent(event));
            closure.call(event);
        }
    }

    public class CustomLocationEvent {

        private LocationEvent event;

        public CustomLocationEvent(LocationEvent event)
        {
            this.event = event;
        }

        public String getLocation()
        {
            return event.location;
        }
    }

}
