package org.felher.sainte_lague

import scala.collection.immutable.SortedMap

final case class State private (
    seats: Int,
    parties: List[Party],
    log: List[String]
) derives io.circe.Codec.AsObject:
  private lazy val partyIdToIndex: Map[PartyId, Int] = parties.map(_.id).zipWithIndex.toMap
  private lazy val indexToPartyId: Map[Int, PartyId] = parties.map(_.id).zipWithIndex.map((a, b) => (b, a)).toMap

  def setSeats(seats: Int): State =
    copy(seats = seats).reallot

  def setPartyName(id: PartyId, name: String): State =
    updateParty(id, _.copy(name = name))

  def setPartyVotes(id: PartyId, votes: Int): State =
    updateParty(id, _.copy(votes = votes)).reallot

  def setPartyAllottedSeats(id: PartyId, allotted: Int): State =
    updateParty(id, _.copy(allotted = allotted))

  def setPartyIdealSeats(id: PartyId, ideal: Double): State =
    updateParty(id, _.copy(ideal = ideal))

  def removeParty(pid: PartyId): State =
    copy(parties = parties.filterNot(_.id == pid)).reallot

  def addParty(id: PartyId): State =
    copy(parties =
      parties :+ (
        Party(
          id = id,
          name = "Party " + (parties.size + 1),
          votes = 0,
          allotted = 0,
          ideal = 0
        )
      )
    ).reallot

  private def updateParty(pid: PartyId, f: Party => Party): State =
    val newParties = parties.map(party => if party.id == pid then f(party) else party)
    copy(parties = newParties)

  def reallot: State =
    val allotment = SainteLague.calc(seats, parties)
    allotment.seats.toList.foldLeft(this): (st, pidAndSeat) =>
      val (pid, seat) = pidAndSeat

      st
        .setPartyAllottedSeats(pid, seat.allotted)
        .setPartyIdealSeats(pid, seat.ideal)
        .copy(log = allotment.log)

object State:
  def empty(): State = State(
    seats = 100,
    parties = List(
       Party(
        id = PartyId.random(),
        name = "Party 1",
        votes = 100,
        allotted = 100,
        ideal = 100.0
      )
    ),
    log = Nil
  )
