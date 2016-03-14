package com.mildlyskilled

import akka.actor.{Props, Actor}
import akka.testkit.{TestActorRef}
import com.mildlyskilled.actor._
import com.mildlyskilled.harness.ActorHarness
import com.mildlyskilled.protocol.CoordinatorProtocol._

class TracerTestActor extends Actor {
  def receive = {
    case _ => println
  }
}

class CoordinatorSpec extends ActorHarness {

}
