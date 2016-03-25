import com.mildlyskilled._
import com.mildlyskilled.actor.CoordinatorActor
import akka.actor.{Props, ActorSystem}
import com.mildlyskilled.protocol.CoordinatorProtocol.StartUp

object TraceMain extends App {
  val width = 800
  val height = 600

  val (infile, outfile) = ("src/main/resources/input.dat", "output.png")
  val scene = FileReader.parse(infile)

  render(scene, outfile, width, height)

  def render(scene: Scene, outfile: String, width: Int, height: Int) = {

    val image = new Image(width, height)

    // Start the CoordinatorActor
    val system = ActorSystem("coordinatorActor")
    val coordinatorActor = system.actorOf(Props(new CoordinatorActor(outfile, image)), "coordinatorActor")

    // Tell the CoordinatorActor to start up
    coordinatorActor ! StartUp(debug = true)

    // Initialize the Scene by sending the ActorRef for the CoordinatorActor
    scene.init(coordinatorActor)

    // Kick off the ray tracing...
    scene.traceImage(width, height)
  }
}
