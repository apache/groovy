package groovy.jface.impl;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * This is the default implementation for a ApplicationWindow
 * 
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class ApplicationWindowImpl extends ApplicationWindow {

	/**
	 * @param shell
	 */
	public ApplicationWindowImpl(Shell parentShell) {
		super(null);

		// default at all
		addMenuBar();
		addStatusLine();
		addToolBar(SWT.FLAT | SWT.WRAP);
		setBlockOnOpen(true);

		// create window
		create();
	}

	/*
	 * override to make public
	 * 
	 * @see org.eclipse.jface.window.Window#getContents()
	 */
	public Control getContents() {
		return super.getContents();
	}

	/*
	 * override to make public
	 * 
	 * @see org.eclipse.jface.window.ApplicationWindow#getStatusLineManager()
	 */
	public StatusLineManager getStatusLineManager() {
		return super.getStatusLineManager();
	}
}
