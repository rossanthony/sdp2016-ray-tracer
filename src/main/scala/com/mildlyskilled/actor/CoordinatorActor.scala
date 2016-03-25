package com.mildlyskilled.actor

import akka.actor.{Actor, ActorLogging, Props}
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

  def init(im: Image, of: String, debug: Boolean) = {
    image = im
    outfile = of
    waiting = im.width * im.height
    start = System.currentTimeMillis
    debugOutput = debug
  }

  def receive = {
    case StartUp(debug: Boolean) => {
      this.init(img, outputFile, debug)
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

    case ProcessRow(row: Int, height: Int, width: Int, scene: Scene) => {
      // Spawn a new TracerActor to handle this particular row of pixels
      val tracer = context.actorOf(Props(new TracerActor(scene, height, width)), "tracer" + row)
      if (debugOutput) log.info(s"Processing row: $row")
      // Send the tracer a message to start working on the row
      tracer ! row
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
