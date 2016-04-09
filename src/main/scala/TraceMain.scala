import com.mildlyskilled._
import com.mildlyskilled.actor.CoordinatorActor
import akka.actor.{Props, ActorSystem}
import com.mildlyskilled.protocol.CoordinatorProtocol.StartUp
import com.typesafe.config.ConfigFactory

object TraceMain extends App {
  val width = 800
  val height = 600
  val debug = true
  val numWorkers = 4 // No speed/efficiency increase noticeable above number of cores on available CPU
  val traceByRow = false;
  val rectWidth = width/2;
  var rectHeight = 1;

  val (infile, outfile) = ("src/main/resources/input.dat", "output.png")
  val scene = FileReader.parse(infile)

  render(scene, outfile, width, height)

  def render(scene: Scene, outfile: String, width: Int, height: Int) = {

    val image = new Image(width, height)

    // Start the CoordinatorActor
    val system = ActorSystem("coordinatorActor", ConfigFactory.load.getConfig("coordinator"))

    val coordinatorActor = system.actorOf(
      Props(new CoordinatorActor(outfile, image)).withDispatcher("thread-pool-dispatcher"),
      "coordinatorActor"
    )

    // Tell the CoordinatorActor to start up
    coordinatorActor ! StartUp(debug, numWorkers, height, width, scene)

    // Initialize the Scene by sending the ActorRef for the CoordinatorActor
    scene.init(coordinatorActor)

    // Kick off the ray tracing...
    if(traceByRow)
      scene.traceImage(width,height);
    else
    //trace by rectangle
      scene.traceImage(width, height, rectWidth,rectHeight);
  }
}
