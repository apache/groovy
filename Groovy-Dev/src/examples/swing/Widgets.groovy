package swing

import java.awt.Color
import javax.swing.SwingConstants
import javax.swing.WindowConstants
import groovy.swing.SwingBuilder

class Widgets {

    def swing = new SwingBuilder()
    def unownedDialog
    def ownedDialog
    

    static void main(args) {
        def demo = new Widgets()
        demo.run()
    }

    def showUnownedDialog(event) {
        unownedDialog.show();
    }
    
    def showOwnedDialog(event) {
        ownedDialog.show();
    }

    void run() {
        unownedDialog = swing.dialog(
            title:'unrooted dialog',
            location: [200, 200],
            pack:true,
            defaultCloseOperation:WindowConstants.DISPOSE_ON_CLOSE
            ) {
                label("I am unowned, but not unwanted");
            }

        def frame = swing.frame(
            title:'FrameTitle',
            location:[100,100],
            size:[800,400],
            defaultCloseOperation:WindowConstants.EXIT_ON_CLOSE) {

            menuBar {
                menu(text:'File') {
                    menuItem() {
                        action(name:'New', closure:{ println("clicked on the new menu item!") })
                    }
                    menuItem() {
                        action(name:'Open', closure:{ println("clicked on the open menu item!") })
                    }
                    separator()
                    menuItem() {
                        action(name:'Save', enabled:false, closure:{ println("clicked on the Save menu item!") })
                    }
                }
                menu(text:'Dialogs') {
                    menuItem() {
                        action(name:'Owned Dialog', closure: this.&showOwnedDialog)
                    }
                    menuItem() {
                        action(name:'Unowned Dialog', closure: this.&showUnownedDialog)
                    }
                    def deeplyOwnedDialog = swing.dialog(
                        title:'rooted dialog #2',
                        location: [200, 200],
                        pack:true,
                        defaultCloseOperation:WindowConstants.DISPOSE_ON_CLOSE
                        ) {
                        label("ownership is deep");
                    }
                    menuItem() {
                        action(name:'Deeply Owned Dialog', closure: {deeplyOwnedDialog.show()} )
                    }
                }
            }

            tabbedPane() {

                //colorChooser(
                //    name:"Color Chooser",
                //    color: 0xfeed42)

                panel(name:"Formatted Text Fields") {
                    gridLayout(columns: 2, rows: 0)
                    label("Simple Constructor:")
                    formattedTextField()
                    label("Date Value")
                    formattedTextField(value: new java.util.Date())
                    label("Integer Value")
                    formattedTextField(value: new java.lang.Integer(42))
                    label("Date Format")
                    formattedTextField(format: java.text.DateFormat.getDateInstance())
                    label("Currency Format ")
                    formattedTextField(format: new java.text.DecimalFormat("¤###.00;(¤###.00)"))
                }

                panel(name:"Sliders") {
                    flowLayout()
                    slider(minimum:-100, 
                        maximum:100, 
                        majorTickSpacing: 50,
                        orientation: SwingConstants.VERTICAL, 
                        paintLabels:true)
                    slider(minimum:-100, 
                        maximum:100, 
                        orientation: SwingConstants.VERTICAL, 
                        paintLabels:true,
                        paintTicks:true,
                        majorTickSpacing: 50,
                        minorTickSpacing: 10,
                        snapToTicks:true,
                        paintTrack:true)
                }

                panel(name:"Spinners") {
                    gridBagLayout()
                    label(
                        text:"Tempuature in London:",
                        insets:[12, 12, 2, 2],
                        anchor: EAST,
                        gridx: 0)
                    spinner(
                        model:spinnerNumberModel(minimum:-10, 
                            maximum: 40, 
                            value:20,
                            stepSize:5),
                        insets:[12, 3, 2, 12],
                        anchor: WEST,
                        gridx: 1,
                        fill: HORIZONTAL)
                    label(
                        text:"Baseball Leagues:",
                        insets:[3, 12, 2, 2],
                        anchor: EAST,
                        gridx: 0)
                    spinner(
                        model:spinnerListModel(
                            list: ["Major League", "AAA", "AA", "A", "Rookie", "Semi-Pro", "Rec A", "Rec B"],
                            value: "AA"),
                        insets:[3, 3, 2, 12],
                        anchor: WEST,
                        gridx: 1,
                        fill: HORIZONTAL)
                    label(
                        text:"Today's Date:",
                        insets:[3, 12, 2, 2],
                        anchor: EAST,
                        gridx: 0)
                    spinner(
                        model:spinnerDateModel(calendarField: Calendar.HOUR_OF_DAY),
                        insets:[3, 3, 2, 12],
                        anchor: WEST,
                        gridx: 1,
                        fill: HORIZONTAL)
                }

                panel(name:"Border Layout") {
                    borderLayout()
                    label(text:"Border Layout", 
                          constraints:NORTH,
                          horizontalAlignment:SwingConstants.CENTER)
                    label(text:"South", 
                          constraints:SOUTH,
                          background:Color.YELLOW,
                          opaque:true,
                          horizontalAlignment:SwingConstants.CENTER,
                          toolTipText:"Tooltip on south")
                    label(text:"West", 
                          constraints:WEST,
                          background:Color.ORANGE,
                          opaque:true,
                          horizontalAlignment:SwingConstants.CENTER,
                          toolTipText:"Tooltip on west")
                    label(text:"East", 
                          constraints:EAST,
                          background:Color.GREEN,
                          opaque:true,
                          horizontalAlignment:SwingConstants.CENTER,
                          toolTipText:"Tooltip on east")
                    label(text:"Center", 
                          constraints:CENTER,
                          background:Color.WHITE,
                          opaque:true,
                          horizontalAlignment:SwingConstants.CENTER,
                          toolTipText:"<html>This is not the tooltip you are looking for.<br><i>*waves hand*</i>")
                }
            }

            ownedDialog = swing.dialog(
                title:'rooted dialog',
                location: [200, 200],
                pack:true,
                defaultCloseOperation:WindowConstants.DISPOSE_ON_CLOSE
                ) {
                label("j00 h4v3 b33n 0wn3xed");
            }
        }        
        frame.show()
    }
    
}
