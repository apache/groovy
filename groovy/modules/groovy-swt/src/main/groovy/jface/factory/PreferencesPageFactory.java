package groovy.jface.factory;

import groovy.jface.impl.PreferenceDialogImpl;
import groovy.jface.impl.PreferencePageFieldEditorImpl;
import groovy.lang.MissingPropertyException;
import groovy.swt.InvalidParentException;
import groovy.swt.factory.AbstractSwtFactory;
import groovy.swt.factory.SwtFactory;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class PreferencesPageFactory extends AbstractSwtFactory implements
        SwtFactory {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory
            .getLog(PreferencesPageFactory.class);

    /*
     * @see groovy.swt.impl.SwtFactory#newInstance(java.util.Map,
     *           java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException {

        // check location
        if (!(parent instanceof PreferenceDialog)) { throw new InvalidParentException(
                "preferenceDialog"); }

        PreferenceDialogImpl preferenceDialogImpl = (PreferenceDialogImpl) parent;

        String filename = (String) properties.remove("filename");
        if (filename == null) { throw new MissingPropertyException("filename",
                PreferencePage.class); }

        String title = (String) properties.remove("title");
        if (title == null) { throw new MissingPropertyException("title",
                PreferencePage.class); }

        // build new PreferenceNode with same title as the PreferencePage
        PreferenceNode node = new PreferenceNode(title);

        // build new PreferencePage
        FieldEditorPreferencePage page = new PreferencePageFieldEditorImpl(
                title);
        PreferenceStore preferenceStore = new PreferenceStore(filename);
        
        try {
            preferenceStore.load();
        } catch (IOException e) {
            log.error(e);
        }
        page.setPreferenceStore(preferenceStore);

        // add node to PreferenceManager
        node.setPage(page);
        preferenceDialogImpl.getPreferenceManager().addToRoot(node);

        return page;
    }

}
