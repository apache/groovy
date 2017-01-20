package bugs

def list = [[1,2],[3,4]] as List<List<Integer>>
println list
println 'bye'
assert [[1,2],[3,4]] == list
