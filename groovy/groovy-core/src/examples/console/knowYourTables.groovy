/** 
 * Simple mathematics quiz
 * @author: Jeremy Rayner
 * based on algorithms from INPUT/Marshall Cavendish/1984
 */
while (true) {
    try {
        n = (int)(Math.random() * 12) + 1
        println "What is ${n} times 9?"
        a = System.in.readLine().toInteger()
        if (a == n * 9) println "Correct"
              
    } catch (Exception e) {
        println "The computer didn't understand your input"
    }
}
