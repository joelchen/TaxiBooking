package com.taxibooking

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class TaxiSpec(_system: ActorSystem) extends TestKit(_system) with Matchers with WordSpecLike with BeforeAndAfterAll {
  import TaxiManager._
  
  def this() = this(ActorSystem("TaxiSpec"))
  override def afterAll: Unit = shutdown(system)

  "Taxi actor" should {
    "be able to make a booking if available" in {
      val probe = TestProbe()

      val taxi = system.actorOf(Taxi.props(1))
      taxi.tell(Booking(Point(1, 1), Point(-1, -1)), probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(0, 0)))
    }
    "not be able to make a booking if status is pickup or busy" in {
      val probe = TestProbe()

      val taxi1 = system.actorOf(Taxi.props(1))
      taxi1.tell(Booking(Point(1, 1), Point(-1, -1)), probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(0, 0)))
      taxi1.tell(Booking(Point(-1, -1), Point(1, 1)), probe.ref)
      probe.expectNoMessage(500.millis)

      val taxi2 = system.actorOf(Taxi.props(2))
      taxi2.tell(Booking(Point(0, 1), Point(1, 0)), probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(0, 0)))
      taxi2.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(0, 1)))
      taxi2.tell(Booking(Point(-1, 1), Point(1, -1)), probe.ref)
      probe.expectNoMessage(500.millis)
    }
    "tick through pickup, busy, and available statuses" in {
      val probe = TestProbe()

      val taxi1 = system.actorOf(Taxi.props(1))
      taxi1.tell(Booking(Point(1, 1), Point(-1, -1)), probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(0, 0)))
      taxi1.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(1, 0)))
      taxi1.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(1, 1)))
      taxi1.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(0, 1)))
      taxi1.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(-1, 1)))
      taxi1.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(-1, 0)))
      taxi1.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiAvailable, Point(-1, -1)))
      taxi1.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiAvailable, Point(-1, -1)))

      val taxi2 = system.actorOf(Taxi.props(2))
      taxi2.tell(Booking(Point(-1, 1), Point(1, -1)), probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(0, 0)))
      taxi2.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(-1, 0)))
      taxi2.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(-1, 1)))
      taxi2.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(0, 1)))
      taxi2.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(1, 1)))
      taxi2.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(1, 0)))
      taxi2.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiAvailable, Point(1, -1)))
      taxi2.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiAvailable, Point(1, -1)))

      val taxi3 = system.actorOf(Taxi.props(3))
      taxi3.tell(Booking(Point(1, -1), Point(-1, 1)), probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(0, 0)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(1, 0)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(1, -1)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(0, -1)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(-1, -1)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(-1, 0)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiAvailable, Point(-1, 1)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiAvailable, Point(-1, 1)))
    }
    "reset after tick through pickup, busy, and available statuses" in {
      val probe = TestProbe()

      val taxi1 = system.actorOf(Taxi.props(1))
      taxi1.tell(Booking(Point(1, 1), Point(-1, -1)), probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(0, 0)))
      taxi1.tell(Reset, probe.ref)
      probe.expectMsg(StatusLocation(TaxiAvailable, Point(0, 0)))

      val taxi2 = system.actorOf(Taxi.props(2))
      taxi2.tell(Booking(Point(-1, 1), Point(1, -1)), probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(0, 0)))
      taxi2.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(-1, 0)))
      taxi2.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(-1, 1)))
      taxi2.tell(Reset, probe.ref)
      probe.expectMsg(StatusLocation(TaxiAvailable, Point(0, 0)))

      val taxi3 = system.actorOf(Taxi.props(3))
      taxi3.tell(Booking(Point(1, -1), Point(-1, 1)), probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(0, 0)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiPickup, Point(1, 0)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(1, -1)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(0, -1)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(-1, -1)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiBusy, Point(-1, 0)))
      taxi3.tell(Tick, probe.ref)
      probe.expectMsg(StatusLocation(TaxiAvailable, Point(-1, 1)))
      taxi3.tell(Reset, probe.ref)
      probe.expectMsg(StatusLocation(TaxiAvailable, Point(0, 0)))
    }
  }
}