# Akka actor implementation of a Ray Tracer

Group coursework submission for SDP MSc module at Birkbeck, team members are as follows:

- Andrew McLean
- Grzegorz Skiba
- Ross Anthony (rantho01)
- Zhi Cong Goh


### To run

Either run from IntelliJ via the TraceMain method, or enter `sbt run` in the terminal.


### To test

Run `sbt test`


### Method of approach

There are two main “Actor” classes, in this folder: [/src/main/scala/com/mildlyskilled/actor/](src/main/scala/com/mildlyskilled/actor/) which have been implemented.

The CoordinatorActor is the main (top level) Actor which is started in TraceMain (line 34) when the app is run...

```scala
    coordinatorActor ! StartUp(debug, numWorkers, height, width, scene)
```

StartUp initialises a router for the TracerActor allowing multiple rows or subsets of pixels to be processed concurrently - see line 33 of CoordinatorActor...

```scala
    tracerRouter = context.actorOf(
      Props(new TracerActor(scene, height, width)).withRouter(BalancingPool(numWorkers)),"tracerRouter")
```

A reference to the CoordinatorActor is then passed into the init method of the Scene, to enable it to send messages back to the CoordinatorActor...

```scala
    // Initialize the Scene by sending the ActorRef for the CoordinatorActor
    scene.init(coordinatorActor)
```

Then the following is lastly run in TraceMain (line 39)...

```scala
    // Kick off the ray tracing...
    if(traceByRow)
      scene.traceImage(width,height);
    else
    //trace by rectangle
      scene.traceImage(width, height, rectWidth,rectHeight);
```

The traceImage method, line 73 of the [Scene class](src/main/scala/com/mildlyskilled/Scene.scala), has been modified to loop over the rows in the image and send a ProcessRow message back to the CoordinatorActor. This then spawns a new TracerActor and passes through the scene object etc, it then sends each row off to the TracerActor router to be distributed to one of the available worker Actors.

Another implementation of traceImage has also been added which splits up each row into smaller chunks. This is used if the `traceByRow` val is set to false in TraceMain.

The TracerActor receives messages from the CoordinatorActor, either one per row or in chunks. It then loops over the pixels in the row/chunk and performs the calculations. A SetColor message is then sent back to the Coordinator so it can append the pixel to the image buffer and decrements the ‘waiting’ var, which keeps track of the number of pixels waiting to the processed.

The very last message sent to SetColor in the CoordinatorActor will then trigger the image file to be generated, i.e. whenever ‘waiting’ <= 0. At this point the task is done, so it calls `context.system.terminate()` to shutdown the Actor system.


### Other things to note

Actor Routing: the TracerActor is using a “BalancingPool” router, we also tried the SmallestMailbox router, but didn't notice any significant improvement in speed.

Dispatcher: currently the CoordinatorActor is configured to use a ThreadPool dispatcher, this seems to be the most efficient. Config for this can be found in /src/main/resources/common.conf and application.conf.


### Assessing efficiency/speed

Results from running on a 2013 macbook 2 GHz Intel Core i7, with 4 cores...

|numWorkers|Runtime(s)|
|---|------------------------|
|1	| 3252ms, 3854ms, 3232ms |
|2	| 2113ms, 2370ms, 2214ms |
|3	| 2041ms, 1897ms, 1789ms |
|4	| 1885ms, 1908ms, 1533ms |
|5	| 1527ms, 1682ms, 2015ms |
|6	| 2247ms, 1496ms, 1640ms |
|7	| 1771ms, 1867ms, 2017ms |
|8	| 1849ms, 1672ms, 2115ms |
|9	| 1817ms, 2099ms, 2210ms |
|10 | 1959ms, 2380ms, 1640ms |

The standard non-concurrent version takes around 3200ms on my system, so roughly the same as when the Akka version is configured to spawn only one worker for the TracerActor router, which makes sense.

As mentioned already, we experimented with dividing up the tasks given to the TracerActor into smaller chunks, to see if this might increase the efficiency, i.e each worker is dealing with a smaller piece of work. It made a slight improvement to the average runtime, it might be more noticeable on a machine with more cores.
