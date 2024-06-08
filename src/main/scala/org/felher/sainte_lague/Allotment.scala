package org.felher.sainte_lague

import com.raquo.laminar.api.L.*
import org.felher.beminar.Bem

object Allotment:
  val bem = Bem("/allotment")

  private val canvasWidth   = 200.0
  private val canvasHeight  = 100.0
  private val canvasPadding = 0.5

  private val svgWidth  = 200.0 + 2 * canvasPadding
  private val svgHeight = 100.0 + 2 * canvasPadding

  def render(state: State): SvgElement =
    require(state.parties.length > 0)

    val numBins     = state.parties.length
    val numPaddings = numBins - 1
    val padding     = if numPaddings == 0 then 0 else canvasWidth / 10 / numPaddings
    val binWidth    = (canvasWidth - padding * numPaddings) / numBins
    val max         = (state.parties.map(_.allotted.toDouble) ++ state.parties.map(_.ideal)).maxOption.getOrElse(1.0)
    val formatter   = TextFormatter(state)

    svg.svg(
      svg.viewBox(s"0 0 $svgWidth $svgHeight"),
      svg.rect(
        bem("/border"),
        svg.x("0"),
        svg.y("0"),
        svg.width(svgWidth.toString),
        svg.height(svgHeight.toString)
      ),
      state.parties.zipWithIndex.map((party, idx) =>
        val height      = canvasHeight * (party.allotted.toDouble / max)
        val idealHeight = canvasHeight * (party.ideal / max)
        val xStart      = canvasPadding + idx * (padding + binWidth)
        val xMiddle     = xStart + binWidth / 2

        svg.g(
          svg.rect(
            bem("/alloted-votes"),
            svg.x(xStart.toString),
            svg.y((canvasPadding + canvasHeight - height).toString),
            svg.width(binWidth.toString),
            svg.height(height.toString)
          ),
          svg.line(
            bem("/ideal-line"),
            svg.x1(xMiddle.toString),
            svg.x2(xMiddle.toString),
            svg.y1((canvasPadding + canvasHeight - idealHeight).toString),
            svg.y2((canvasPadding + canvasHeight).toString)
          ),
          svg.line(
            bem("/ideal-line"),
            svg.x1(xStart.toString),
            svg.x2((xStart + binWidth).toString),
            svg.y1((canvasPadding + canvasHeight - idealHeight).toString),
            svg.y2((canvasPadding + canvasHeight - idealHeight).toString)
          ), {
            val text = formatter.format(party)
            val fs   = getFontSize(text, binWidth / 2, canvasHeight)
            val x    = (xMiddle).toString
            val y    = (canvasPadding + canvasHeight - fs).toString

            svg.text(
              bem("/text-info"),
              svg.fontSize(fs.toString),
              svg.transform(s"translate($x, $y) rotate(-90)"),
              text
            )
          }
        )
      ),
      svg.line(
        bem("/zero-line"),
        svg.x1(canvasPadding.toString),
        svg.x2((canvasPadding + canvasWidth).toString),
        svg.y1((canvasPadding + canvasHeight).toString),
        svg.y2((canvasPadding + canvasHeight).toString)
      )
    )

  private def getFontSize(text: String, maxHeight: Double, maxWidth: Double): Double =
    Math.min(
      maxHeight,
      maxWidth / (text.size + 1) * 1.5
    )

  private class TextFormatter(state: State):
    private def numDigits(i: Int): Int =
      if i < 10 then 1 else 1 + numDigits(i / 10)

    private def numWholeDigits(d: Double): Int =
      String.format("%f", d).indexOf('.')

    private val maxAllottedDigits = state.parties.map(_.allotted).map(numDigits).maxOption.getOrElse(1)
    private val maxIdealWholeDigits    = state.parties.map(_.ideal).map(numWholeDigits).maxOption.getOrElse(1)
    private val maxNameLetters    = state.parties.map(_.name.length).maxOption.getOrElse(1)

    def format(p: Party): String =
      println(s"%${maxNameLetters}s: %${maxAllottedDigits}d (% ${maxIdealWholeDigits + 3}.2f)")
      s"%${maxNameLetters}s: %${maxAllottedDigits}d (%${maxIdealWholeDigits + 3}.2f)".format(p.name, p.allotted, p.ideal)
