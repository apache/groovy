/** 
 *  simple patchwork graphics demo
 * @author: Jeremy Rayner, changes by Dierk Koenig
 */

def width = 500; def height = 400; def blockSize = 10
def g = createGraphics()

// main loop
while (true) {
    drawBlock()
}

// --------------------------------------------------

// random integer
def rnd(upperBound){
    (int)(Math.random() * upperBound)
}

// draw a random coloured square within bounds
def drawBlock() {
    def row    = rnd(height / blockSize)
    def column = rnd(width  / blockSize)
    def colour = new java.awt.Color(rnd(255),rnd(255),rnd(255))
    g.setColor(colour)
    g.fillRect(column * blockSize, row * blockSize, blockSize, blockSize)
}


// create a new frame and clear screen
def createGraphics() {
    def frame = new groovy.swing.SwingBuilder().
              frame(title:'Painting by numbers', 
                    location:[20,20], 
                    size:[width,height],
                    defaultCloseOperation:javax.swing.WindowConstants.EXIT_ON_CLOSE) {
    }
    frame.show()
              
    // obtain graphics context
    def gfx = frame.getGraphics()
              
    // clear screen
    gfx.setColor(java.awt.Color.BLACK)
    gfx.fillRect(0,0,width,height)

    return gfx
}
