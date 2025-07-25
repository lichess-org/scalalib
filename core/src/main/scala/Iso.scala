package scalalib

import newtypes.SameRuntime

trait Iso[A, B]:
  val from: A => B
  val to: B => A

  def map[BB](mapFrom: B => BB, mapTo: BB => B) = new Iso[A, BB]:
    val from = a => mapFrom(Iso.this.from(a))
    val to = bb => Iso.this.to(mapTo(bb))

object Iso:

  type StringIso[B] = Iso[String, B]
  type IntIso[B] = Iso[Int, B]
  type BooleanIso[B] = Iso[Boolean, B]
  type DoubleIso[B] = Iso[Double, B]

  given [A, B] => (sr: SameRuntime[A, B], rs: SameRuntime[B, A]) => Iso[A, B]:
    val from = sr.apply
    val to = rs.apply

  def apply[A, B](f: A => B, t: B => A): Iso[A, B] = new:
    val from = f
    val to = t

  def string[B](from: String => B, to: B => String): StringIso[B] = apply(from, to)
  def int[B](from: Int => B, to: B => Int): IntIso[B] = apply(from, to)
  def double[B](from: Double => B, to: B => Double): DoubleIso[B] = apply(from, to)
