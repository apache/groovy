package groovy.swing.factory

import org.codehaus.groovy.binding.AggregateBinding

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Nov 2, 2008
 * Time: 8:48:19 AM
 * To change this template use File | Settings | File Templates.
 */
class BindGroupFactory extends AbstractFactory {

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        AggregateBinding bindGroup = new AggregateBinding();
        def bind = attributes.remove('bind')
        if ((bind == null) || bind) {
            bindGroup.bind()
        }
        return bindGroup
    }

}