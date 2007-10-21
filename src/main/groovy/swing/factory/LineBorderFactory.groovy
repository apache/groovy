/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Oct 19, 2007
 * Time: 10:43:54 AM
 * To change this template use File | Settings | File Templates.
 */
package groovy.swing.factory

import javax.swing.border.LineBorder

/**
 * accepts attributes:<br />
 * color: java.awt.Color <br/>
 * color: java.awt.Color, thickness: int <br/>
 * color: java.awt.Color, thickness: int, roundedBorders: boolean <br/>
 *
 */
class LineBorderFactory extends SwingBorderFactory {

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context.applyBorderToParent = attributes.remove('parent')
        
        def color = attributes.remove("color")
        if (color == null) {
            throw new RuntimeException("color: is a required attribute for $name")
        }
        def thickness = attributes.remove("thickness")
        if (thickness == null) {
            thickness = 1
        }
        def roundedCorners = attributes.remove("roundedCorners")
        if (roundedCorners == null) {
            roundedCorners = false
        }
        if (attributes) {
            throw new RuntimeException("$name does not know how to handle the remaining attibutes: ${attributes.keySet()}")
        }

        return new LineBorder(color, thickness, roundedCorners);
    }

}