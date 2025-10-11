package scalalib

import scalalib.newtypes.*

object model:

  opaque type Max = Int
  object Max extends RelaxedOpaqueInt[Max]

  opaque type MaxPerPage = Int
  object MaxPerPage extends RichOpaqueInt[MaxPerPage]

  opaque type MaxPerSecond = Int
  object MaxPerSecond extends RichOpaqueInt[MaxPerSecond]

  opaque type Days = Int
  object Days extends RichOpaqueInt[Days]

  opaque type Seconds = Int
  object Seconds extends RelaxedOpaqueInt[Seconds]

  trait Percent[A]:
    def value(a: A): Double
    def apply(a: Double): A
  object Percent:
    def of[A](w: TotalWrapper[A, Double]): Percent[A] = new:
      def apply(a: Double): A = w(a)
      def value(a: A): Double = w.value(a)
    def toInt[A](a: A)(using p: Percent[A]): Int = Math.round(p.value(a)).toInt // round to closest

  /* play.api.i18n.Lang is composed of language and country.
   * Let's make new types for those so we don't mix them.
   */
  opaque type Language = String
  object Language extends OpaqueString[Language]

  opaque type Country = String
  object Country extends OpaqueString[Country]

  /* A IETF BCP 47 language tag representing a locale.
   * See [[java.util.Locale.toLanguageTag]].
   * Is returned by [[play.api.i18n.Lang.code]].
   * E.g. "tr" or "en-US"
   */
  opaque type LangTag = String
  object LangTag extends OpaqueString[LangTag]
