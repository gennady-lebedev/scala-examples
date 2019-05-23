package dev.rudiments.examples.streams

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Simple extends App {
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  val hundred: Source[Int, NotUsed] = Source(1 to 100)
  val fizzbuzz: Flow[Int, String, NotUsed] = Flow.fromFunction {
    case i if i % 3 == 0 && i % 5 == 0 => "fizz buzz"
    case i if i % 3 == 0 => "fizz"
    case i if i % 5 == 0 => "buzz"
    case i => i.toString
  }

  val factorial: Source[BigInt, NotUsed] = hundred.scan(BigInt(1))(_ * _)

  val printer: Sink[Any, Future[Done]] = Sink.foreach(println)

  Await.result(hundred.via(fizzbuzz).runWith(printer), Duration("1 second"))
  Await.result(factorial.runWith(printer), Duration("1 second"))

  val zipped = factorial.zipWith(Source(0 to 100))((f, i) => s"$i! = $f")
  Await.result(zipped.runWith(printer), Duration("5 seconds"))
}
