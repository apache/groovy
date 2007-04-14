/** 
 * Simple patchwork graphics demo
 * @author: Jeremy Rayner, changes by Dierk Koenig
 */
import javax.swing.WindowConstants as WC
import groovy.swing.SwingBuilder

def width = 500, height = 400, blockSize = 10
def g = createGraphics(width, height)

// main loop
while (true) {
    drawBlock(width, height, blockSize, g)
}

// random integer
def rnd(upperBound){
    (int)(Math.random() * upperBound)
}

// draw a random coloured square within bounds
def drawBlock(w, h, b, g) {
    def row    = rnd(h / b)
    def column = rnd(w / b)
    def colour = new java.awt.Color(rnd(255),rnd(255),rnd(255))
    g.color = colour
    g.fillRect(column * b, row * b, b, b)
}

// create a new frame and clear screen
def createGraphics(w, h) {
    def frame = new SwingBuilder().frame(
        title:'Painting by numbers',
        location:[20,20],
        size:[w, h],
        defaultCloseOperation:WC.EXIT_ON_CLOSE
    )
    frame.show()
              
    // obtain graphics context
    def gfx = frame.graphics
              
    // clear screen
    gfx.color = java.awt.Color.BLACK
    gfx.fillRect(0, 0, w, h)

    return gfx
}
