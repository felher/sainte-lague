package org.felher.sainte_lague

import cats.data.*
import cats.syntax.all.*

final case class SainteLague(
    seats: Map[PartyId, SainteLague.Seat],
    log: List[String]
)

object SainteLague:
  final case class Seat(allotted: Int, ideal: Double)

  def calc(seats: Int, parties: List[Party]): SainteLague =
    val votes            = parties.map(p => p.id -> p.votes).toMap
    val names            = parties.map(p => p.id -> p.name).toMap
    val sumVotes         = votes.values.sum.toDouble
    val initDiv          = sumVotes / seats.toDouble
    val idealSeats       = votes.mapValues(v => seats.toDouble * (v.toDouble / sumVotes))
    val initialAllotment = votes.toList.map((p, v) => p -> Math.round(v.toDouble / initDiv).toInt).toMap

    def tick(allotment: Map[PartyId, Int]): Writer[List[String], Map[PartyId, Int]] =
      val limits   = votes.toList.map((pid, votes) => pid -> getLimitForParty(votes, allotment(pid))).toMap
      val allotted = allotment.values.sum

      if allotted != seats then
        for
          _                <- List(s"Wrong number of seats allotted. $seats available, $allotted allotted").tell
          strategy          = if allotted < seats then incStrategy else decStrategy
          newDiv            = strategy.getNewDiv(limits)
          _                <- Writer.tell(List(f"New div will be ~$newDiv%.3f"))
          newSeatPartiesRaw = strategy.getPartiesToBeChanged(newDiv, limits)
          newSeatParties    = newSeatPartiesRaw.take(Math.abs((seats - allotted)))
          _                <- (newSeatParties == newSeatPartiesRaw) match
                                case true  =>
                                  Writer.tell(
                                    List(s"The parties ${newSeatParties.map(names).mkString(", ")} should change their seat")
                                  )
                                case false =>
                                  Writer.tell(List(s"The parties ${newSeatPartiesRaw
                                      .map(names)
                                      .mkString(", ")} should change their seats, but only ${newSeatParties.map(names).mkString(", ")} can"))
          result           <-
            tick(
              newSeatParties.foldLeft(allotment)((allotment, party) =>
                allotment.updated(party, allotment(party) + strategy.allotmentDelta)
              )
            )
        yield result
      else Writer.tell(List("Number of allotted seats same as number of seats. Done!")).as(allotment)

    val prog =
      for
        _      <- List(s"Initial devisor = votes / seats = ${sumVotes} / ${seats} = ${initDiv}").tell
        _      <- List(
                    s"Initial allotment: ${parties.map(p => s"${p.name}: ${initialAllotment(p.id)}").mkString(", ")}"
                  ).tell
        result <- tick(initialAllotment)
      yield result

    val (log, allotment) = prog.run
    val result           = allotment.toList.map((pid, seats) => pid -> Seat(seats, idealSeats(pid))).toMap
    SainteLague(result, log)

  final private case class Limit(lessSeatsDiv: Double, moreDivsSeat: Double)

  final private case class Strategy(
      getNewDiv: Map[PartyId, Limit] => Double,
      getPartiesToBeChanged: (Double, Map[PartyId, Limit]) => List[PartyId],
      allotmentDelta: Int
  )

  private val decStrategy = Strategy(
    _.values.map(_.lessSeatsDiv).min,
    (newDiv, limits) => limits.toList.flatMap((p, l) => if l.lessSeatsDiv == newDiv then List(p) else Nil),
    -1
  )

  private val incStrategy = Strategy(
    _.values.map(_.moreDivsSeat).max,
    (newDiv, limits) => limits.toList.flatMap((p, l) => if l.moreDivsSeat == newDiv then List(p) else Nil),
    +1
  )

  private def getLimitForParty(votesForParty: Int, curSeatsOfParty: Int): Limit =
    val lessSeatsDiv =
      if curSeatsOfParty == 0 then Double.MaxValue else votesForParty / (curSeatsOfParty.toDouble - 0.5)
    val moreSeatsDiv = votesForParty / (curSeatsOfParty + 0.5)
    Limit(lessSeatsDiv, moreSeatsDiv)
