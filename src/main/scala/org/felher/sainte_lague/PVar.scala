package org.felher.sainte_lague

import com.raquo.laminar.api.L.*
import io.circe.*
import org.scalajs.dom.window

object PVar:
  def localStorageKey(suffix: String): String =
    "org.felher.saint_lague.sfdemo" + suffix

  /**
   * This function is unsafe in the sense that its default owner is the unsafeWindowOwner,
   * which means that the subscripts will not be cleaned up. You need to specify a
   * dedicated owner for the subscription if you want to use it for non-global/ephemeral
   * components.
   */

  def apply[A](name: String, default: A, owner: Owner = unsafeWindowOwner)(using
      enc: Encoder[A],
      dec: Decoder[A]
  ): Var[A] =
    val key = localStorageKey(name)
    val v   = window.localStorage.getItem(key) match
      case null => Var(default)
      case s    =>
        io.circe.parser.decode[A](s) match
          case Left(_)  => Var(default)
          case Right(a) => Var(a)

    v.signal.foreach(value => window.localStorage.setItem(key, enc(value).noSpaces))(owner)

    v
