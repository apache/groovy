/*
 * Created on Feb 20, 2004
 *
 */
package groovy.jface.factory;

import groovy.jface.impl.WizardDialogImpl;
import groovy.jface.impl.WizardImpl;
import groovy.swt.SwtUtils;
import groovy.swt.factory.AbstractSwtFactory;
import groovy.swt.factory.SwtFactory;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class WizardDialogFactory extends AbstractSwtFactory implements SwtFactory {

    /*
     * @see groovy.swt.factory.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent) throws GroovyException {
        Shell parentShell = SwtUtils.getParentShell(parent);
        Wizard wizard = new WizardImpl();
        WizardDialog wizardDialog = new WizardDialogImpl(parentShell, wizard);
        return wizardDialog;
    }
}