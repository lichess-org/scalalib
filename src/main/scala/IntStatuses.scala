package ornicar.scalalib

trait IntStatuses[A <: IntStatus] {

  val values: Set[A]

  def find(id: Int): Option[A] = allById get id

  def find(name: String): Option[A] = allByName get name

  def contains(name: String): Boolean = allByName contains name

  def contains(id: Int): Boolean = allById contains id

  lazy val allById: Map[Int, A] = values map { v => (v.id, v) } toMap
  lazy val allByName: Map[String, A] = values map { v => (v.name, v) } toMap

  override def toString = values mkString ", "
}

trait IntStatus {

  val id: Int

  val name: String

  val toInt = id

  override def toString = name
}
