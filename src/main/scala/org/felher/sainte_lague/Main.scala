package org.felher.sainte_lague

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import org.felher.beminar.Bem

@main def main(): Unit =
  val bem   = Bem("/app")
  val state = PVar("state", State.empty())
  state.update(_.reallot)

  render(
    dom.document.getElementsByTagName("body")(0),
    div(
      Panel
        .render(
          "General",
          div(
            bem("/general"),
            div(
              bem("/intro-text"),
              """
                Hi there.

                This is a small calculator for the Webster method, also called
                the Sainte-Laguë method.

                You can use it calculate how a fixed number of seats are
                assigned to a fixed number of parties, depending on how many
                votes the respective parties received.

                You can change the number of seats, how many parties there are,
                what their names are, as well as how many votes they received.

                The results are plotted and printed. The number of seats
                received are the filled rectangles and normal numbers,
                the number of ideal seats are the lines through the middle of
                each bar as well as the numbers in parentheses. 

                You can also see a log of the allotment process at the bottom.
              """.split("\n\n").map(text => p(text))
            ),
            div(
              bem("/property"),
              div(bem("/property-title"), "Number of Seats"),
              ParsedInput
                .render(
                  state.now().seats,
                  state.signal.map(_.seats),
                  Observer(seats => state.update(_.setSeats(seats))),
                  _.toString,
                  _.toIntOption.filter(_ > 0)
                )
                .amend(bem("/property-value"))
            ),
            div(
              Bem("/button"),
              "Reset Everything",
              onClick --> (_ => state.set(State.empty()))
            )
          )
        )
        .amend(bem("/panel")),
      Panel
        .render(
          "Parties and Votes",
          Votes.render(state)
        )
        .amend(bem("/panel")),
      child <-- state.signal.map(state =>
        Panel
          .render(
            "Allotment",
            Allotment.render(state).amend(bem("/allotment"))
          )
          .amend(bem("/panel"))
      ),
      Panel
        .render(
          "Log",
          div(
            bem("/result"),
            children <-- state.signal.map(state => state.log.map(line => div(bem("result-line"), line)))
          )
        )
        .amend(bem("/panel"))
    )
  )
