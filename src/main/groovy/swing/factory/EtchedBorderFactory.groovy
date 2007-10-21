/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Oct 19, 2007
 * Time: 1:03:08 PM
 * To change this template use File | Settings | File Templates.
 */
package groovy.swing.factory

import java.awt.Color
import javax.swing.BorderFactory

class EtchedBorderFactory extends SwingBorderFactory {

    final int type;

    public EtchedBorderFactory(int newType) {
        type = newType;
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context.applyBorderToParent = attributes.remove('parent')
        // no if-else-if chain so that we can have one attribute failure block
        if (attributes.containsKey("highlight")) {
            Color highlight = attributes.remove("highlight")
            Color shadow = attributes.remove("shadow")
            if (highlight && shadow && !attributes) {
                return BorderFactory.createEtchedBorder(type, highlight, shadow);
            }
        }
        if (attributes) {
            throw new RuntimeException("$name only accepts no attributes, or highlight: and shadow: attributes")
        }
        return BorderFactory.createEtchedBorder(type);
    }

}