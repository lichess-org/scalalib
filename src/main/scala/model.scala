package ornicar.scalalib

import ornicar.scalalib.newtypes.*

object model:

  opaque type Max = Int
  object Max extends OpaqueInt[Max]

  opaque type MaxPerPage = Int
  object MaxPerPage extends OpaqueInt[MaxPerPage]

  opaque type MaxPerSecond = Int
  object MaxPerSecond extends OpaqueInt[MaxPerSecond]
