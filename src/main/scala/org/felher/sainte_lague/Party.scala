package org.felher.sainte_lague

import io.circe.*

final case class Party(
    id: PartyId,
    name: String,
    votes: Int,
    allotted: Int,
    ideal: Double
) derives io.circe.Codec.AsObject
