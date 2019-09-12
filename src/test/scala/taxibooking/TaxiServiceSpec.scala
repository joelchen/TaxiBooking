package com.taxibooking

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.MalformedRequestContentRejection
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class TaxiServiceSpec extends WordSpec with Matchers with ScalatestRouteTest with TaxiService {
  val taxiManager = system.actorOf(TaxiManager.props)

  "TaxiService" should {
    "leave GET/PUT/POST requests to other paths unhandled" in {
      Get("/api/1") ~> route ~> check {
        handled shouldBe false
      }
      Put("/api/2") ~> route ~> check {
        handled shouldBe false
      }
      Post("/api/3") ~> route ~> check {
        handled shouldBe false
      }
    }
    "return reset response in /api/reset" in {
      Put("/api/reset") ~> route ~> check {
        responseAs[String] shouldEqual "Taxis reset."
      }
    }
    "return MalformedRequestContentRejection in /api/book when no JSON is passed" in {
      Post("/api/book") ~> route ~> check {
        rejection shouldBe a[MalformedRequestContentRejection]
        rejection match {
          case MalformedRequestContentRejection(message, _) ⇒
            message should include("expected JSON Value")
        }
      }
    }
    "return nothing when booking initially with no taxis available" in {
      Post("/api/book", HttpEntity(ContentTypes.`application/json`, """{"source": {"x": 1, "y": 0}, "destination": {"x": 0, "y": 1}}""")) ~> route ~> check {
        responseAs[String] shouldEqual ""
      }
    }
    "return car_id and total_time when booking with taxis available" in {
      taxiManager ! TaxiManager.RegisterTaxi
      taxiManager ! TaxiManager.RegisterTaxi
      taxiManager ! TaxiManager.RegisterTaxi
      Post("/api/book", HttpEntity(ContentTypes.`application/json`, """{"source": {"x": 1, "y": 0}, "destination": {"x": 0, "y": 1}}""")) ~> route ~> check {
        responseAs[String] shouldEqual """{"car_id":1,"total_time":3}"""
      }
      Post("/api/book", HttpEntity(ContentTypes.`application/json`, """{"source": {"x": -1, "y": 0}, "destination": {"x": 1, "y": 0}}""")) ~> route ~> check {
        responseAs[String] shouldEqual """{"car_id":2,"total_time":3}"""
      }
      Post("/api/book", HttpEntity(ContentTypes.`application/json`, """{"source": {"x": 0, "y": 1}, "destination": {"x": -1, "y": 0}}""")) ~> route ~> check {
        responseAs[String] shouldEqual """{"car_id":3,"total_time":3}"""
      }
    }
    "return empty when booking after last available taxi is taken" in {
      Post("/api/book", HttpEntity(ContentTypes.`application/json`, """{"source": {"x": 1, "y": 0}, "destination": {"x": 0, "y": 1}}""")) ~> route ~> check {
        responseAs[String] shouldEqual ""
      }
    }
    "be able to tick all taxis to update their statuses and locations" in {
      Post("/api/tick") ~> route ~> check {
        responseAs[String] shouldEqual "Taxis ticked."
      }
      Post("/api/tick") ~> route ~> check {
        responseAs[String] shouldEqual "Taxis ticked."
      }
      Post("/api/tick") ~> route ~> check {
        responseAs[String] shouldEqual "Taxis ticked."
      }
    }
    "return nearest available car to the customer location" in {
      Post("/api/book", HttpEntity(ContentTypes.`application/json`, """{"source": {"x": 2, "y": 0}, "destination": {"x": -1, "y": -1}}""")) ~> route ~> check {
        responseAs[String] shouldEqual """{"car_id":2,"total_time":5}"""
      }
    }
    "return car with smallest ID in the event that there are more than one car near the customer location" in {
      Post("/api/book", HttpEntity(ContentTypes.`application/json`, """{"source": {"x": 0, "y": 0}, "destination": {"x": 1, "y": 1}}""")) ~> route ~> check {
        responseAs[String] shouldEqual """{"car_id":1,"total_time":3}"""
      }
    }
    "be able to reset all taxis' statuses and locations" in {
      Put("/api/reset") ~> route ~> check {
        responseAs[String] shouldEqual "Taxis reset."
      }
      Post("/api/book", HttpEntity(ContentTypes.`application/json`, """{"source": {"x": 2, "y": 2}, "destination": {"x": -2, "y": -2}}""")) ~> route ~> check {
        responseAs[String] shouldEqual """{"car_id":1,"total_time":12}"""
      }
    }
  }
}