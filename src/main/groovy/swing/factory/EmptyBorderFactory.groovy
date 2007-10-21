/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Oct 19, 2007
 * Time: 1:04:07 PM
 * To change this template use File | Settings | File Templates.
 */
package groovy.swing.factory

import javax.swing.BorderFactory

/**
 * accepts values in leiu of attributes:
 *  int - all of top, left, bottom, right
 *  [int, int, int, int] - top, left, bottom, right
 * accepts attributes when no value present:
 *  top: int, left: int, bottom: int, right: int
 *
 */
class EmptyBorderFactory extends SwingBorderFactory {

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context.applyBorderToParent = attributes.remove('parent')
        
        if (!attributes) {
            if (value instanceof Integer) {
                return BorderFactory.createEmptyBorder(value, value, value, value)
            } else if (value instanceof List && value.size() == 4) {
                // we need GDK methods on Collection for boolean .any{} and boolean .all{} :(
                boolean ints = true
                value.each {ints = ints & it instanceof Integer}
                if (ints) {
                    return BorderFactory.createEmptyBorder(*value);
                }
            }
            throw new RuntimeException("$name only accepts a single integer or an array of four integers as a value argument");
        }
        if (value == null) {
            int top = attributes.remove("top")
            int left = attributes.remove("left")
            int bottom = attributes.remove("bottom")
            int right = attributes.remove("right")
            if ((top == null) || (top == null) || (top == null) || (top == null) || attributes) {
                throw new RuntimeException("When $name is called it must be call with top:, left:, bottom:, right:, and no other attributes")
            }
            return BorderFactory.createEmptyBorder(top, left, bottom, right);
        }
        throw new RuntimeException("$name cannot be called with both an argulent value and attributes")
    }

}