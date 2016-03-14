package com.mildlyskilled

import com.mildlyskilled.harness.DefaultHarness


class SceneSpec extends DefaultHarness {

  behavior of "SceneSpec"

  val inputFile = "src/test/resources/input.dat"

  val scene = FileReader.parse(inputFile)

}
