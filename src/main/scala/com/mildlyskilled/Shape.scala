package com.mildlyskilled

sealed trait Shape {
  def normal(v: Vector): Vector

  def intersect(ray: Ray): List[Vector]

  def shine: Float

  def reflect: Float = shine

  def specular: Float = shine

  def color: Color
}

case class Sphere(val center: Vector, val radius: Float, val color: Color, val shine: Float) extends Shape {

  // normal vector at point v on surface of the sphere
  def normal(v: Vector): Vector = (v - center).normalized

  def intersect(ray: Ray): List[Vector] = {
    // vector to the center
    val vc = center - ray.orig

    // The ray is ray.orig + t * ray.dir
    // We want to solve for t to compute the intersections.

    // coefficients of the quadratic equation for t
    val A = ray.dir.normSquared
    val B = 2.0f * (ray.dir dot vc)
    val C = vc.normSquared - radius * radius

    val disc = B * B - 4.0f * A * C

    if (disc < 0)
      return Nil

    val d = math.sqrt(disc)

    val t0 = (-B + d) / (2.0 * A)
    val t1 = (-B - d) / (2.0 * A)

    val p0 = ray.orig + ray.dir * t0.toFloat
    val p1 = ray.orig + ray.dir * t1.toFloat

    List(p0, p1)
  }
}

