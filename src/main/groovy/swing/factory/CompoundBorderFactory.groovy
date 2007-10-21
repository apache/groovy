/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Oct 19, 2007
 * Time: 1:04:20 PM
 * To change this template use File | Settings | File Templates.
 */
package groovy.swing.factory

import javax.swing.border.Border
import javax.swing.border.CompoundBorder

class CompoundBorderFactory extends SwingBorderFactory {

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context.applyBorderToParent = attributes.remove('parent')

        Border border  = null
        if (value instanceof List) {
            switch (value.size()) {
                case 0:
                    throw new RuntimeException("$name does not accept an empty array as an value argument")
                case 1:
                    border = value[0]
                    break
                case 2:
                    border = new CompoundBorder(value[0], value[1])
                    break
                case 3:
                default:
                    border = new CompoundBorder(value[0], value[1])
                    border = value[2..-1].inject(border) {that, it -> new CompoundBorder(that, it) }
                    break;
            }
        }

        if (!border && attributes) {
            if (value) {
                throw new RuntimeException("$name only accepts an array of borders as a value argument")
            }
            def inner = attributes.remove("inner")
            def outer = attributes.remove("outer")
            if (inner instanceof Border && outer instanceof Border) {
                border = new CompoundBorder(outer, inner)
            }
        }

        if (!border) {
            throw new RuntimeException("$name only accepts an array of javax.swing.border.Border or an inner: and outer: attribute")
        }

        return border
    }
}