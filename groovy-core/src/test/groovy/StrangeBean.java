package groovy;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This bean should encompass all legal, but bad, JavaBeans practices
 */
public class StrangeBean {
    
    Set strangeListeners;
    
    /** Creates a new instance of StrangeBean */
    public StrangeBean() {
        strangeListeners = new LinkedHashSet();
    }
    
    public void addStrangeEventListener(StrangeEventListener listener) {
        strangeListeners.add(listener);
    }
    
    public void removeStrangeEventListener(StrangeEventListener listener) {
        strangeListeners.remove(listener);
    }
    
    public StrangeEventListener[] getStrangeEventListeners() {
        return (StrangeEventListener[]) strangeListeners.toArray(new StrangeEventListener[strangeListeners.size()]);
    }
    
    public void somethingStrangeHappened(String what, String where) {
        Iterator iter = strangeListeners.iterator();
        while (iter.hasNext()) {
            StrangeEventListener listener = (StrangeEventListener) iter.next();
            listener.somethingStrangeHappened(what, where);
        }
    }
    
}
