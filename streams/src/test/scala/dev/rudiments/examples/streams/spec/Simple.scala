package dev.rudiments.examples.streams.spec

import akka.actor.ActorSystem
import org.scalatest.{AsyncFlatSpec, Matchers, WordSpec}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import akka.stream._
import akka.stream.scaladsl._
import com.typesafe.scalalogging.StrictLogging

@RunWith(classOf[JUnitRunner])
class Simple extends WordSpec with Matchers with StrictLogging {
  private implicit val actorSystem: ActorSystem = ActorSystem()
  private implicit val mat: ActorMaterializer = ActorMaterializer()

  "runs stream with simple range value" in {
    Source(1 to 20).map {
      case i if i % 3 == 0 && i % 5 == 0 => "fizz buzz"
      case i if i % 3 == 0 => "fizz"
      case i if i % 5 == 0 => "buzz"
      case i => i.toString
    }.runForeach(s => logger.info(s))
  }
}
