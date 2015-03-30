import akka.actor.{ Actor, ActorRef, Props, ActorSystem }
import akka.util.Timeout
//used for postfix operator "seconds" 
import scala.concurrent.duration._
//used for "? and !" methods of akka
import akka.pattern.ask
//used for future objects
import scala.concurrent.ExecutionContext.Implicits.global
//used with onComplete method
import scala.util.Failure
import scala.util.Success

case class Line(string: String)
case class Words(words: Integer)

class WordCounterActor extends Actor {
  def receive = {
    case Line(string) => {
      if (!(string.isEmpty())) {
        val countOfWords = string.trim.split(" ").toList.filterNot(_ equals ("\n")).length
        sender ! Words(countOfWords)
      } else
        sender ! Words(0)
    }
    case _ => println("Error: message not recognized")
  }
}

case object CountNow

class AddWordCountActor(filename: String) extends Actor {

  private var totalLines = 0
  private var linesProcessed = 0
  private var result = 0
  private var fileSender: Option[ActorRef] = None

  def receive = {
    case CountNow => {
      fileSender = Some(sender) // save reference to process invoker
      import scala.io.Source._
      fromFile(filename).getLines.foreach { line =>
        context.actorOf(Props[WordCounterActor]) ! Line(line.stripLineEnd)
        totalLines += 1
      }
    }

    case Words(words) => {
      result += words
      linesProcessed += 1
      if (linesProcessed == totalLines) {
        fileSender.map(_ ! result) // provide result to process invoker
      }
    }
    case _ => println("message not recognized!")
  }
}

object Sample extends App {
  val system = ActorSystem("System")
  val filename = "resources/file1"
  val actor = system.actorOf(Props(new AddWordCountActor(filename)))
  implicit val timeout = Timeout(25 seconds)
  val future = (actor ? CountNow).mapTo[Int]
  future onComplete {
    case Success(res) => {
      println("\n::::::::::::No. of Words in the File are ::::::::::\n\t\t\t" + res + "\n")
      system.shutdown
    }
    case Failure(r) => {
      println("\n::::::::::::Actor call Failed, actor system shuting down::::::::::\n ")
      system.shutdown
    }
  }
}