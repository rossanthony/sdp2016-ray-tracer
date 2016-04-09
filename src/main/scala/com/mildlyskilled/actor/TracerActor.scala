package com.mildlyskilled.actor

import akka.actor.{ActorLogging, Actor}
import com.mildlyskilled._
import com.mildlyskilled.protocol.CoordinatorProtocol.SetColor

/**
  * Tracer Actor - calculates the pixels per row and sends a `SetColor` message
  * back to the coordinator for each pixel in the row
  *
  */
class TracerActor(scene: Scene, height: Int, width: Int) extends Actor with ActorLogging {

  def receive = {
    // y = variable in the for loop from scene, it represents one row of pixels
    case y: Int => {
      for (x <- 0 until width) {
        var colour = Color.black

        for (dx <- 0 until scene.ss) {
          for (dy <- 0 until scene.ss) {

            // Create a vector to the pixel on the view plane formed when
            // the eye is at the origin and the normal is the Z-axis.
            val dir = Vector(
              (scene.sinf * 2 * ((x + dx.toFloat / scene.ss) / width - .5)).toFloat,
              (scene.sinf * 2 * (height.toFloat / width) * (.5 - (y + dy.toFloat / scene.ss) / height)).toFloat,
              scene.cosf.toFloat).normalized

            val c = scene.trace(Ray(scene.eye, dir)) / (scene.ss * scene.ss)
            colour += c
          }
        }

        if (Vector(colour.r, colour.g, colour.b).norm < 1)
          scene.t.darkCount += 1
        if (Vector(colour.r, colour.g, colour.b).norm > 1)
          scene.t.lightCount += 1

        // Send back a message to the sender (the CoordinatorActor)
        // to set the color of the current pixel
        sender ! SetColor(x, y, colour)
      }
    }
    case rect: Rectangle => {
      for (x <- rect.x until rect.x + rect.width) {
        for (y <- rect.y until rect.y + rect.height) {
          var colour = Color.black

          for (dx <- 0 until scene.ss) {
            for (dy <- 0 until scene.ss) {

              // Create a vector to the pixel on the view plane formed when
              // the eye is at the origin and the normal is the Z-axis.
              val dir = Vector(
                (scene.sinf * 2 * ((x + dx.toFloat / scene.ss) / width - .5)).toFloat,
                (scene.sinf * 2 * (height.toFloat / width) * (.5 - (y + dy.toFloat / scene.ss) / height)).toFloat,
                scene.cosf.toFloat).normalized

              val c = scene.trace(Ray(scene.eye, dir)) / (scene.ss * scene.ss)
              colour += c
            }
          }

          if (Vector(colour.r, colour.g, colour.b).norm < 1)
            scene.t.darkCount += 1
          if (Vector(colour.r, colour.g, colour.b).norm > 1)
            scene.t.lightCount += 1

          // Send back a message to the sender (the CoordinatorActor)
          // to set the color of the current pixel
          sender ! SetColor(x, y, colour)
        }
      }
//      println("Rect: " + rect.x+ ":" + rect.y+ " width: "+rect.width+" height: " +rect.height)
    }
  }
}
