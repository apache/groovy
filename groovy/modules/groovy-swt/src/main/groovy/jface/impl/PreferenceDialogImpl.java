/*
 * Created on Feb 20, 2004
 *
 */
package groovy.jface.impl;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;


/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class PreferenceDialogImpl extends PreferenceDialog {
    public PreferenceDialogImpl(Shell shell, PreferenceManager pm) {
        super(shell, pm);
        // this.parentShell = shell;
    }

    protected void handleSave() {
        super.handleSave();
        /*
         * try { if (handleSave != null) { handleSave.run(context, output); }
         * else { invokeBody(output); } } catch (JellyTagException e) {
         * log.error(e);
         */
    }
}
