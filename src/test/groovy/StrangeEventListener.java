package groovy;

import java.util.EventListener;
import javax.swing.event.ChangeEvent;

public interface StrangeEventListener extends EventListener {

    /**
     * Acorrding to section 6.4.1 of the JavaBeans spec this is legal, but not
     * good practice.  We need to test what can be done not what should be done
     */
    void somethingStrangeHappened(String what, String where);
    
    void somethingChanged(ChangeEvent changeEvent);
    
}
