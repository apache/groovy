/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Oct 19, 2007
 * Time: 1:18:19 PM
 * To change this template use File | Settings | File Templates.
 */
package groovy.swing.factory

import javax.swing.RootPaneContainer
import javax.swing.JComponent

abstract class SwingBorderFactory extends AbstractFactory {

    public boolean isLeaf() {
        // no children
        return true;
    }

    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        // never do bean apply
        return false;
    }

    public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
        if (builder.context.applyBorderToParent) {
            if (parent instanceof JComponent) {
                parent.setBorder(child);
            } else if (parent instanceof RootPaneContainer) {
                setParent(builder, parent.contentPane, child)
            } else {
                throw new RuntimeException("Border cannot be applied to parent, it is neither a JComponent or a RootPaneContainer")
            }
        }
    }


}