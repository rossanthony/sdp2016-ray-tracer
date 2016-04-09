package com.mildlyskilled.actor

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import akka.routing.BalancingPool       // attempts to distribute work evenly between Actors
import akka.routing.SmallestMailboxPool // sends messages to the Actor with the fewest messages in its mailbox.
import com.mildlyskilled.{Image, Color, Scene}
import com.mildlyskilled.protocol.CoordinatorProtocol._


class CoordinatorActor(outputFile: String, img: Image) extends Actor with ActorLogging {

  // Number of pixels awaiting processing
  var waiting = 0
  var outfile: String = null
  var image: Image = null
  var start: Long = 0
  var duration: Long = 0
  var debugOutput: Boolean = false
  var tracerRouter: ActorRef = null

  def init(im: Image, of: String, debug: Boolean) = {
    image = im
    outfile = of
    waiting = im.width * im.height
    start = System.currentTimeMillis
    debugOutput = debug
  }

  def receive = {
    case StartUp(debug: Boolean, numWorkers: Int, height: Int, width: Int, scene: Scene) => {
      this.init(img, outputFile, debug)
      tracerRouter = context.actorOf(
        Props(new TracerActor(scene, height, width)).withRouter(BalancingPool(numWorkers)),
        "tracerRouter"
      )
      duration = System.currentTimeMillis - start
      if (debugOutput) log.info(s"Starting up the Coordinator Actor, time elapsed: ${duration}ms, pixels to be processed: $waiting")
    }

    case SetColor(x: Int, y: Int, c: Color) => {
      //if (debugOutput) log.info(s"Setting colour: x:$x, y:$y, colour:$c")
      set(x, y, c)
      if (waiting <= 0) {
        // print the image png file
        print
        if (debugOutput)
          log.info(s"Runtime: ${System.currentTimeMillis - start}ms, terminating the Actor system...")
        // Job done, so terminate the Actor system
        context.system.terminate()
      }
    }

    case ProcessRow(row: Int) => {
      // Send the tracer router a message to start working on this row
      //if (debugOutput) log.info(s"Processing row: $row")
      tracerRouter ! row
    }
    case ProcessRectangle(rect: com.mildlyskilled.Rectangle)=>{
      tracerRouter ! rect
    }

    case x => log.warning("Received unknown message: {}", x)
  }

  private def set(x: Int, y: Int, c: Color) = {
    image(x, y) = c
    waiting -= 1
  }

  private def print = {
    assert(waiting == 0)
    image.print(outputFile)
  }

}
