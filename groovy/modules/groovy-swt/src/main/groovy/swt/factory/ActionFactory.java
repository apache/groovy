package groovy.swt.factory;

import groovy.jface.factory.ActionImpl;

import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ActionFactory extends AbstractSwtFactory implements SwtFactory {


    /*
     * @see groovy.swt.impl.SwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent)
    throws GroovyException {
        Action action = new ActionImpl();
        setBeanProperties(action, properties);
        if (parent instanceof IContributionManager){
            IContributionManager contributionManager = (IContributionManager) parent;
            contributionManager.add(action);
        }
        return action;
    }
}
