package org.felher.sainte_lague

import com.raquo.laminar.api.L.*
import org.felher.beminar.Bem
import com.raquo.laminar.nodes.ReactiveElement

object Panel:
  val bem = Bem("/panel")

  def render(title: String, element: ReactiveElement.Base): HtmlElement =
    div(
      bem,
      div(bem("/title"), title),
      div(bem("/upper-right")),
      div(bem("/content"), element)
    )
