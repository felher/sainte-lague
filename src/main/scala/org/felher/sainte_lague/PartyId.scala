package org.felher.sainte_lague

import io.circe.*
import java.util.UUID

opaque type PartyId = UUID

object PartyId:
  def random(): PartyId = UUID.randomUUID()

  given Encoder[PartyId]    = Encoder.encodeUUID
  given Decoder[PartyId]    = Decoder.decodeUUID
  given KeyEncoder[PartyId] = KeyEncoder.encodeKeyUUID
  given KeyDecoder[PartyId] = KeyDecoder.decodeKeyUUID
  given Ordering[PartyId]   =
    Ordering[(Long, Long)].on[UUID](uuid => (uuid.getLeastSignificantBits(), uuid.getMostSignificantBits()))
