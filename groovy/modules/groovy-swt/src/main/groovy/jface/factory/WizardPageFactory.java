/*
 * Created on Feb 20, 2004
 *
 */
package groovy.jface.factory;

import groovy.jface.impl.WizardDialogImpl;
import groovy.jface.impl.WizardPageImpl;
import groovy.lang.MissingPropertyException;
import groovy.swt.InvalidParentException;
import groovy.swt.factory.AbstractSwtFactory;
import groovy.swt.factory.SwtFactory;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class WizardPageFactory extends AbstractSwtFactory implements SwtFactory {

    /*
     * @see groovy.swt.factory.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
    throws GroovyException {

        // check location
        if (!(parent instanceof WizardDialog)) {
            throw new InvalidParentException("wizardDialog");
        }
        WizardDialogImpl wizardDialog = (WizardDialogImpl) parent;

        // check for missing attributes
        String title = (String) properties.get("title");
        if (title == null) {
            throw new MissingPropertyException("title", 
            WizardPage.class);
        }

        // get WizardPageImpl
        WizardPageImpl page = new WizardPageImpl(title);
        setBeanProperties(page, properties);

        // get Wizard
        WizardDialogImpl dialog = (WizardDialogImpl) parent;
        Wizard wizard = (Wizard) dialog.getWizard();

        // add WizardPage to the Wizard
        wizard.addPage(page);
        return page;
    }
}
