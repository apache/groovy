package groovy.swt.impl;

import groovy.lang.Closure;
import groovy.swt.ClosureSupport;

import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;

/**
 * This implementation adds a StatusTextListener to a browser widget
 * 
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class StatusTextListenerImpl implements StatusTextListener, ClosureSupport {

    private Closure closure;

    public Closure getClosure()
    {
        return closure;
    }

    public void setClosure(Closure closure)
    {
        this.closure = closure;
    }

    /*
     * @see org.eclipse.swt.browser.StatusTextListener#changed(org.eclipse.swt.browser.StatusTextEvent)
     */
    public void changed(StatusTextEvent event)
    {
        if (closure == null) { 
            throw new NullPointerException(
                "No closure has been configured for this Listener"); 
        }
        
        closure.setProperty("event", new CustomStatusTextEvent(event));
        closure.call(event);
    }

	public class CustomStatusTextEvent
	{
		private StatusTextEvent event;
		
		public CustomStatusTextEvent(StatusTextEvent event)
		{
			this.event = event;
		}
		
		public String getText()
		{
			return event.text;
		}
	}

	
}
