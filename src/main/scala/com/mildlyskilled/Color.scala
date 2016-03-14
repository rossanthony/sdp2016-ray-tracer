package com.mildlyskilled

object Color {
  val black = Color(0, 0, 0)
  val red = Color(1, 0, 0)
  val green = Color(0, 1, 0)
  val blue = Color(0, 0, 1)
  val magenta = red + blue
  val cyan = green + blue
  val yellow = red + green
  val white = red + green + blue

  def fromRGB(x: Int) = {
    val a = (x >>> 24) & 255
    val r = (x >>> 16) & 255
    val g = (x >>> 8) & 255
    val b = x & 255
    Color(r, g, b)
  }
}

case class Color(r: Float, g: Float, b: Float) {
  def rgbTuple = (up(r), up(g), up(b))

  private def up(x: Float) = (x * 256).toInt max 0 min 255

  def rgb = {
    val (r, g, b) = rgbTuple
    (r << 16) | (g << 8) | b
  }

  def +(c: Color) = Color(r + c.r, g + c.g, b + c.b)

  def -(c: Color) = Color(r - c.r, g - c.g, b - c.b)

  def *(a: Float) = Color(r * a, g * a, b * a)

  def /(a: Float) = Color(r / a, g / a, b / a)

  def mix(c: Color) = Color(
    r * c.r,
    g * c.g,
    b * c.b)
}
