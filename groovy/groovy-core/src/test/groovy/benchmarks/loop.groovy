import java.util.ArrayList

class Loop {
  property array = new ArrayList()
  property pos = 0

  void push(obj){
     array[pos] = obj
     pos = pos + 1
  }
  Object pop(){
     pos = pos - 1
     return array[pos]
  }

  static void main(args){
     s = new Loop()
     for (i in 1..1000000){
       s.push(i)
     }
     for (i in 1..1000000){
       s.pop()
     }
  }
}

