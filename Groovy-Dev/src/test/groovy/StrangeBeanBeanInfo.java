package groovy;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;

public class StrangeBeanBeanInfo extends SimpleBeanInfo {

    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
            Method[] events = StrangeEventListener.class.getMethods();
            Method addListener = StrangeBean.class.getMethod("addStrangeEventListener", new Class[] {StrangeEventListener.class});
            Method removeListener = StrangeBean.class.getMethod("removeStrangeEventListener", new Class[] {StrangeEventListener.class});
            Method getListeners = StrangeBean.class.getMethod("getStrangeEventListeners", new Class[0]);
            
            return new EventSetDescriptor[] {
                new EventSetDescriptor( 
                        "strangeEvent",
                        StrangeEventListener.class, 
                        events,
                        addListener,
                        removeListener,
                        getListeners)
            };
        } catch (Exception ie) {
            ie.printStackTrace(System.out);
            return super.getEventSetDescriptors();
        }
    }
}
