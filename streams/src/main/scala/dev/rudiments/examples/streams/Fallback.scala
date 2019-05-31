package dev.rudiments.examples.streams

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl._
import akka.{Done, NotUsed}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Fallback extends App {
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  val hundred: Source[Int, NotUsed] = Source(1 to 100)
  val fizzbuzz: Flow[Int, Either[RainyDay, SunnyDay], NotUsed] = Flow.fromFunction {
    case i if i % 3 == 0 && i % 5 == 0 => Left(RainyDay("fizz buzz"))
    case i if i % 3 == 0 => Left(RainyDay("fizz"))
    case i if i % 5 == 0 => Left(RainyDay("buzz"))
    case i => Right(SunnyDay(i))
  }

  val sunnyPrinter: Sink[SunnyDay, Future[Done]] = Sink.foreach(s => println(s"Success: ${s.i}"))
  val rainyPrinter: Sink[RainyDay, Future[Done]] = Sink.foreach(s => println(s"Failure: ${s.s}"))

  val days: Source[Either[RainyDay, SunnyDay], NotUsed] = hundred.via(fizzbuzz)



  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    val rainy = Flow[Either[RainyDay, SunnyDay]].collect { case Left(day) => day }
    val sunny = Flow[Either[RainyDay, SunnyDay]].collect { case Right(day) => day }

    val bcast = builder.add(Broadcast[Either[RainyDay, SunnyDay]](2))

    hundred ~> fizzbuzz ~> bcast ~> rainy ~> rainyPrinter
                           bcast ~> sunny ~> sunnyPrinter
    ClosedShape
  })

  g.run()
}

case class SunnyDay(i: Int)
case class RainyDay(s: String)