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
println

variables = [
    "Amount of mortgage" : 0.0, 
    "Annual interest rate (%)" : 0.0, 
    "Loan duration (months)" : 0.0, 
    "Monthly payments" : 0.0
]

for (entry in variables.entrySet()) {
    print("${entry.key}:")
    userInput = System.in.readLine()
    if ("" == userInput) {
        valueToCalculate = entry.key
    } else {
        entry.value = userInput.toDouble()
    }
}

println "${valueToCalculate} = ${calculateValueOf(valueToCalculate)}"





def calculateValueOf(valueToCalculate) {
    result = 0
    principal = variables["Amount of mortgage"]
    interest = variables["Annual interest rate (%)"] / 1200
    months = variables["Loan duration (months)"]
    payment = variables["Monthly payments"]

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
        diff = 100; accuracy = 0.00001; maxIterations = 1000
        index = 0
        while ((diff > accuracy) && (index < maxIterations)) {
            temp = result
            numerator = (principal * temp / payment) + Math.pow((1 + temp), -months) - 1
            denominator= (principal / payment) - months * Math.pow((1 + temp), (-months - 1))
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
