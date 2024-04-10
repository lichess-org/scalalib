package scalalib

import scalalib.newtypes.*

object model:

  opaque type Max = Int
  object Max extends OpaqueInt[Max]

  opaque type MaxPerPage = Int
  object MaxPerPage extends OpaqueInt[MaxPerPage]

  opaque type MaxPerSecond = Int
  object MaxPerSecond extends OpaqueInt[MaxPerSecond]

  opaque type Days = Int
  object Days extends OpaqueInt[Days]

  opaque type Seconds = Int
  object Seconds extends OpaqueInt[Seconds]
