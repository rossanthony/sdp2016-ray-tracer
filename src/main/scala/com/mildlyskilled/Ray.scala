package com.mildlyskilled

case class Ray(orig: Vector, dir: Vector)

case class Light(loc: Vector, color: Color) {
  def brightness = 1.0f
}
