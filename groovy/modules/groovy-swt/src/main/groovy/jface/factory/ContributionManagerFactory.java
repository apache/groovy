package groovy.jface.factory;

import groovy.swt.factory.SwtFactory;
import groovy.swt.factory.WidgetFactory;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ContributionManagerFactory extends WidgetFactory implements
        SwtFactory {

    public ContributionManagerFactory(Class beanClass) {
        super(beanClass);
    }

    /*
     * @see groovy.swt.impl.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
            throws GroovyException {

        Object bean = createWidget(parent);

        setBeanProperties(bean, properties);

        if (parent instanceof IContributionManager) {
            IContributionManager contributionManager = (IContributionManager) parent;
            
            if (bean instanceof IAction) {
                contributionManager.add((IAction)bean);
            }
            
            if (bean instanceof IContributionItem) {
                contributionManager.add((IContributionItem)bean);
            }
        }
        return bean;
    }
}