/*
 * Created on Feb 15, 2004
 */
package groovy.swt.factory;

import groovy.swt.SwtUtils;
import groovy.swt.impl.ExpansionListenerImpl;
import groovy.swt.impl.HyperLinkListenerImpl;
import groovy.swt.impl.ListenerImpl;
import groovy.swt.impl.LocationListenerImpl;
import groovy.swt.impl.ProgressListenerImpl;
import groovy.swt.impl.StatusTextListenerImpl;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.AbstractHyperlink;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ListenerFactory extends AbstractSwtFactory implements SwtFactory {

    private Class beanClass;

    /**
     * @param class1
     */
    public ListenerFactory(Class beanClass) {
        this.beanClass = beanClass;
    }

    /*
     * @see groovy.swt.impl.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException {
        final String type = (String) properties.remove("type");
        if (parent instanceof Browser) {
            Browser browser = (Browser) parent;
            if (beanClass.equals(LocationListener.class)) {
                LocationListener locationListener = new LocationListenerImpl(
                        type);
                browser.addLocationListener(locationListener);
                return locationListener;
            } else if (beanClass.equals(ProgressListener.class)) {
                ProgressListener progressListener = new ProgressListenerImpl(
                        type);
                browser.addProgressListener(progressListener);
                return progressListener;
            } else if (beanClass.equals(StatusTextListener.class)) {
                StatusTextListener statusTextListener = new StatusTextListenerImpl();
                browser.addStatusTextListener(statusTextListener);
                return statusTextListener;
            }
        } else if (parent instanceof AbstractHyperlink) {
            AbstractHyperlink hyperlink = (AbstractHyperlink) parent;
            IHyperlinkListener hyperLinkListenerImpl = new HyperLinkListenerImpl(
                    type);
            hyperlink.addHyperlinkListener(hyperLinkListenerImpl);
            return hyperLinkListenerImpl;
        } else if (parent instanceof ExpandableComposite) {
            ExpandableComposite expandableComposite = (ExpandableComposite) parent;
            IExpansionListener expansionListener = new ExpansionListenerImpl(
                    type);
            expandableComposite.addExpansionListener(expansionListener);
            return expansionListener;

        } else if (parent instanceof Widget || parent instanceof Viewer) {
            Widget widget = null;
            if (parent instanceof Viewer) {
                widget = ((Viewer) parent).getControl();
            } else {
                widget = (Widget) parent;
            }
            int eventType = getEventType(type);
            if (eventType == 0) { throw new GroovyException(
                    "No event type specified, could not understand: " + type); }
            ListenerImpl listenerImpl = new ListenerImpl();
            widget.addListener(eventType, listenerImpl);
            return listenerImpl;
        }
        throw new GroovyException("No factory found for class: " + beanClass);
    }

    /**
     * Parses the given event type String and returns the SWT event type code
     * 
     * @param type
     *            is the String event type
     * @return the SWT integer event type
     */
    protected int getEventType(String type) throws GroovyException {
        return SwtUtils.parseStyle(SWT.class, type, false);
    }
}