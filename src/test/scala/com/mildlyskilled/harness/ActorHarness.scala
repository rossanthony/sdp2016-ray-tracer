package com.mildlyskilled.harness

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, Suite, WordSpecLike}

class ActorHarness extends TestKit(ActorSystem("testSystem"))
  // Using the ImplicitSender trait will automatically set `testActor` as the sender
  with ImplicitSender
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll {
  this: TestKit with Suite =>
  override protected def afterAll(): Unit ={
    super.afterAll()
    system.terminate()
  }
}
