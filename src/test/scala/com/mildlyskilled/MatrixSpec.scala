package com.mildlyskilled

import com.mildlyskilled.harness.DefaultHarness

class MatrixSpec extends DefaultHarness {
  val matrix = Matrix(Vector(1,2,3), Vector(4,5,6), Vector(7,8,9))

  behavior of "MatrixSpec"

  it should "have a column 0" in {
    matrix.col0 should be(Vector(1,4,7))
  }

  it should "have a column 1" in {
    matrix.col1 should be(Vector(2,5,8))
  }

  it should "have a column 2" in {
    matrix.col2 should be(Vector(3,6,9))
  }

  it should "be 'multipliable' by a vector" in {
    matrix * Vector(1,2,3) should be(Vector(14f,32f,50f))
  }

  it should "be 'addable' to another matrix" in {
    matrix + matrix should be(Matrix(Vector(2f,4f,6f),Vector(8f,10f,12f),Vector(14f,16f,18f)))
  }

  it should "be 'multipliable' by another matrix" in {
    matrix * matrix should be(Matrix(Vector(30f,36f,42f),Vector(66f,81f,96f),Vector(102f,126f,150f)))
  }
}
