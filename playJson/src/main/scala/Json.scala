package scalalib
package json

import scala.annotation.nowarn
import scala.util.NotGiven
import play.api.libs.json.{ Json as PlayJson, * }
import java.time.Instant
import alleycats.Zero

import scalalib.newtypes.SameRuntime
import scalalib.time.toMillis

object Json:

  trait NoJsonHandler[A] // don't create default JSON handlers for this type

  given Zero[JsObject]:
    def zero = JsObject(Seq.empty)

  @nowarn("msg=unused implicit parameter")
  given [A, T] => (
      bts: SameRuntime[A, T],
      stb: SameRuntime[T, A],
      format: Format[A]
  ) => NotGiven[NoJsonHandler[T]] => Format[T] =
    format.bimap(bts.apply, stb.apply)

  given [A] => (sr: SameRuntime[A, String]) => KeyWrites[A]:
    def writeKey(key: A) = sr(key)

  private val stringFormatBase: Format[String] = Format(Reads.StringReads, Writes.StringWrites)
  private val intFormatBase: Format[Int] = Format(Reads.IntReads, Writes.IntWrites)

  def stringFormat[A <: String](f: String => A): Format[A] = stringFormatBase.bimap(f, identity)
  def intFormat[A <: Int](f: Int => A): Format[A] = intFormatBase.bimap(f, identity)

  def writeAs[O, A: Writes](f: O => A) = Writes[O](o => PlayJson.toJson(f(o)))

  def writeWrap[A, B](fieldName: String)(get: A => B)(using writes: Writes[B]): OWrites[A] = OWrites: a =>
    PlayJson.obj(fieldName -> writes.writes(get(a)))

  def stringIsoWriter[O](using iso: Iso[String, O]): Writes[O] = writeAs[O, String](iso.to)
  def intIsoWriter[O](using iso: Iso[Int, O]): Writes[O] = writeAs[O, Int](iso.to)
  def anyIsoWriter[A: Writes, O](using iso: Iso[A, O]): Writes[O] = writeAs[O, A](iso.to)

  def stringIsoReader[O](using iso: Iso[String, O]): Reads[O] = Reads.of[String].map(iso.from)

  def intIsoFormat[O](using iso: Iso[Int, O]): Format[O] =
    Format[O](
      Reads.of[Int].map(iso.from),
      Writes: o =>
        JsNumber(iso to o)
    )

  def stringIsoFormat[O](using iso: Iso[String, O]): Format[O] =
    Format[O](
      Reads.of[String].map(iso.from),
      Writes: o =>
        JsString(iso to o)
    )

  def stringRead[O](from: String => O): Reads[O] = Reads.of[String].map(from)

  def optRead[O](from: String => Option[O]): Reads[O] = Reads
    .of[String]
    .flatMapResult: str =>
      from(str).fold[JsResult[O]](JsError(s"Invalid value: $str"))(JsSuccess(_))
  def optFormat[O](from: String => Option[O], to: O => String): Format[O] = Format[O](
    optRead(from),
    Writes(o => JsString(to(o)))
  )

  def tryRead[O](from: String => scala.util.Try[O]): Reads[O] = Reads
    .of[String]
    .flatMapResult: code =>
      from(code).fold(err => JsError(err.getMessage), JsSuccess(_))
  def tryFormat[O](from: String => scala.util.Try[O], to: O => String): Format[O] = Format[O](
    tryRead(from),
    Writes[O](o => JsString(to(o)))
  )

  given Writes[Instant] = writeAs(_.toMillis)
