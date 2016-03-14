package com.mildlyskilled

import com.mildlyskilled.harness.DefaultHarness
import org.scalatest.FlatSpec

/**
  * Created by kwabena on 22/02/2016.
  */
class VectorSpec extends DefaultHarness {

  behavior of "VectorSpec"

  val vector1 = Vector(0, 0, 0)
  val vector2 = Vector(6, 0, 0)
  val vectorObject = Vector

  it should "have a z vector value" in {
    vectorObject.z should be(Vector(0, 0, 1))
  }

  it should "have a y vector value" in {
    vectorObject.y should be(Vector(0, 1, 0))
  }

  it should "have an x vector value" in {
    vectorObject.x should be(Vector(1, 0, 0))
  }

  it should "the companion object must have an origin which should be a vector" in {
    vectorObject.origin should be (Vector(0, 0, 0))
  }

  it should "sum to another vector" in {
    assert(vector1 + vector2 === Vector(6.0.toFloat, 0.0.toFloat, 0.0.toFloat))
  }

  it should "be must form a product given a double " in {
    val vector3 = vector2 * 2

    assert(vector3 === Vector(12.0.toFloat,0.0.toFloat,0.0.toFloat))
    vector3.x should be(12)


  }

  it should "be divisible by a double" in {
    val vector4 = (vector2 * 2) / 2
    vector4.x should be(6)
  }

  "Given another vector if dot is called it" should "return a float " in {
    vector1 dot vector2 should be(0)
  }

}
