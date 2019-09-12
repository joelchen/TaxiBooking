package com.taxibooking

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.pattern.ask
import akka.stream.ActorMaterializer
import scala.io.StdIn

/** HTTP server with [[com.taxibooking.TaxiService]] routing. */
object TaxiServer extends TaxiService {
  implicit val system = ActorSystem("TaxiBooking")
  /** ActorRef of TaxiManager actor. */
  val taxiManager = system.actorOf(TaxiManager.props)

  /** Setup HTTP server. */
  def setup {
    import TaxiManager._

    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    // Initialize with 3 taxis
    for (_ <- 1 to 3) {
      for {
        // Send RegisterTaxi message to TaxiManager and get String message reply
        s <- ask(taxiManager, RegisterTaxi).mapTo[String]
      } yield println(s)
    }

    println("Server online at http://localhost:8080\nPress RETURN to stop...")
    // Binds TaxiService directive to localhost:8080
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    // Listens for newline before proceeding
    StdIn.readLine()
    // Unbinds TaxiService directive and terminate system
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}