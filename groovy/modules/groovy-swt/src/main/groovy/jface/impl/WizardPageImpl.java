/*
 * Created on Feb 21, 2004
 *
 */
package groovy.jface.impl;

import groovy.lang.Closure;
import groovy.swt.ClosureSupport;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of a WizardPage method createControl is called on
 * Dialog.open()
 * 
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class WizardPageImpl extends WizardPage implements ClosureSupport {
    private Closure closure;

    public WizardPageImpl(String title) {
        super(title);
    }

    public void createControl(Composite parent) {
        if (closure == null) {
            throw new NullPointerException(
            "No closure has been configured for this WizardPage");
        }
        Composite composite = (Composite) closure.call(parent);
        setControl(composite);
    }

    public Closure getClosure() {
        return closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }
}
