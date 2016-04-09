package com.mildlyskilled

import akka.actor.ActorRef
import com.mildlyskilled.protocol.CoordinatorProtocol.{ProcessRectangle, ProcessRow}


object Scene {

  import java.io.{FileReader, LineNumberReader}
  import scala.annotation.tailrec

  def fromFile(file: String) = {
    val in = new LineNumberReader(new FileReader(file))
    val (objects, lights) = readLines(in, Nil, Nil)
    new Scene(objects, lights)
  }

  @tailrec
  private def readLines(in: LineNumberReader, objects: List[Shape], lights: List[Light]): (List[Shape], List[Light]) = {
    val line = in.readLine
    if (line == null) {
      (objects, lights) match {
        case (Nil, _) => throw new RuntimeException("no objects")
        case (_, Nil) => throw new RuntimeException("no lights")
        case (os, ls) => (os.reverse, ls.reverse)
      }
    }
    else {
      val fields = line.replaceAll("#.*", "").trim.split("\\s+").filter(_ != "")
      fields.headOption match {
        case Some("sphere") =>
          val Array(x, y, z, rad, r, g, b, shine) = fields.tail.map(_.toFloat)
          readLines(in, Sphere(Vector(x, y, z), rad, Color(r, g, b), shine) :: objects, lights)
        case Some("light") =>
          val Array(x, y, z, r, g, b) = fields.tail.map(_.toFloat)
          readLines(in, objects, Light(Vector(x, y, z), Color(r, g, b)) :: lights)
        case None =>
          // blank line
          readLines(in, objects, lights)
        case Some(x) =>
          throw new RuntimeException("unknown command: " + x)
      }
    }
  }
}

class Scene(val objects: List[Shape], val lights: List[Light]) {

  def this(p: (List[Shape], List[Light])) = this(p._1, p._2)

  val t = new Trace
  // Anti-aliasing parameter -- divide each pixel into sub-pixels and
  // average the results to get smoother images.
  val ss = t.AntiAliasingFactor
  val ambient = .2f
  // controls the brightness
  val background = Color.black
  val eye = Vector.origin
  val angle = 90f // viewing angle
  //val angle = 180f // fisheye

  val frustum = (.5 * angle * math.Pi / 180).toFloat

  val cosf = math.cos(frustum)
  val sinf = math.sin(frustum)

  var coordinatorActor: ActorRef = null

  // Allow setting of the ActorRef for the Coordinator from TraceMain
  def init(actorReference: ActorRef) = {
    coordinatorActor = actorReference
  }

  def traceImage(width: Int, height: Int) {
    (0 until height).par foreach {
      row: Int => {
        // Send message back to the CoordinatorActor, it will then
        // spawn a new TracerActor to process the row
        coordinatorActor ! ProcessRow(row)
      }
    }
  }

  def traceImage(width: Int, height: Int, squareSide: Int): Unit = {
      traceImage(width,height,squareSide,squareSide);
  }

  def traceImage(width: Int, height: Int, rectWidth: Int, rectHeight: Int): Unit = {
    var w = rectWidth;
    var h = rectHeight;
    if (w > width) w = width;
    if (h > height) h = height;
    val maxX = (width / w);
    var offSetX = width - maxX * w;
    val maxY = (height / h);
    var offSetY = height - maxY * h;
    for (i <- 0 until maxX) {
      for (j <- 0 until maxY) {
        var width = w;
        var height = h;
        if (i == maxX - 1) width += offSetX;
        if (j == maxY - 1) height += offSetY;
        coordinatorActor ! ProcessRectangle(Rectangle(i * w, j * h, width, height));
      }
    }
  }

  def shadow(ray: Ray, l: Light): Boolean = {
    val distSquared = (l.loc - ray.orig).normSquared
    intersections(ray).foreach {
      case (v, o) =>
        if ((v - ray.orig).normSquared < distSquared)
          return true
    }
    false
  }

  // Compute the color contributed by light l at point v on object o.
  def shade(ray: Ray, l: Light, v: Vector, o: Shape): Color = {
    val toLight = Ray(v, (l.loc - v).normalized)

    val N = o.normal(v)

    // Diffuse light
    if (shadow(toLight, l) || (N dot toLight.dir) < 0)
      Color.black
    else {
      // diffuse light
      val diffuse = o.color * (N dot toLight.dir)

      println("ray " + ray)
      println("diffuse " + diffuse)

      // specular light
      val R = reflected(-toLight.dir, N)
      val len = ray.dir.norm * R.norm

      val specular = if (len > 1e-12) {
        // Want the angle between R and the vector TO the eye

        val cosalpha = -(ray.dir dot R) / len

        val power = if (cosalpha > 1e-12) math.pow(cosalpha, o.shine).toFloat else 0.0f

        if (power > 1e-12) {
          val scale = o.reflect * power
          l.color * o.specular * scale
        }
        else
          Color.black
      }
      else
        Color.black

      println("specular " + specular)

      val color = diffuse + specular

      println("color " + color + " 0x" + color.rgb.toHexString)

      color
    }
  }

  def reflected(v: Vector, N: Vector): Vector = v - (N * 2.0f * (v dot N))

  def intersections(ray: Ray) = objects.flatMap {
    o => o.intersect(ray).map { v => (v, o) }
  }

  def closestIntersection(ray: Ray) = intersections(ray).sortWith {
    case ((v1, o1), (v2, o2)) => {
      val q1 = (v1 - ray.orig).normSquared
      val q2 = (v2 - ray.orig).normSquared
      q1 < q2
    }
  }.headOption

  val maxDepth = 3

  def trace(ray: Ray): Color = trace(ray, maxDepth)

  private def trace(ray: Ray, depth: Int): Color = {
    t.rayCount += 1

    // Compute the intersections of the ray with every object, sort by
    // distance from the ray's origin and pick the closest to the origin.
    val r = closestIntersection(ray)

    r match {
      case None => {
        // If no intersection, the color is black
        background
      }
      case Some((v, o)) => {
        // Compute the color as the sum of:

        t.hitCount += 1

        // The contribution of each point light source.
        var c = lights.foldLeft(Color.black) {
          case (c, l) => c + shade(ray, l, v, o)
        }

        // The contribution of the ambient light.
        c += o.color * ambient

        // Return the color.
        c
      }
    }
  }
}

