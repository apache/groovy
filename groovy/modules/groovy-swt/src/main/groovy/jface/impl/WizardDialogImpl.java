/*
 * Created on Feb 20, 2004
 *
 */
package groovy.jface.impl;

import groovy.lang.Closure;
import groovy.swt.ClosureSupport;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Provide a public method getWizard and provide access to parentShell
 * 
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class WizardDialogImpl extends WizardDialog implements ClosureSupport {
    private Closure closure;
    private Shell parentShell;

    public WizardDialogImpl(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
        this.parentShell = parentShell;
    }

    public IWizard getWizard() {
        return super.getWizard();
    }

    public Shell getParentShell() {
        return parentShell;
    }

    /*
     * @see groovy.swt.ClosureSupport#getClosure()
     */
    public Closure getClosure() {
        return closure;
    }

    /*
     * @see groovy.swt.ClosureSupport#setClosure(groovy.lang.Closure)
     */
    public void setClosure(Closure closure) {
        this.closure = closure;
    }
}
