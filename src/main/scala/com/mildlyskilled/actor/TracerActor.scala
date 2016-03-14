package com.mildlyskilled.actor

import akka.actor.{ActorLogging, Actor}
import com.mildlyskilled._
/**
  * Created by kwabena on 29/02/2016.
  */


class TracerActor(objects:List[Shape], lights:List[Light])
  extends Actor with ActorLogging {
  def receive = ???
}
