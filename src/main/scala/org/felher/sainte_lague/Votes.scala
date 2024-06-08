package org.felher.sainte_lague

import com.raquo.laminar.api.L.*
import org.felher.beminar.Bem

object Votes:
  val bem = Bem("/votes")

  def render(state: Var[State]): HtmlElement =
    div(
      bem,
      div(
        bem("/header-row"),
        div("Party"),
        div("Number of Votes")
      ),
      children <-- state.signal
        .map(_.parties)
        .split(_.id)((id, start, sig) =>
          div(
            bem("/row"),
            ParsedInput
              .render(
                start.name,
                sig.map(_.name),
                Observer[String](name => state.update(_.setPartyName(id, name))),
                _.toString,
                Some.apply
              )
              .amend(bem("/party-name")),
            ParsedInput
              .render(
                start.votes,
                sig.map(_.votes),
                Observer[Int](i => state.update(_.setPartyVotes(id, i))),
                _.toString,
                _.toIntOption
              )
              .amend(bem("/input")),
            div(Bem("/button", "tertiary"), "Remove", onClick --> (_ => state.update(_.removeParty(id))))
          )
        ),
      addPartyButton(state)
    )

  private def addPartyButton(state: Var[State]): HtmlElement =
    div(
      className("button"),
      bem("/add-new-button"),
      "New Party",
      onClick --> (_ =>
        val id = PartyId.random()
        state.update(_.addParty(id))
      )
    )
