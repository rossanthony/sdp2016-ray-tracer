package com.mildlyskilled

import java.awt.image.BufferedImage

import com.mildlyskilled.harness.DefaultHarness

class ImageSpec extends DefaultHarness {

  val image = new Image(40, 40)

  behavior of "ImageSpec"

  it should "produce a new buffered image" in {
    assert(image.im.isInstanceOf[BufferedImage])
  }

  it should "have a height and width" in {
    image.im.getHeight should be(40)
    image.im.getWidth should be(40)
  }
}
