/** 
 * Mortgage Calculator
 * @author: Jeremy Rayner
 * based on algorithms by Jeff Louie, Dr W Carlini and Newton
 */

println "__..::~~'''~~::..__"
println "Mortgage Calculator"
println "~~~~~~~~~~~~~~~~~~~"
println "Please input 3 of the 4 values in your mortgage calculation"
println "This program will then calculate the value you leave blank"
println ""

def variables = [
    "Amount of mortgage" : 0.0, 
    "Annual interest rate (%)" : 0.0, 
    "Loan duration (months)" : 0.0, 
    "Monthly payments" : 0.0
]

for (entry in variables.entrySet()) {
    print("${entry.key}:")
    def userInput = System.in.readLine()
    if ("" == userInput) {
        valueToCalculate = entry.key
    } else {
        entry.value = userInput.toDouble()
    }
}

println "$valueToCalculate = ${calculateValueOf(valueToCalculate)}"





def calculateValueOf(valueToCalculate) {
    def result = 0
    def principal = variables["Amount of mortgage"]
    def interest = variables["Annual interest rate (%)"] / 1200
    def months = variables["Loan duration (months)"]
    def payment = variables["Monthly payments"]

    switch (valueToCalculate) {
    case "Amount of mortgage":
        result = 1 + interest
        result = 1/Math.pow(result,months)
        result = ((1-result)/interest) * payment
        break           
    case "Loan duration (months)":
        result = (1 - (principal * interest / payment))
        result = Math.log(result)
        result = - result / Math.log(1 + interest)  
        break
    case "Monthly payments":
        result = 1 + interest
        result = 1 / Math.pow(result,months)
        result = (principal * interest) / (1 - result)
        break          
    case "Annual interest rate (%)":
        result = payment / principal
        def diff = 100; def accuracy = 0.00001; def maxIterations = 1000
        def index = 0
        while ((diff > accuracy) && (index < maxIterations)) {
            def temp = result
            def numerator = (principal * temp / payment) + Math.pow((1 + temp), -months) - 1
            def denominator= (principal / payment) - months * Math.pow((1 + temp), (-months - 1))
            result = temp - (numerator / denominator)
            diff = result - temp
            diff = Math.abs(diff)
            index++
        }
        result *= 1200
        break           
    }
    return result
}
