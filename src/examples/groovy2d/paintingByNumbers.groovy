/** 
 *  simple patchwork graphics demo
 * @author: Jeremy Rayner, changes by Dierk Koenig
 */

width = 500; height = 400; blockSize = 10
g = createGraphics()

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
    row    = rnd(height / blockSize)
    column = rnd(width  / blockSize)
    colour = new java.awt.Color(rnd(255),rnd(255),rnd(255))
    g.setColor(colour)
    g.fillRect(column * blockSize, row * blockSize, blockSize, blockSize)
}


// create a new frame and clear screen
def createGraphics() {
    frame = new groovy.swing.SwingBuilder().
              frame(title:'Painting by numbers', 
                    location:[20,20], 
                    size:[width,height],
                    defaultCloseOperation:javax.swing.WindowConstants.EXIT_ON_CLOSE) {
    }
    frame.show()
              
    // obtain graphics context
    g = frame.getGraphics()
              
    // clear screen
    g.setColor(java.awt.Color.BLACK)
    g.fillRect(0,0,width,height)

    return g
}
