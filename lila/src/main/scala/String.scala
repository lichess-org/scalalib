package scalalib

import java.text.Normalizer

object String:

  object slug:
    private val slugR              = """[^\w-]""".r
    private val slugMultiDashRegex = """-{2,}""".r

    def apply(input: String) =
      val nowhitespace = input.trim.replace(' ', '-')
      val singleDashes = slugMultiDashRegex.replaceAllIn(nowhitespace, "-")
      val normalized   = Normalizer.normalize(singleDashes, Normalizer.Form.NFD)
      val slug         = slugR.replaceAllIn(normalized, "")
      slug.toLowerCase

  private val onelineR                           = """\s+""".r
  def shorten(text: String, length: Int): String = shorten(text, length, "…")
  def shorten(text: String, length: Int, sep: String): String =
    val oneline = onelineR.replaceAllIn(text, " ")
    if oneline.lengthIs > length + sep.length then oneline.take(length) ++ sep
    else oneline

  def urlencode(str: String): String = java.net.URLEncoder.encode(str, "UTF-8")

  def addQueryParam(url: String, key: String, value: String): String = addQueryParams(url, Map(key -> value))
  def addQueryParams(url: String, params: Map[String, String]): String =
    if params.isEmpty then url
    else
      val queryString = params // we could encode the key, and we should, but is it really necessary?
        .map { (key, value) => s"$key=${urlencode(value)}" }
        .mkString("&")
      s"$url${if url.contains("?") then "&" else "?"}$queryString"
