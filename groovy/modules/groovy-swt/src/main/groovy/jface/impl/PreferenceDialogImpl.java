/*
 * Created on Feb 20, 2004
 *  
 */
package groovy.jface.impl;

import groovy.lang.Closure;
import groovy.swt.ClosureSupport;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class PreferenceDialogImpl extends PreferenceDialog implements
        ClosureSupport {

    private Closure closure;

    public PreferenceDialogImpl(Shell shell, PreferenceManager pm) {
        super(shell, pm);
    }

    protected void handleSave() {
        super.handleSave();
        closure.call();
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