package com.taxibooking

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class TaxiManagerSpec(_system: ActorSystem) extends TestKit(_system) with Matchers with WordSpecLike with BeforeAndAfterAll {
  import TaxiManager._

  def this() = this(ActorSystem("TaxiManagerSpec"))
  override def afterAll: Unit = shutdown(system)

  "TaxiManager actor" should {
    "be able to register taxis" in {
      val probe = TestProbe()
      val taxiManager = system.actorOf(TaxiManager.props)
      taxiManager.tell(RegisterTaxi, probe.ref)
      probe.expectMsg("Taxi 1 registered.")
      taxiManager.tell(RegisterTaxi, probe.ref)
      probe.expectMsg("Taxi 2 registered.")
    }
    "reply none when booking initially with no taxis available" in {
      val probe = TestProbe()
      val taxiManager = system.actorOf(TaxiManager.props)
      taxiManager.tell(Booking(Point(1, 1), Point(-1, -1)), probe.ref)
      probe.expectMsg(None)
    }
    "reply booked with car_id and total_time when booking with taxis available" in {
      val probe = TestProbe()
      val taxiManager = system.actorOf(TaxiManager.props)
      taxiManager.tell(RegisterTaxi, probe.ref)
      probe.expectMsg("Taxi 1 registered.")
      taxiManager.tell(RegisterTaxi, probe.ref)
      probe.expectMsg("Taxi 2 registered.")
      taxiManager.tell(Booking(Point(1, 1), Point(-1, -1)), probe.ref)
      probe.expectMsg(Some(Booked(1, 6)))
      taxiManager.tell(Booking(Point(-1, -1), Point(2, 2)), probe.ref)
      probe.expectMsg(Some(Booked(2, 8)))
    }
    "reply none when booking after last available taxi is taken" in {
      val probe = TestProbe()
      val taxiManager = system.actorOf(TaxiManager.props)
      taxiManager.tell(RegisterTaxi, probe.ref)
      probe.expectMsg("Taxi 1 registered.")
      taxiManager.tell(RegisterTaxi, probe.ref)
      probe.expectMsg("Taxi 2 registered.")
      taxiManager.tell(Booking(Point(1, 1), Point(-1, -1)), probe.ref)
      probe.expectMsg(Some(Booked(1, 6)))
      taxiManager.tell(Booking(Point(-1, -1), Point(2, 2)), probe.ref)
      probe.expectMsg(Some(Booked(2, 8)))
      taxiManager.tell(Booking(Point(1, -1), Point(-1, 1)), probe.ref)
      probe.expectMsg(None)
    }
    "be able to tick all taxis to update their statuses and locations" in {
      val probe = TestProbe()
      val taxiManager = system.actorOf(TaxiManager.props)
      taxiManager.tell(RegisterTaxi, probe.ref)
      probe.expectMsg("Taxi 1 registered.")
      taxiManager.tell(RegisterTaxi, probe.ref)
      probe.expectMsg("Taxi 2 registered.")
      taxiManager.tell(Booking(Point(1, 1), Point(-1, -1)), probe.ref)
      probe.expectMsg(Some(Booked(1, 6)))
      taxiManager.tell(Booking(Point(-1, -1), Point(2, 2)), probe.ref)
      probe.expectMsg(Some(Booked(2, 8)))
      taxiManager.tell(Tick, probe.ref)
      probe.expectMsg("Taxis ticked.")
    }
    "be able to reset all taxis' statuses and locations" in {
      val probe = TestProbe()
      val taxiManager = system.actorOf(TaxiManager.props)
      taxiManager.tell(RegisterTaxi, probe.ref)
      probe.expectMsg("Taxi 1 registered.")
      taxiManager.tell(RegisterTaxi, probe.ref)
      probe.expectMsg("Taxi 2 registered.")
      taxiManager.tell(Booking(Point(1, 1), Point(-1, -1)), probe.ref)
      probe.expectMsg(Some(Booked(1, 6)))
      taxiManager.tell(Booking(Point(-1, -1), Point(2, 2)), probe.ref)
      probe.expectMsg(Some(Booked(2, 8)))
      taxiManager.tell(Reset, probe.ref)
      probe.expectMsg("Taxis reset.")
    }
  }
}