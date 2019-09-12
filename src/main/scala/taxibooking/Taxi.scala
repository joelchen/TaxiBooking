package com.taxibooking

import akka.actor.{ Actor, ActorLogging, Props }

/** Factory for [[com.taxibooking.Taxi]] instances. */
object Taxi {
  /** Configuration object using in creating Taxi actor. */
  def props(id: Long): Props = Props(new Taxi(id))
}

/** Taxi actor.
 *
 *  @param id Taxi ID
 */
class Taxi(id: Long) extends Actor with ActorLogging {
  import TaxiManager._

  /** Called asynchronously when an Taxi is started. */
  override def preStart(): Unit = log.info("Taxi actor {} started.", id)
  /** Called asynchronously after Taxi is stopped. */
  override def postStop(): Unit = log.info("Taxi actor {} stopped.", id)
  /** Defines Taxi's behavior. */
  override def receive: Receive = available(Point(0, 0))

  /** Receive method with available behavior and parameterized state keeping.
   *
   * @param location Location of taxi.
   */
  private def available(location: Point): Receive = {
    // Booking message received
    case Booking(source, destination) =>
      log.info("Customer is traveling from {} to {}.", source, destination)
      // Become pickup behavior by changing receive method
      context.become(pickup(location, source, destination))
      // Send pickup status and current location of taxi to sender
      sender ! StatusLocation(TaxiPickup, location)
    // Tick message received
    case Tick =>
      log.info("Tick...")
      sender ! StatusLocation(TaxiAvailable, location)
    // Reset message received
    case Reset =>
      reset
  }

  /** Receive method with pickup behavior and parameterized state keeping.
   *
   * @param location Location of taxi.
   * @param source Location of customer.
   * @param destination Location of customer's destination.
   */
  private def pickup(location: Point, source: Point, destination: Point): Receive = {
    // Tick message received
    case Tick =>
      log.info("Tick...")
      // Get taxi's location 1 unit towards destination
      val current = update(location, source)
      if (current == source) {
        log.info("Taxi arrived customer's pickup point.")
        // Become busy behavior by changing receive method
        context.become(busy(current, destination))
        // Send busy status and current location of taxi to sender
        sender ! StatusLocation(TaxiBusy, current)
      } else {
        log.info("Taxi is going to pickup customer.")
        // Become pickup behavior by changing receive method
        context.become(pickup(current, source, destination))
        // Send pickup status and current location of taxi to sender
        sender ! StatusLocation(TaxiPickup, current)
      }
    // Reset message received
    case Reset =>
      reset
  }

  /** Receive method with busy behavior and parameterized state keeping.
   *
   * @param location Location of taxi.
   * @param destination Location of customer's destination.
   */
  private def busy(location: Point, destination: Point): Receive = {
    case Tick =>
      log.info("Tick...")
      // Get taxi's location 1 unit towards destination
      val current = update(location, destination)
      if (current == destination) {
        log.info("Taxi arrived customer's destination.")
        // Become available behavior by changing receive method
        context.become(available(current))
        // Send available status and current location of taxi to sender
        sender ! StatusLocation(TaxiAvailable, current)
      } else {
        log.info("Taxi is going to customer's destination.")
        // Become busy behavior by changing receive method
        context.become(busy(current, destination))
        // Send busy status and current location of taxi to sender
        sender ! StatusLocation(TaxiBusy, current)
      }
    // Reset message received
    case Reset =>
      reset
  }

  /** Method for updating taxi's location 1 unit towards destination
   *
   * @param location Location of taxi.
   * @param destination Location of customer's destination.
   */
  private def update(location: Point, destination: Point): Point = {
    if (location.x < destination.x) {
      // Move 1 unit towards destination in X axis
      location.copy(x = location.x + 1)
    } else if (location.x > destination.x) {
      // Move 1 unit towards destination in X axis
      location.copy(x = location.x - 1)
    } else if (location.y < destination.y) {
      // Move 1 unit towards destination in Y axis
      location.copy(y = location.y + 1)
    } else if (location.y > destination.y) {
      // Move 1 unit towards destination in Y axis
      location.copy(y = location.y - 1)
    } else location
  }

  /** Method for reseting taxi's status and location. */
  private def reset = {
    log.info("Reset taxi's status and location.")
    // Become available behavior by changing receive method
    context.become(available(Point(0, 0)))
    // Send available status and current location of taxi to sender
    sender ! StatusLocation(TaxiAvailable, Point(0, 0))
  }
}