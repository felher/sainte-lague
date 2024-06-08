package org.felher.sainte_lague

import com.raquo.laminar.api.L.*
import org.felher.beminar.Bem

object ParsedInput:
  val bem = Bem("/parsed-input")

  def render[A](
      start: A,
      in: Signal[A],
      out: Observer[A],
      stringify: A => String,
      parse: String => Option[A]
  )(using CanEqual[A, A]): HtmlElement =
    val model = Var(stringify(start))
    println(stringify(start))

    input(
      bem("invalid" -> model.signal.map(parse).map(_.isEmpty)),
      controlled(
        value <-- model,
        onInput.mapToValue --> (v =>
          model.set(v)
          parse(v).foreach(v => out.onNext(v))
        )
      )
    )
