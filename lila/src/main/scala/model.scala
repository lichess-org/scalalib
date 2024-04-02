package scalalib

import scalalib.newtypes.*

object model:

  opaque type Max = Int
  object Max extends OpaqueInt[Max]

  opaque type MaxPerPage = Int
  object MaxPerPage extends OpaqueInt[MaxPerPage]:
    import play.api.libs.json.*
    given Writes[MaxPerPage] with
      def writes(m: MaxPerPage) = JsNumber(m.value)

  opaque type MaxPerSecond = Int
  object MaxPerSecond extends OpaqueInt[MaxPerSecond]
