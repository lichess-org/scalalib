package ornicar.scalalib

/* Like cats.Show,
 * except it's actually fine to render it to an end-user.
 * cats.Show has a default instance for Option[A] that renders
 * as `None` or `Some(value)`, which is not very user-friendly.
 */
@FunctionalInterface
trait Render[A]:
  def apply(a: A): String
  extension (a: A) def render: String = apply(a)

object Render:
  def apply[A](f: A => String): Render[A] = f(_)

  /* If we have Render, we have Show;
   * but not the other way around. */
  given [A](using Render[A]): cats.Show[A] = cats.Show.show(_.render)
