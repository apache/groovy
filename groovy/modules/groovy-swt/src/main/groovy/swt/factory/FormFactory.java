/*
 * Created on Feb 27, 2004
 *  
 */
package groovy.swt.factory;

import groovy.swt.InvalidParentException;
import groovy.swt.SwtUtils;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class FormFactory extends AbstractSwtFactory implements SwtFactory {

    /** static is evil, too many toolkits is evil */
    protected static FormToolkit toolkit;

    /** type of */
    private String type;

    /**
     * @param string
     */
    public FormFactory(String type) {
        this.type = type;
    }

    /*
     * @see groovy.swt.factory.AbstractSwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException {
        Composite parentComposite = (Composite) SwtUtils
                .getParentWidget(parent);
        if (toolkit == null) {
            toolkit = new FormToolkit(parentComposite.getDisplay());
            toolkit.setBorderStyle(SWT.BORDER);
        }
        String styleProperty = (String) properties.remove("style");
        String text = (String) properties.remove("text");
        int style = SWT.NULL;
        if (styleProperty != null) {
            if (type.equals("formSection")) {
                style = SwtUtils.parseStyle(Section.class, styleProperty);
            } else {
                style = SwtUtils.parseStyle(SWT.class, styleProperty);
            }
        }
        if (parentComposite != null) {
            Object formWidget = getFormWidget(parentComposite, properties,
                    style, text);
            setBeanProperties(formWidget, properties);
            return formWidget;
        } else {
            throw new InvalidParentException("composite instance");
        }
    }

    /**
     * @param parentComposite
     * @param style
     * @param text
     * @return
     */
    private Object getFormWidget(final Composite parentComposite,
            Map properties, int style, String text) throws GroovyException {
        if ("form".equals(type)) {
            Form form = toolkit.createForm(parentComposite);
            form.setText(text);
            return form;
        }
        if ("scrolledForm".equals(type)) {
            ScrolledForm scrolledForm = toolkit
                    .createScrolledForm(parentComposite);
            scrolledForm.setText(text);
            return scrolledForm;
        }
        if ("formButton".equals(type)) { return toolkit.createButton(
                parentComposite, text, style); }
        if ("formColors".equals(type)) { return toolkit.getColors(); }
        if ("formComposite".equals(type)) { return toolkit.createComposite(
                parentComposite, style); }
        if ("formCompositeSeparator".equals(type)) { return toolkit
                .createCompositeSeparator(parentComposite); }
        if ("formExpandableComposite".equals(type)) { return toolkit
                .createExpandableComposite(parentComposite, style); }
        if ("formText".equals(type)) { return toolkit.createText(
                parentComposite, text, style); }
        if ("formHyperlink".equals(type)) { return toolkit.createHyperlink(
                parentComposite, text, style); }
        if ("formImageHyperlink".equals(type)) { return toolkit
                .createImageHyperlink(parentComposite, style); }
        if ("formLabel".equals(type)) { return toolkit.createLabel(
                parentComposite, text, style); }
        if ("formPageBook".equals(type)) { return toolkit.createPageBook(
                parentComposite, style); }
        if ("formPageBookPage".equals(type)) {
            if (parentComposite instanceof ScrolledPageBook) {
                ScrolledPageBook pageBook = (ScrolledPageBook) parentComposite;
                String key = (String) properties.remove("key");
                if (key != null) {
                    Composite page = pageBook.createPage(key);
                    pageBook.registerPage(key, page);
                    return page;
                }
            } else {
                throw new InvalidParentException("formPageBook");
            }
        }
        if ("formSection".equals(type)) {
            Section section = toolkit.createSection(parentComposite, style);
            if (text != null) {
                section.setText(text);
            }
            section.setSeparatorControl(toolkit
                    .createCompositeSeparator(section));
            String description = (String) properties.remove("description");
            if (description != null) {
                section.setDescription(description);
            }
            Composite client = toolkit.createComposite(section);
            client.setLayout(new GridLayout());
            section.setClient(client);
            return section;
        }
        if ("formSeparator".equals(type)) { return toolkit.createSeparator(
                parentComposite, style); }
        if ("formTable".equals(type)) { return toolkit.createTable(
                parentComposite, style); }
        if ("formToolkit".equals(type)) { return toolkit; }
        if ("formFormattedText".equals(type)) {
            boolean parseTags = false;
            boolean expandURLs = false;
            if (properties.get("parseTags") != null) {
                parseTags = ((Boolean) properties.remove("parseTags"))
                        .booleanValue();
            }
            if (properties.get("expandURLs") != null) {
                expandURLs = ((Boolean) properties.remove("expandURLs"))
                        .booleanValue();
            }
            FormText formText = toolkit.createFormText(parentComposite, true);
            formText.setText(text, parseTags, expandURLs);
            return formText;
        }
        if ("formTree".equals(type)) { return toolkit.createTree(
                parentComposite, style); }
        return null;
    }
}