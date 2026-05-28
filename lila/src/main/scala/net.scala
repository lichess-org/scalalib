package scalalib
package net

import scalalib.newtypes.{ YesNo, OpaqueString }

opaque type IpAddressStr = String
object IpAddressStr extends OpaqueString[IpAddressStr]

opaque type Domain = String
object Domain extends OpaqueString[Domain]:
  extension (a: Domain) def lower = Domain.Lower(a.value.toLowerCase)

  // https://stackoverflow.com/a/26987741/1744715
  private val regex = """(?i)^_?[a-z0-9-]{1,63}+(?:\._?[a-z0-9-]{1,63}+)*$""".r
  def isValid(str: String) = str.contains('.') && regex.matches(str)
  def from(str: String): Option[Domain] = Option.when(isValid(str))(Domain(str))
  def unsafe(str: String): Domain = Domain(str)

  opaque type Lower = String
  object Lower extends OpaqueString[Lower]

opaque type Bearer = String
object Bearer extends OpaqueString[Bearer]:
  def random() = Bearer(s"lio_${SecureRandom.nextString(32)}")
  def randomPersonal() = Bearer(s"lip_${SecureRandom.nextString(20)}")

opaque type UserAgent = String
object UserAgent extends OpaqueString[UserAgent]:
  val zero: UserAgent = ""

opaque type Crawler = Boolean
object Crawler extends YesNo[Crawler]
