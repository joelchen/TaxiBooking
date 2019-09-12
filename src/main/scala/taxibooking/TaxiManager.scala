package com.taxibooking

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import scala.collection.mutable.ArrayBuffer

/** Factory for [[com.taxibooking.TaxiManager]] instances. */
object TaxiManager {
  /** Configuration object using in creating TaxiManager actor. */
  def props():Props = Props(new TaxiManager)
  /** Coordinates of location in 2D grid world. */
  final case class Point(x: Int, y: Int)
  /** Status and location of taxi. */
  final case class StatusLocation(status: TaxiStatus, location: Point)
  /** Message to add Taxi actor into system. */
  case object RegisterTaxi
  /** Message to make a taxi booking. */
  final case class Booking(source: Point, destination: Point)
  /** Message to return car_id and total_time. */
  final case class Booked(car_id: Long, total_time: Long)
  /** Message to update taxi's status and location. */
  case object Tick
  /** Message to reset taxi's status and location. */
  case object Reset
  /** Taxi status. */
  sealed trait TaxiStatus
  /** Taxi available status. */
  case object TaxiAvailable extends TaxiStatus
  /** Taxi pickup status. */
  case object TaxiPickup extends TaxiStatus
  /** Taxi busy status. */
  case object TaxiBusy extends TaxiStatus
}

/** TaxiManager actor. */
class TaxiManager extends Actor with ActorLogging {
  import TaxiManager._

  /** Defines TaxiManager's behavior. */
  override def receive = management(0, Vector.empty, Vector.empty)

  /** Receive method with management behavior and parameterized state keeping.
   *
   * @param total Total number of Taxi actors.
   * @param taxisStatusLocation Vector of status and location of all Taxi actors.
   * @param taxisActorRef Vector of ActorRef of all Taxi actors.
   */
  private def management(total: Long, taxisStatusLocation:Vector[(TaxiStatus, Point)], taxisActorRef: Vector[ActorRef]): Receive = {
    // RegisterTaxi message received
    case RegisterTaxi =>
      val taxisCount = total + 1
      val taxi = context.actorOf(Taxi.props(taxisCount), s"${taxisCount}")
      // Change receive method with updated set of parameters
      context.become(management(taxisCount, taxisStatusLocation :+ (TaxiAvailable, Point(0, 0)), taxisActorRef :+ taxi))
      sender ! s"Taxi ${taxisCount} registered."
    // Booking message received
    case book @ Booking(source, destination) =>
      // Build an array of index and distance of all taxis
      val arrayIndexDistance = ArrayBuffer.empty[(Int, Long)]
      taxisStatusLocation.zipWithIndex.foreach { case ((status, location), index) =>
        if (status == TaxiAvailable) {
          // Find distance between taxi and customer
          val distance = Math.abs(location.x - source.x) + Math.abs(location.y - source.y)
          arrayIndexDistance.append((index, distance))
        }
      }
      if (arrayIndexDistance.isEmpty) {
        log.info("No available taxis.")
        // Send None message to sender
        sender ! None
      } else {
        log.info("Taxis available.")
        // Sort array of index and distance by distance in ascending order
        val sortedIndexDistance = arrayIndexDistance.sortBy(_._2)
        // Get ActorRef of taxi with shortest distance to customer
        val taxi = taxisActorRef(sortedIndexDistance(0)._1)
        // Send Book message to Taxi actor with shortest distance to customer
        taxi ! book
        // Change receive method with updated set of parameters
        context.become(management(total, taxisStatusLocation.updated(sortedIndexDistance(0)._1, (TaxiPickup, taxisStatusLocation(sortedIndexDistance(0)._1)._2)), taxisActorRef))
        // Send car_id and total_time to sender
        sender ! Option(Booked(sortedIndexDistance(0)._1 + 1, sortedIndexDistance(0)._2 + Math.abs(destination.x - source.x) + Math.abs(destination.y - source.y)))
      }
    // Tick message received
    case Tick =>
      // Send Tick message to all Taxi actors
      taxisActorRef.foreach { taxi =>
        taxi ! Tick
      }
      sender ! "Taxis ticked."
    // Reset message received
    case Reset =>
      // Send Reset message to all Taxi actors
      taxisActorRef.foreach { taxi =>
        taxi ! Reset
      }
      sender ! "Taxis reset."
    // StatusLocation message received
    case StatusLocation(status, location) =>
      // Get index of Taxi actor with updated status and location
      val index = taxisActorRef.indexOf(sender)
      log.info("ActorRef: {}, Index: {}, Status: {}, Location: {}", sender, index, status, location)
      // Change receive method with updated Taxi actor's status and location
      context.become(management(total, taxisStatusLocation.updated(index, (status, location)), taxisActorRef))
  }
}