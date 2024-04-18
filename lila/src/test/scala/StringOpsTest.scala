package scalalib

class StringTest extends munit.FunSuite:

  import scalalib.StringOps.*

  test("slug be safe >> html"):
    assert(!slug("hello \" world").contains("\""))
    assert(!slug("<<<").contains("<"))
