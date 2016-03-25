package com.mildlyskilled.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.mildlyskilled.{Image, Color, Scene}
import com.mildlyskilled.protocol.CoordinatorProtocol._


class CoordinatorActor(outputFile: String, img: Image) extends Actor with ActorLogging {

 // Number of pixels awaiting processing
 var waiting = 0
 var outfile: String = null
 var image: Image = null

 def init(im: Image, of: String) = {
  image = im
  outfile = of
  waiting = im.width * im.height
 }

 def receive = {
  case StartUp => {
   println(s"Starting up the Coordinator Actor")
   this.init(img, outputFile)
   println(s"Pixels to be processed: $waiting")
  }

  case SetColor(x: Int, y: Int, c: Color) => {
   set(x, y, c)
   //println(s"Pixels waiting to be processed: $waiting")
   if (waiting <= 0) {
    println(s"Generating the output png file...")
    print
    println(s"Terminating the Actor system...")
    context.system.terminate()
   }
  }

  case ProcessRow(row: Int, height: Int, width: Int, scene: Scene) => {
   //println(s"Processing the next row: $row")
   // Spawn a new TracerActor to handle this particular row of pixels
   val tracer = context.actorOf(Props(new TracerActor(scene, height, width)), "tracer" + row)
   // Send the tracer a message to start working on the row
   tracer ! row
  }

  case _ => println("Unrecognised message passed to CoordinatorActor")
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
