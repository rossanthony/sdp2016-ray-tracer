# Akka actor implementation of a Ray Tracer

Group coursework submission for SDP MSc module at Birkbeck, team members are as follows:

- Andrew McLean
- Grzegorz Skiba
- Ross Anthony (rantho01)
- Zhi Cong Goh


### To run

Either run from IntelliJ via the TraceMain method, or enter `sbt run` in the terminal.


### Progress so far

The two main “Actor” classes, in this folder: [/src/main/scala/com.mildlyskilled/actor/](tree/master/src/main/scala/com.mildlyskilled/actor/) have been implemented.

The CoordinatorActor is the main Actor which is started in TraceMain (line 31) when the app is run...

```scala
    coordinatorActor ! StartUp(debug, numWorkers, height, width, scene)
```

StartUp then spins up a router for the TracerActor allowing multiple rows to be processed concurrently - see line 32 of CoordinatorActor...

```scala
    tracerRouter = context.actorOf(
      Props(new TracerActor(scene, height, width)).withDispatcher("thread-pool-dispatcher").withRouter(BalancingPool(numWorkers)),
      "tracerRouter"
    )
```

A reference to the CoordinatorActor is then passed into the init method of the Scene, to enable it to send messages back to the Coordinator...

```scala
    // Initialize the Scene by sending the ActorRef for the CoordinatorActor
    scene.init(coordinatorActor)
```

Then the following is lastly run in TraceMain (line 37)...

```scala
    // Kick off the ray tracing...
    scene.traceImage(width, height)
```

The traceImage method, line 74 of the [Scene class](tree/master/src/main/scala/com.mildlyskilled/Scene.scala), has been modified to loop over the rows in the image and send a ProcessRow message back to the CoordinatorActor. This then spawns a new TracerActor and passes through the scene object etc, it then sends each row off to the TracerActor router to be distributed to one of the available worker Actors.

The TracerActor receives messages from the CoordinatorActor, currently one message per row. It then loops over the pixels in the row and performs the calculations. A SetColor message is then sent back to the Coordinator so it can append the pixel to the image buffer and decrements the ‘waiting’ var, which keeps track of the number of pixels waiting to the processed.

The very last message sent to SetColor in the CoordinatorActor will then trigger the image file to be generated, i.e. whenever ‘waiting’ <= 0. At this point the task is done, so it calls `context.system.terminate()` to shutdown the Actor system.

### Things outstanding

1. Try dividing up the tasks given to the TracerActor into smaller chunks. This might increase the efficiency if each worker is dealing with a smaller piece of work. Currently each worker deals with one row at a time, so as the image is 600 pixels high by 800 wide, we end up with 600 jobs distributed around the workers (each with 800 pixels to be processed).  Perhaps each row could be sub-divided into pixels and the TracerActor worker then just deals with one pixel at a time?

2. Write some Tests for the Actors: the existing tests can be run with “set test” from the project root folder. I haven’t added any additional tests yet, the pre-existing ones still run and all pass. There is a class called ActorHarness added by Kwabena Kaning, under /src/test/scala/com.mildlyskilled/harness/. This seems to be a starting point for writing tests for the Actors.

3. Investigate the best approach for Actor Routing: the TracerActor is currently using the “BalancingPool” router, I also tried the SmallestMailbox router, but didn't notice any significant improvement in speed.

4. Investigate the best approach for handling the dispatcher. Currently the CoordinatorActor is configured to use a ThreadPool dispatcher, from my initial trials this seems to be the most efficient. Config for this can be found in /src/main/resources/common.conf and application.conf.

5. Any other tweaks or improvements anyone can thing of?


### Assessing efficiency/speed

Results from running on my macbook, which has 4 cores...

| numWorkers | Runtime(s) |
| - | -                            |
|1	|		3252ms, 3854ms, 3232ms |
|2	|		2113ms, 2370ms, 2214ms |
|3	|		2041ms, 1897ms, 1789ms |
|4	|		1885ms, 1908ms, 1533ms |
|5	|		1527ms, 1682ms, 2015ms |
|6	|		2247ms, 1496ms, 1640ms |
|7	|		1771ms, 1867ms, 2017ms |
|8	|		1849ms, 1672ms, 2115ms |
|9	|		1817ms, 2099ms, 2210ms |
|10 |		1959ms, 2380ms, 1640ms |

For some reason results seem to vary quite a bit, so it will be best to take averages when comparing different approaches to routing and the issue of whether to divide up the work into smaller chunks.

The standard non-concurrent version takes around 3200ms on my system, so roughly the same as when the Akka version is configured to spawn only one worker for the TracerActor router, which makes sense.

