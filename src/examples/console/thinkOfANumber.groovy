/** 
 * Simple game
 * @author: Jeremy Rayner
 * based on algorithms from INPUT/Marshall Cavendish/1984
 */
while (true) {
    try {
        int x = Math.random() * 6
        print "The computer has chosen a number between 0 and 5. Can you guess it?"
              
        def line = System.in.readLine()
        int g = line.toInteger()
        if (g == x) {
           println "Well done" 
        } else {
           println "Tough luck - you're wrong"
        }
     } catch (NumberFormatException e) {
         println "The computer didn't understand '$line'"
     }
}
