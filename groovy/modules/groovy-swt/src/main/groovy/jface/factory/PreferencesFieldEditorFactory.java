package groovy.jface.factory;

import groovy.jface.impl.PreferencePageFieldEditorImpl;
import groovy.lang.MissingPropertyException;
import groovy.swt.InvalidParentException;
import groovy.swt.factory.AbstractSwtFactory;
import groovy.swt.factory.SwtFactory;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class PreferencesFieldEditorFactory extends AbstractSwtFactory implements
        SwtFactory {

    private Class beanClass;

    public PreferencesFieldEditorFactory(Class beanClass) {
        this.beanClass = beanClass;
    }

    public Object newInstance(Map properties, Object parent)
            throws GroovyException {

        if (beanClass == null) { throw new GroovyException(
                "No Class available to create the FieldEditor"); }

        // check location
        if (!(parent instanceof PreferencePage)) { throw new InvalidParentException(
                "preferencePage"); }

        String name = (String) properties.get("propertyName");
        if (name == null) { throw new MissingPropertyException("propertyName",
                FieldEditor.class); }

        String labelText = (String) properties.get("title");
        if (labelText == null) { throw new MissingPropertyException("title",
                FieldEditor.class); }

        PreferencePageFieldEditorImpl preferencePageImpl = (PreferencePageFieldEditorImpl) parent;
        preferencePageImpl.addFieldCreator(beanClass, name, labelText);

        return preferencePageImpl;
    }

}
