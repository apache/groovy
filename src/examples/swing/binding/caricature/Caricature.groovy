package swing.binding.caricature

import groovy.swing.SwingBuilder
import java.awt.Color
import java.awt.BorderLayout
import javax.swing.border.TitledBorder

swing = new SwingBuilder()

frame = swing.frame(defaultCloseOperation:javax.swing.JFrame.DISPOSE_ON_CLOSE) {
    panel(/*border: new DropShadowBorder(lineColor:Color.BLACK, lineWidth:0, 
            shadowSize:5, shadowOpacity:0.5f, cornerSize:12, showLeftShadow:true, 
            showBottomShadow:true, showRightShadow:true),*/
          constraints:BorderLayout.CENTER) 
    {
        widget(new JCaricature(
                background:Color.WHITE,
                opaque:true),
            id:'caricature')
    }
    hbox(constraints:BorderLayout.SOUTH) {
        panel(border:new TitledBorder("Style")) {//, font:new Font
            gridLayout(new java.awt.GridLayout(5, 2))
            label("Eyes") // font stuff
            slider(id:'eyeSlider', maximum:4, value:2)
            label("Face") // font stuff
            slider(id:'faceSlider', maximum:4, value:2)
            label("Mouth") // font stuff
            slider(id:'mouthSlider', maximum:4, value:2)
            label("Hair") // font stuff
            slider(id:'hairSlider', maximum:4, value:2)
            label("Nose") // font stuff
            slider(id:'noseSlider', maximum:4, value:2)
        }
        panel(border:new TitledBorder("Effects")) { //, font stuff
            gridLayout(new java.awt.GridLayout(2, 5))
            label("Rotation") // font stuff
            slider(id:'rotationSlider', maximum:360, value:0)  
            label("Scale") // font stuff
            slider(id:'scaleSlider', maximum: 150, minimum:50, value:100)  
        }
    }
    bean(caricature,
        eyeStyle: bind(source:eyeSlider, sourceProperty:'value'),
        faceStyle: bind(source:faceSlider, sourceProperty:'value'),
        mouthStyle: bind(source:mouthSlider, sourceProperty:'value'),
        hairStyle: bind(source:hairSlider, sourceProperty:'value'),
        noseStyle: bind(source:noseSlider, sourceProperty:'value'),
        rotation: bind(source:rotationSlider, sourceProperty:'value'),
        scale: bind(source:scaleSlider, sourceProperty:'value', converter: {it / 100f})
    )
}

//bb = new BeansBindingBuilder()
//class ScaleConverter extends javax.beans.binding.BindingConverter { // grrr.... this should be an interface!
//    Object sourceToTarget(Object it) {
//        (int)(it * 100.0f)
//    }
//
//    Object targetToSource(Object it) {
//        (float)(it / 100.0f)
//    }
//}
//
//context = bb.context() {
//    binding(source: caricature, value: '${eyeStyle}',  target: swing.eyeSlider,      property: 'value')
//    binding(source: caricature, value: '${faceStyle}',  target: swing.faceSlider,     property: 'value')
//    binding(source: caricature, value: '${mouthStyle}', target: swing.mouthSlider,    property: 'value')
//    binding(source: caricature, value: '${hairStyle}',  target: swing.hairSlider,     property: 'value')
//    binding(source: caricature, value: '${noseStyle}',  target: swing.noseSlider,     property: 'value')
//    binding(source: caricature, value: '${rotation}',   target: swing.rotationSlider, property: 'value')
//    binding(source: caricature, value: '${scale}',      target: swing.scaleSlider,    property: 'value') {
//        converter(new ScaleConverter())
//    }
//}
//context.bind()
frame.pack()
frame.show()