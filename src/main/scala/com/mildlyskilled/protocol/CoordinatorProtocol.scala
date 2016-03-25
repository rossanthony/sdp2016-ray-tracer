package com.mildlyskilled.protocol

import com.mildlyskilled.{Scene, Color}


object CoordinatorProtocol {

  // Events
  sealed trait Event

  // Data
  sealed trait Data


  // Coordinator States
  sealed trait State


  sealed trait Message

  // Messages that can be received by the CoordinatorActor
  case class StartUp(debug: Boolean) extends Message
  case class SetColor(x: Int, y: Int, c: Color) extends Message
  case class ProcessRow(row: Int, height: Int, width: Int, scene: Scene) extends Message

}
