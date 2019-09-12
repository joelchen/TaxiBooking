package com.taxibooking

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.json._
import spray.json.DefaultJsonProtocol._

/** HTTP routing, handling, and interfacing with [[com.taxibooking.TaxiManager]]. */
trait TaxiService {
  import TaxiManager._

  /** ActorRef of TaxiManager actor. */
  val taxiManager: ActorRef

  implicit val pointFormat = jsonFormat2(Point)
  implicit val bookingFormat = jsonFormat2(Booking)
  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val bookedJsonWriter = new JsonWriter[Booked] {
    // Write Booked message as JSON
    def write(booked: Booked): JsValue = {
      JsObject(
        "car_id" -> JsNumber(booked.car_id),
        "total_time" -> JsNumber(booked.total_time)
      )
    }
  }

  /** Directive to handle HTTP requests. */
  val route = pathPrefix("api") {
    path("book") {
      // Handle /api/book POST request
      post {
        // Extract JSON from /api/book request
        entity(as[Booking]) { booking =>
          // Send Booking message to TaxiManager actor
          val future = ask(taxiManager, Booking(booking.source, booking.destination)).mapTo[Option[Booked]]
          // Receive Booked message from TaxiManager actor
          onSuccess(future) {
            // Send Booked JSON as response to client
            case Some(b: Booked) => complete(HttpEntity(ContentTypes.`application/json`, b.toJson.toString))
            case None => complete(None)
          }
        }
      }
    } ~
    path("tick") {
      // Handle /api/tick POST request
      post {
        // Send Tick message to TaxiManager actor
        val future = ask(taxiManager, Tick).mapTo[String]
        // Receive String message from TaxiManager actor
        onSuccess(future) {
          // Send String message as response to client
          case s: String => complete(HttpEntity(ContentTypes.`application/json`, s))
        }
      }
    } ~
    path("reset") {
      // Handle /api/tick PUT request
      put {
        // Send Reset message to TaxiManager actor
        val future = ask(taxiManager, Reset).mapTo[String]
        // Receive String message from TaxiManager actor
        onSuccess(future) {
          // Send String message as response to client
          case s: String => complete(HttpEntity(ContentTypes.`application/json`, s))
        }
      }
    }
  }
}