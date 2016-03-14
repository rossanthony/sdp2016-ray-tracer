package com.mildlyskilled

import com.mildlyskilled.harness.DefaultHarness

class LightSpec extends DefaultHarness {
  val light = Light(Vector(20, 10, 5), Color(1,1,1))

  behavior of "LightSpec"

  it should "have a brightness value" in {
    light.brightness should be(1f)
  }
}
