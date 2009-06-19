/*
 * $Id: SwingBuilderBindingsTest.groovy 14196 2008-11-27 16:32:25Z shemnon $
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package groovy.swing.vm5

import groovy.beans.Bindable
import groovy.swing.SwingBuilder
import java.text.SimpleDateFormat

public class SwingBuilderBindingsTest extends GroovySwingTestCase {

  def mutualPropertyShortWorkout = { source, sourceProperty, sourceMutators,
      target, targetProperty, targetMutators, binding ->

    sourceMutators[0]()
    targetMutators[0]()

    // test forward binding
    assert source[sourceProperty] == target[targetProperty]
    sourceMutators[1]()
    assert source[sourceProperty] == target[targetProperty]
    sourceMutators[0]()
    assert source[sourceProperty] == target[targetProperty]

    // test reverse binding
    targetMutators[1]()
    assert source[sourceProperty] == target[targetProperty]
    targetMutators[0]()
    assert source[sourceProperty] == target[targetProperty]
  }

  def mutualPropertyWorkout = { source, sourceProperty, sourceMutators,
    target, targetProperty, targetMutators, binding ->

    mutualPropertyShortWorkout(source, sourceProperty, sourceMutators,
      target, targetProperty, targetMutators, binding)

    // test rebound
    binding.rebind()
    targetMutators[1]()
    assert source[sourceProperty] == target[targetProperty]
    sourceMutators[0]()
    assert source[sourceProperty] == target[targetProperty]

    // test unbound not updating
    binding.unbind()
    sourceMutators[1]()
    assert source[sourceProperty] != target[targetProperty]
    targetMutators[1]()
    assert source[sourceProperty] == target[targetProperty]
    sourceMutators[0]()
    assert source[sourceProperty] != target[targetProperty]

    // test manual forward update
    sourceMutators[0]()
    assert source[sourceProperty] != target[targetProperty]
    binding.update()
    assert source[sourceProperty] == target[targetProperty]

    // test manual reverse update
    sourceMutators[1]()
    assert source[sourceProperty] != target[targetProperty]
    binding.reverseUpdate()
    assert source[sourceProperty] == target[targetProperty]
  }

  public void testMutualPropertyBinding() {
    testInEDT {
      ['full', 'source', 'target'].each { mode -> // contextual bind mode
        ['prop', 'synth'].each { target -> // target binding
          ['prop', 'synth'].each { source -> // source binding
            println "Trying $mode binding on $source source and $target target"

            SwingBuilder swing = new SwingBuilder()

            def sProp, tProp
            swing.actions() {
              switch (source) {
                case 'prop':
                  sProp = 'enabled'
                  st = new BindableBean(text:'Baz')
                  break
                case 'synth':
                  sProp = 'selected'
                  st = textField(text:'Baz')
                  break
                default: fail()
              }
              switch (target) {
                case 'prop':
                  tProp = 'enabled'
                  tt = new BindableBean(text:'Baz')
                  break
                case 'synth':
                  tProp = 'selected'
                  tt = textField(text:'Baz')
                  break
                default: fail()
              }

              switch (mode) {
                case 'full':
                  checkBox(id:'cb1')
                  checkBox(id:'cb2')
                  bind(source: cb1, sourceProperty: sProp,
                       target: cb2, targetProperty: tProp,
                       id:'binding', mutual:'true')

                  bind('text', source: st, target: tt,
                       id:'textBinding', mutual:'true')

                  break
                case 'source':
                  checkBox(id:'cb2')
                  checkBox(id:'cb1', "$sProp": bind(
                       target: cb2, targetProperty:tProp,
                       id:'binding', mutual:'true'))

                  bean(st, text:bind(
                       target: tt, 'text',
                       id:'textBinding', mutual:'true'))
                  break
                case 'target':
                  checkBox(id:'cb1')
                  checkBox(id:'cb2', "$tProp": bind(
                       source: cb1, sourceProperty:sProp,
                       id:'binding', mutual:'true'))

                  bean(tt, text:bind(
                       source: st, 'text',
                       id:'textBinding', mutual:'true'))
                break
                default: fail()
              }
            }
            mutualPropertyWorkout(swing.cb1, sProp, [{swing.cb1[sProp] = true}, {swing.cb1[sProp] = false}],
              swing.cb2, tProp, [{swing.cb2[tProp] = true}, {swing.cb2[tProp] = false}],
              swing.binding)

            mutualPropertyWorkout(swing.st, 'text', [{swing.st.text = "Foo"}, {swing.st.text = "Bar"}],
              swing.tt, 'text', [{swing.tt.text = "Foo"}, {swing.tt.text = "Bar"}],
              swing.textBinding)
          }
          if (mode != 'source') {
            println "Trying $mode binding on event source and $target target"

            SwingBuilder swing = new SwingBuilder()

            def tProp
            swing.actions() {
              st = button(actionCommand:'Baz')

              switch (target) {
                case 'prop':
                  tProp = 'enabled'
                  tt = new BindableBean(text:'Baz')
                  break
                case 'synth':
                  tProp = 'selected'
                  tt = textField(text:'Baz')
                  break
                default: fail()
              }
              switch (mode) {
                case 'full':
                  checkBox(id:'cb1')
                  checkBox(id:'cb2')
                  bind(source: cb1, sourceEvent: 'actionPerformed', sourceProperty: 'borderPaintedFlat',
                       target: cb2, targetProperty: tProp,
                       id:'binding', mutual:'true')

                  bind('text', source: st, sourceEvent: 'actionPerformed', sourceProperty:'actionCommand', target: tt,
                       id:'textBinding', mutual:'true')

                  break
                case 'target':
                  checkBox(id:'cb1')
                  checkBox(id:'cb2', "$tProp": bind(  source: cb1,
                       sourceEvent: 'actionPerformed', sourceProperty: 'borderPaintedFlat',
                       id:'binding', mutual:'true'))

                  bean(tt, text:bind(
                       source: st, 'actionCommand', sourceEvent: 'actionPerformed',
                       id:'textBinding', mutual:'true'))
                break
                default: fail()
              }
              mutualPropertyShortWorkout(swing.cb1, 'borderPaintedFlat', [{swing.cb1.borderPaintedFlat = true; swing.cb1.doClick()}, {swing.cb1.borderPaintedFlat = false; swing.cb1.doClick()}],
                swing.cb2, tProp, [{swing.cb2[tProp] = true}, {swing.cb2[tProp] = false}],
                swing.binding)

              mutualPropertyShortWorkout(swing.st, 'actionCommand', [{swing.st.actionCommand = "Foo"; swing.st.doClick()}, {swing.st.actionCommand = "Bar"; swing.st.doClick()}],
                swing.tt, 'text', [{swing.tt.text = "Foo"}, {swing.tt.text = "Bar"}],
                swing.textBinding)
            }
          }
        }
      }

      println "finished all permutations successfully"

    }
  }

    public void testConverter() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()
        def model = new BindableBean()

        def toNumber = { v ->
          if( v == null || v == "" ) return null
          try { return new Integer(v) }
          catch( x ) { return null }
        }

        swing.actions {

          frame( title: "Binding test", size: [100,60]) {
            textField( id: "t1", text: bind(target:model,
            targetProperty: "value", converter: {toNumber(it)}) )
          textField( id: "t2", text: bind(target:model,
            'floatValue', value:'1234', converter: { Float.parseFloat(it) * 2 }))
          }
        }
        assert model.floatValue == 2468
        swing.t1.text = '1234'
        assert model.value == 1234
      }
    }


    public void testDateConverters() {
      testInEDT {
        BindableBean model = new BindableBean()
        model.date = new Date()
        def dateConverter = { dateString ->
                new SimpleDateFormat("dd.MM.yyyy").parse(dateString)
        }

        def dateValidator = { dateString ->
                try {
                        def parser = new SimpleDateFormat("dd.MM.yyyy")
                        parser.lenient = false
                        parser.parse(dateString)
                }
                catch (Exception e) {
                        return false
                }
                return true
        }


        SwingBuilder swing = new SwingBuilder()

        swing.actions() {
            tf1 = textField('01.01.1970', id:'birthdayText', columns:20,
              text:bind (
                 validator: dateValidator,
                 converter: dateConverter,
                 target:model, targetProperty:'birthday'
                 ))
            tf2 = textField('01.01.1970', id:'birthdayText', columns:20)
            binding = bind (
                  source: tf2,
                  sourceProperty: 'text',
                  validator: dateValidator,
                  converter: dateConverter,
                  target:model,
                  targetProperty:'birthday'
                  )

        }
      }
    }


    public void testValidator() {
      testInEDT {
        SwingBuilder swing = new SwingBuilder()
        def model = new BindableBean()

        def isInteger = { v ->
          try {
              Float.parseFloat(v)
              return true
          } catch (NumberFormatException ignore) {
              return false
          }
        }

        swing.actions {
          frame( title: "Binding test", size: [100,60]) {
            textField( id: "t1", text: bind(target:model, value:'123',
              targetProperty: "value", validator: isInteger, converter: Integer.&parseInt) )
          }
        }
        assert model.value == 123
        swing.t1.text = '1234'
        assert model.value == 1234
        swing.t1.text = 'Bogus'
        assert model.value == 1234
        swing.t1.text = '12345'
        assert model.value == 12345
      }
    }
}

@Bindable class BindableBean {
    boolean enabled
    Integer value
    float floatValue
    int pvalue
    String text
    Date date
}
