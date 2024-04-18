package scalalib

class StringTest extends munit.FunSuite:

  import scalalib.String.*

  test("slug be safe >> html"):
    assert(!String.slug("hello \" world").contains("\""))
    assert(!String.slug("<<<").contains("<"))
