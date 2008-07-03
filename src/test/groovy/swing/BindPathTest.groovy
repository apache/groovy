package groovy.swing

import groovy.beans.Bindable

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Jun 9, 2008
 * Time: 8:19:38 PM
 * To change this template use File | Settings | File Templates.
 */
class BindPathTest extends GroovySwingTestCase {

    public void testClosureBindingProperties() {
        if (isHeadless()) return
        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            beanA = new BeanPathTestA(foo:'x', bar:'y', bif:'z', qux:'w')
            beanC = new BeanPathTestA(foo:beanA, bar:'a')
            beanB = bean(new BeanPathTestB(), foo:bind {beanA.foo}, baz:bind {beanA.bar * 2}, bif: bind {beanC.foo.bar})
        }
        def beanA = swing.beanA
        def beanB = swing.beanB
        def beanC = swing.beanC
        assert beanB.foo == 'x'
        assert beanB.baz == 'yy'

        // bif is chained two levels down
        assert beanB.bif == 'y'

        beanA.bar = 3
        assert beanB.baz == 6

        // assert change at deepest level
        assert beanB.bif == 3
        //assert change at first level
        beanC.foo = beanC
        assert beanB.bif == 'a'

        // assert change at deepest level again
        beanC.bar = 'c'
        assert beanB.bif == 'c'
    }

    public void testClosureBindingLocalVariables() {
        if (isHeadless()) return
        SwingBuilder swing = new SwingBuilder()

        def beanA = null
        def beanB = null
        def beanC = null
        swing.actions() {
            beanA = new BeanPathTestA(foo:'x', bar:'y', bif:'z', qux:'w')
            beanC = new BeanPathTestA(foo:beanA, bar:'a')
            beanB = bean(new BeanPathTestB(), foo:bind {beanA.foo}, baz:bind {beanA.bar * 2}, bif: bind {beanC.foo.bar})
        }
        assert beanB.foo == 'x'
        assert beanB.baz == 'yy'

        // bif is chained two levels down
        assert beanB.bif == 'y'

        beanA.bar = 3
        assert beanB.baz == 6

        // assert change at deepest level
        assert beanB.bif == 3
        //assert change at first level
        beanC.foo = beanC
        assert beanB.bif == 'a'

        // assert change at deepest level again
        beanC.bar = 'c'
        assert beanB.bif == 'c'
    }

    public void testSyntheticBindings() {
        if (isHeadless()) return
        SwingBuilder swing = new SwingBuilder()

        swing.panel() {
            tweetBox = textField()
            tweetButton = button(enabled:bind {tweetBox.text.length() in  1..140})
            tweetLimit = progressBar(value:bind {Math.min(140, tweetBox.text.length())},
                    string: bind { int count = tweetBox.text.length();
                        ((count <= 140)
                            ? "+${140 - count}"
                            : "-${count - 140}")
                    })
        }
        assert !swing.tweetButton.enabled
        assert swing.tweetLimit.string == "+140"

        swing.tweetBox.text = 'xxx'
        assert swing.tweetButton.enabled
        assert swing.tweetLimit.string == "+137"

        swing.tweetBox.text = 'x'*141
        assert !swing.tweetButton.enabled
        assert swing.tweetLimit.string == "-1"


    }
}

public class BeanPathTestA {
    @Bindable Object foo
    @Bindable Object bar
    Object bif
    @Bindable Object qux
}

public class BeanPathTestB {
    @Bindable Object foo
    @Bindable Object baz
    @Bindable Object  bif
    Object qux
}
