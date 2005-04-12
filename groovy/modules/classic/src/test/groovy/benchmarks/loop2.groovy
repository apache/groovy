import java.util.ArrayList

class Loop2 {
  private array = new ArrayList()
  private pos = 0

  void push(obj){
     array[pos] = obj
     pos = pos + 1
  }
  Object pop(){
     pos = pos - 1
     return array[pos]
  }

  static void main(args){
      println "Starting the Loop2 test"
      
     s = new Loop2()
     for (i in 1..1000000){
       s.push(i)
     }
     for (i in 1..1000000){
       s.pop()
     }
  }
}

