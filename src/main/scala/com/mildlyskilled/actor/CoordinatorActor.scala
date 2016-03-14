package com.mildlyskilled.actor

import akka.actor.{Actor, ActorLogging}
import com.mildlyskilled.{Image}


class CoordinatorActor(outputFile: String, image: Image) extends Actor with ActorLogging {

 def receive = ???

}
