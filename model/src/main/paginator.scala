package scalalib
package paginator

import cats.syntax.all.*
import alleycats.Zero
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import scalalib.model.MaxPerPage
import scalalib.future.FutureExtension.dmap
import scalalib.extensions.squeeze

final class Paginator[A] private[paginator] (
    val currentPage: Int,
    val maxPerPage: MaxPerPage,
    val currentPageResults: Seq[A],
    val nbResults: Int
):

  def previousPage: Option[Int] = Option.when(currentPage > 1)(currentPage - 1)

  def nextPage: Option[Int] =
    Option.when(currentPage < nbPages && currentPageResults.nonEmpty)(currentPage + 1)

  def nbPages: Int =
    if maxPerPage.value > 0 then (nbResults + maxPerPage.value - 1) / maxPerPage.value
    else 0

  def hasToPaginate: Boolean = nbResults > maxPerPage.value

  def hasPreviousPage: Boolean = previousPage.isDefined

  def hasNextPage: Boolean = nextPage.isDefined

  def withCurrentPageResults[B](newResults: Seq[B]): Paginator[B] =
    new Paginator(
      currentPage = currentPage,
      maxPerPage = maxPerPage,
      currentPageResults = newResults,
      nbResults = nbResults
    )

  def mapResults[B](f: A => B): Paginator[B] =
    withCurrentPageResults(currentPageResults.map(f))

  def mapList[B](f: Seq[A] => Seq[B]): Paginator[B] =
    withCurrentPageResults(f(currentPageResults))

  def mapFutureResults[B](f: A => Future[B])(using ExecutionContext): Future[Paginator[B]] =
    currentPageResults.traverse(f).dmap(withCurrentPageResults)

  def mapFutureList[B](f: Seq[A] => Future[Seq[B]]): Future[Paginator[B]] =
    f(currentPageResults).dmap(withCurrentPageResults)

object Paginator:

  def apply[A](
      adapter: AdapterLike[A],
      currentPage: Int,
      maxPerPage: MaxPerPage
  )(using ExecutionContext): Future[Paginator[A]] =
    validate(adapter, currentPage, maxPerPage).getOrElse(apply(adapter, 1, maxPerPage))

  def empty[A]: Paginator[A] = new Paginator(0, MaxPerPage(0), Nil, 0)

  given [A]: Zero[Paginator[A]] with
    def zero = empty[A]

  given cats.Functor[Paginator] with
    def map[A, B](p: Paginator[A])(f: A => B) = new Paginator(
      currentPage = p.currentPage,
      maxPerPage = p.maxPerPage,
      currentPageResults = p.currentPageResults.map(f),
      nbResults = p.nbResults
    )

  def fromResults[A](
      currentPageResults: Seq[A],
      nbResults: Int,
      currentPage: Int,
      maxPerPage: MaxPerPage
  ): Paginator[A] =
    new Paginator(
      currentPage = currentPage,
      maxPerPage = maxPerPage,
      currentPageResults = currentPageResults,
      nbResults = nbResults
    )

  def validate[A](
      adapter: AdapterLike[A],
      currentPage: Int = 1,
      maxPerPage: MaxPerPage = MaxPerPage(10)
  )(using ExecutionContext): Either[String, Future[Paginator[A]]] =
    if currentPage < 1 then Left("Current page must be greater than zero")
    else if maxPerPage.value <= 0 then Left("Max per page must be greater than zero")
    else
      Right(for
        nbResults <- adapter.nbResults
        safePage = currentPage.squeeze(1, Math.ceil(nbResults.toDouble / maxPerPage.value).toInt)
        // would rather let upstream code know the value they passed in was bad.
        // unfortunately can't do that without completing nbResults, so ig it's on them to check after
        results <- adapter.slice((safePage - 1) * maxPerPage.value, maxPerPage.value)
      yield new Paginator(safePage, maxPerPage, results, nbResults))

abstract class AdapterLike[A](using ExecutionContext):

  /** Returns the total number of results.
    */
  def nbResults: Future[Int]

  /** Returns a slice of the results.
    *
    * @param offset
    *   The number of elements to skip, starting from zero
    * @param length
    *   The maximum number of elements to return
    */
  def slice(offset: Int, length: Int): Future[Seq[A]]

  def map[B](f: A => B): AdapterLike[B] = new:

    def nbResults = AdapterLike.this.nbResults

    def slice(offset: Int, length: Int) =
      AdapterLike.this.slice(offset, length).map { _.map(f) }

  def mapFutureList[B](f: Seq[A] => Future[Seq[B]]): AdapterLike[B] = new:

    def nbResults = AdapterLike.this.nbResults

    def slice(offset: Int, length: Int) =
      AdapterLike.this.slice(offset, length).flatMap(f)
