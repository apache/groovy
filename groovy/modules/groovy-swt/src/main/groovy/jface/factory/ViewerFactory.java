/*
 * Created on Feb 28, 2004
 *
 */
package groovy.jface.factory;

import groovy.swt.SwtUtils;
import groovy.swt.factory.SwtFactory;
import groovy.swt.factory.WidgetFactory;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ViewerFactory extends WidgetFactory implements SwtFactory {

    /**
     * @param beanClass
     * @param style
     */
    public ViewerFactory(Class beanClass, int style) {
        super(beanClass, style);
    }

    /**
     * @param class1
     */
    public ViewerFactory(Class beanClass) {
        super(beanClass);
    }

    /*
     * @see groovy.swt.factory.AbstractSwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent) throws GroovyException {
        Object bean;

        String styleProperty = (String) properties.remove("style");
        if (styleProperty != null) {
            style = SwtUtils.parseStyle(SWT.class, styleProperty);
        }

        if (beanClass.equals(TableViewer.class) && (parent instanceof Table)) {
            bean = new TableViewer((Table) parent, style);

        } else if (beanClass.equals(TableTreeViewer.class) && (parent instanceof TableTree)) {
            bean = new TableTreeViewer((TableTree) parent, style);

        } else if (beanClass.equals(TreeViewer.class) && (parent instanceof Tree)) {
            bean = new TreeViewer((Tree) parent, style);

        } else if (beanClass.equals(CheckboxTreeViewer.class) && (parent instanceof Tree)) {
            bean = new CheckboxTreeViewer((Tree) parent, style);

        } else {
            Object parentWidget = SwtUtils.getParentWidget(parent, properties);
            bean = createWidget(parentWidget);
        }

        if (bean != null) {
            setBeanProperties(bean, properties);
        }

        setControl(bean, parent);

        return bean;
    }
}