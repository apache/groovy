/** 
 *  simple patchwork graphics demo
 * @author: Jeremy Rayner
 */

width = 500; height = 400; blockSize = 10
g = createGraphics()

// create convienence closure for random numbers
rnd = {(int)(Math.random() * it)}

// main loop
while (true) {
    for (row in 0..(int)(height / blockSize)) {
        drawBlock(row,rnd)
    }
}

// --------------------------------------------------

// draw a random coloured square in the specified row
def drawBlock(row,rnd) {
    column = rnd(width / blockSize)
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
