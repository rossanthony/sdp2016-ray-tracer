package com.mildlyskilled

import com.mildlyskilled.harness.DefaultHarness


class ShapeSpec extends DefaultHarness {

  behavior of "ShapeSpec"

  val vector = Vector(20, 3, 5)
  val shape = new Sphere(vector, 10, Color(0, 0, 1), 2)

  "A Shape" should "have a colour" in {
    assert(shape.color === Color.blue)

  }

  it should "intersect" in {
    assert(shape.intersect(new Ray(vector, vector)) === List(
      Vector(29.600307.toFloat, 4.4400463.toFloat, 7.400077.toFloat),
      Vector(10.399693.toFloat, 1.5599539.toFloat, 2.5999231.toFloat)))
  }

  it should "shine" in {
    shape.shine should be(2.0)
  }

  it should "reflect" in {
    shape.reflect should be (2.0)
  }

  "specular" should "be the same as shine" in {
    assert(shape.specular === shape.shine)
  }
}
