package scalalib

import scalalib.newtypes.*

object model:

  opaque type Max = Int
  object Max extends RelaxedOpaqueInt[Max]

  opaque type MaxPerPage = Int
  object MaxPerPage extends RelaxedOpaqueInt[MaxPerPage]

  opaque type MaxPerSecond = Int
  object MaxPerSecond extends RelaxedOpaqueInt[MaxPerSecond]

  opaque type Days = Int
  object Days extends RelaxedOpaqueInt[Days]

  opaque type Seconds = Int
  object Seconds extends RelaxedOpaqueInt[Seconds]
