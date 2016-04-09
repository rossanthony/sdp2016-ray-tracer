package com.mildlyskilled

import java.time.Duration

import akka.actor.{ActorRef, Props, Actor, ActorSystem}
import akka.testkit.{TestProbe, TestActorRef, EventFilter}
import com.mildlyskilled.actor._
import com.mildlyskilled.harness.ActorHarness
import com.mildlyskilled.protocol.CoordinatorProtocol._
import com.mildlyskilled.protocol.CoordinatorProtocol
import java.io.File
import org.scalatest.matchers._


//http://doc.scalatest.org/2.2.6/#org.scalatest.matchers.BePropertyMatcher
trait CustomMatchers {
  class FileBePropertyMatcher extends BePropertyMatcher[java.io.File] {
    def apply(left: java.io.File) = BePropertyMatchResult(left.isFile, "file")
  }
  val file = new FileBePropertyMatcher
}


class TracerTestActor extends Actor {
  def receive = {
    case y: Int => { sender ! Color.black }
    case _ => println
  }
}


class CoordinatorSpec(_system: ActorSystem) extends ActorHarness with CustomMatchers {
  def this() = this(ActorSystem("RayTest"))

  val height = 2
  val width = 2
  val image = new Image(height, width)
  val infile = "src/test/resources/input.dat"
  val outfile = "outTest.png"
  val coordActor = TestActorRef(Props(classOf[CoordinatorActor], outfile, image))
  coordActor.underlyingActor.asInstanceOf[CoordinatorActor].init(image, outfile, false)
  val scene = FileReader.parse(infile)
  val traceActor = TestActorRef(Props(classOf[TracerActor], scene, height, width))
  val probe1 = TestProbe()

  "a Tracer actor " must {
    "produce a SetColor class when receiving an integer " in {
      traceActor ! 0
      expectMsgClass(classOf[CoordinatorProtocol.SetColor])
    }
  }

  " a Coordinator actor " must {
    "produce a warning message when receiving random message " in {
      //http://alvinalexander.com/java/jwarehouse/akka-2.3/akka-testkit/src/main/scala/akka/testkit/TestEventListener.scala.shtml
      EventFilter.warning() intercept {
        coordActor ! "four"
      }
    }
  }

  " a Coordinator actor " must {
    " produce an output file when receiving a SetColor message (after waiting) " in {

      for (i <- 1 to height * width) {
        coordActor ! SetColor(1, 0, Color.black)
      }
      expectMsgClass(classOf[CoordinatorProtocol.SetColor])

      val outputFile: java.io.File = new File(outfile)
      try {
        outputFile must be a (file)
      } finally {
        outputFile.delete()
      }
    }
  }

  //testing the startup procedure. If startup is successful it will return the time taken else it will return a string
  " a Coordinator actor " must {
    " start timing and fill a tracer Router when it receives a valid input " in {
      coordActor ! StartUp(false, 1, height, width, scene)
      expectMsgType[java.lang.Long]
    }
  }
  " a Coordinator actor " must {
    " send a message to the tracer router to process a particular row and tracerActor should set the color " in {
      /* Method1
      coordActor ! (probe1.ref)
      coordActor ! ProcessRow(1)
      probe1.send(coordActor, ProcessRow(1))

*/
      //method 2
      probe1 watch coordActor
      coordActor!ProcessRow(1)
      probe1.expectTerminated(coordActor)
    }
  }

}

