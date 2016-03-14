package com.mildlyskilled

import com.mildlyskilled.harness.DefaultHarness

class ColorSpec extends DefaultHarness {

  val colour = Color(0.45.toFloat, .20.toFloat, .10.toFloat)
  val colour1 = Color(0.30.toFloat, .20.toFloat, .10.toFloat)
  val colorObject = Color
  val white = Color(23,2,3)

  behavior of "ColorSpec"

  it should "have an RGB tuple" in {
    colour.rgbTuple should be((115,51,25))
  }

  it should "have an rgb value" in {
    colour.rgb should be(7549721)
  }

  "Given another colour, a colour" should "be 'mixable' with the other colour" in {
    val colour2 = colour.mix(colour1)
    colour2.rgbTuple should be((34,10,2))
    colour2.rgb should be(2230786)
  }

  it should "be 'addable' to another colour" in {
    colour + colour1 should be(Color(0.75.toFloat,0.4.toFloat,0.2.toFloat))
  }

  it should "be 'subtractable' from another color" in {
    colour - colour1 should be(Color(0.14999998.toFloat,0.0.toFloat,0.0.toFloat))
  }

  it should "be 'multipliable' by a float " in {
    colour * 2 should be(Color(0.90.toFloat,0.40.toFloat,0.20.toFloat))
  }

  it should "be divisible by a float" in {
    (colour * 2) / 2 should be(colour)
  }

  "Given r,g,b values greater than one a colour" should "return an RGB tuple (255, 255, 255)" in {
    white.rgbTuple should be((255,255,255))
  }

  "The Colour object" should "Have the primary, secondary colours as well as black and white" in {
    colorObject.black should be(Color(0,0,0))
    colorObject.white should be(Color(1,1,1))
    colorObject.white should be(Color(1,1,1))
    colorObject.red should be(Color(1, 0, 0))
    colorObject.blue should be(Color(0,0,1))
    colorObject.green should be(Color(0,1,0))
    colorObject.magenta should be(colorObject.red + colorObject.blue)
    colorObject.cyan should be(colorObject.green + colorObject.blue)
    colorObject.yellow should be(colorObject.red + colorObject.green)
  }

  // Not sure about this one
  it should "derive a colour object given an RGB value" in {
    colorObject.fromRGB(colour.rgb) should be(Color(colour.rgbTuple._1, colour.rgbTuple._2, colour.rgbTuple._3))
  }
}
