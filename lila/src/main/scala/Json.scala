package scalalib

import play.api.libs.json.{ Json as PlayJson, * }
import scalalib.json.Json.*
import scalalib.model.MaxPerPage
import scalalib.paginator.Paginator

object Json:
  export scalalib.json.Json.{ *, given }
  export scalalib.json.extensions.{ *, given }

  given Writes[MaxPerPage] with
    def writes(m: MaxPerPage) = JsNumber(m.value)

  given paginatorWrite[A: Writes]: OWrites[Paginator[A]] = OWrites[Paginator[A]]: p =>
    PlayJson.obj(
      "currentPage"        -> p.currentPage,
      "maxPerPage"         -> p.maxPerPage,
      "currentPageResults" -> p.currentPageResults,
      "nbResults"          -> p.nbResults,
      "previousPage"       -> p.previousPage,
      "nextPage"           -> p.nextPage,
      "nbPages"            -> p.nbPages
    )
