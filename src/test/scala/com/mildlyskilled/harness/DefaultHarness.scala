package com.mildlyskilled.harness

import org.scalatest._

trait DefaultHarness extends FlatSpec with Matchers with OptionValues with Inside with Inspectors
