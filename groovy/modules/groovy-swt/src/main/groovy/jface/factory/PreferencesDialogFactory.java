package groovy.jface.factory;

import groovy.jface.impl.PreferenceDialogImpl;
import groovy.swt.InvalidParentException;
import groovy.swt.SwtUtils;
import groovy.swt.factory.AbstractSwtFactory;
import groovy.swt.factory.SwtFactory;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class PreferencesDialogFactory extends AbstractSwtFactory implements
        SwtFactory {

    /*
     * @see groovy.swt.impl.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException {
        Shell parentShell = SwtUtils.getParentShell(parent);
        if (parent != null) {
            PreferenceManager pm = new PreferenceManager();
            return new PreferenceDialogImpl(parentShell, pm);
        } else {
            throw new InvalidParentException("applicationWindow or shell");
        }
    }
}