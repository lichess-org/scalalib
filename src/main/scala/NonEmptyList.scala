package ornicar.scalalib

import scalaz.NonEmptyList

trait OrnicarNonEmptyList {

  implicit final class ornicarNonEmptyList[A](neList: NonEmptyList[A]) {

    def min[B >: A](implicit cmp: Ordering[B]): A =
      neList.tail.foldLeft(neList.head) { (a, b) ⇒ if (cmp.lteq(a, b)) a else b }

    def max[B >: A](implicit cmp: Ordering[B]): A =
      neList.tail.foldLeft(neList.head) { (a, b) ⇒ if (cmp.gteq(a, b)) a else b }

    def minBy[B](f: A ⇒ B)(implicit cmp: Ordering[B]): A =
      neList.tail.foldLeft(neList.head) { (a, b) ⇒ if (cmp.lteq(f(a), f(b))) a else b }

    def maxBy[B](f: A ⇒ B)(implicit cmp: Ordering[B]): A =
      neList.tail.foldLeft(neList.head) { (a, b) ⇒ if (cmp.gteq(f(a), f(b))) a else b }

    def foldl1Nel[B >: A](f: (B, A) ⇒ B): B = ((neList.head: B) /: neList.tail) { f }
  }
}
object OrnicarNonEmptyList extends OrnicarNonEmptyList
