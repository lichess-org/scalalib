package scalalib

class StringTest extends munit.FunSuite:

  import scalalib.StringOps.*

  test("slug be safe >> html"):
    assert(!slug("hello \" world").contains("\""))
    assert(!slug("<<<").contains("<"))

  val i18nValidStrings = List(
    """éâòöÌÒÒçÇ""",
    """صارف اپنا نام تبدیل کریں۔ یہ صرف ایک دفعہ ہو سکتا ہے اور صرف انگریزی حروف چھوٹے یا بڑے کرنے کی اجازت ہے۔.""",
    """ユーザー名を変更します。これは一回限りで、行なえるのは大文字・小文字の変更だけです。""",
    """ਤੁਹਾਡੇ ਵਿਰੋਧੀ ਨੇ ਖੇਡ ਨੂੰ ਛੱਡ ਦਿੱਤਾ. ਤੁਸੀਂ ਜਿੱਤ ਦਾ ਦਾਅਵਾ ਕਰ ਸਕਦੇ ਹੋ, ਖੇਡ ਨੂੰ ਡਰਾਅ ਕਹਿ ਸਕਦੇ ਹੋ, ਜਾਂ ਇੰਤਜ਼ਾਰ ਕਰ ਸਕਦੇ ਹੋ.""",
    """మీ ప్రత్యర్థి బహుశా ఆట విడిచి వెళ్లిపోయారేమో. మీరు కాసేపు ఆగి చూడవచ్చు, లేదా గెలుపోటములు సమానంగా పంచుకోవచ్చు, లేదా విజయం ప్రకటించుకోవచ్చు.""",
    """ผู้เล่นที่เป็นคอมพิวเตอร์หรือใช้คอมพิวเตอร์ช่วย จะไม่ได้รับอนุญาตให้เล่น  โปรดอย่าใช้การช่วยเหลือจากตัวช่วยเล่นหมากรุก, ฐานข้อมูล หรือบุคคลอื่น ในขณะเล่น""",
    """သင့်ရဲ့ပြိုင်ဘက် ဂိမ်းမှထွက်ခွာသွားပါပြီ. လက်ရှိပွဲကို အနိုင်ယူမည်လား သရေကျပေးမည်လား သို့မဟုတ် စောင့်ဆိုင််းဦးမလား.""",
    """יריבך עזב את המשחק. באפשרותך לכפות פרישה, להכריז על תיקו או להמתין לו."""
  )

  val rms = removeMultibyteSymbols
  test("remove multibyte garbage"):
    assertEquals(rms("""🕸Trampas en Aperturas🕸: INTRO👋"""), "Trampas en Aperturas: INTRO")
    assertEquals(
      rms("""🚌🚎🚐🚑🚒🚓🚕🚗🚙🚚🚛🚜🚲🛴🛵🛺🦼🦽 with new and better !pizzes on lichess.org"""),
      " with new and better !pizzes on lichess.org"
    )
    assertEquals(rms("🥹"), "")
    assertEquals(rms("🥹🥹🥹 xxx 🥹"), " xxx ")
    assertEquals(rms("🌈"), "")
    assertEquals(rms("🫟"), "")
    assertEquals(rms("𓏼"), "")
    assertEquals(rms("𝅙"), "")
    assertEquals(rms("𝅳"), "")
    assertEquals(rms("𝅴"), "")
    assertEquals(rms("𝅵"), "")
    assertEquals(rms("𝅶"), "")
    assertEquals(rms("𝅸𝅸𝅸𝅸𝅸𝅹𝅺𝅸"), "")
    assertEquals(rms("󠀁"), "")
    assertEquals(rms("󠀠"), "")
    assertEquals(rms("󠀰"), "")
  test("preserve languages"):
    i18nValidStrings.foreach: txt =>
      assertEquals(rms(txt), txt)
  test("preserve half point"):
    assertEquals(rms("½"), "½")

  test("remove garbage chars"):
    val rgc = removeGarbageChars
    assertEquals(rgc("""ℱ۩۞۩꧁꧂"""), "")
    assertEquals(rgc("""ᴀᴛᴏᴍɪᴄ"""), "")
    assertEquals(rgc("""af éâòöÌÒÒçÇℱ۩۞۩꧁꧂"  صار"""), """af éâòöÌÒÒçÇ"  صار""")
    assertEquals(rgc("卐卐卐"), "")
    i18nValidStrings.foreach: txt =>
      assertEquals(rgc(txt), txt)

  test("normalize keep º and ª"):
    assertEquals(normalize("keep normal text"), "keep normal text")
    assertEquals(normalize("keep º and ª"), "keep º and ª")
  test("normalize preserve half point"):
    assertEquals(normalize("½"), "½")

  test("soft cleanup"):
    val sc = softCleanUp
    assertEquals(sc("卐卐卐"), "")
    // normal space
    assertEquals(sc(" "), "")
    assertEquals(sc("    "), "")
    // braille space
    assertEquals(sc("⠀"), "")
    assertEquals(sc("⠀⠀⠀"), "")
    assertEquals(sc("⠀uh⠀⠀"), "uh")
    // https://blankcopypaste.com
    // assertEquals(sc(" ︎ ︎ ︎ ︎ ︎ ︎ ︎ ︎ ︎ ︎"), "")
    // utf32 invisible
    assertEquals(sc("𝅙"), "")
    assertEquals(sc("𝅳"), "")
    assertEquals(sc("𝅴"), "")
    assertEquals(sc("𝅵"), "")
    assertEquals(sc("𝅶"), "")
    assertEquals(sc("𝅸𝅸𝅸𝅸𝅸𝅹𝅺𝅸"), "")
    assertEquals(sc("󠀁"), "")
    assertEquals(sc("󠀠"), "")
    assertEquals(sc("󠀰"), "")
    assertEquals(sc("​"), "​") // zero-width space, keep in soft cleanup
    assertEquals(
      sc("""multi
line"""),
      "multi\nline"
    )

  test("full cleanup"):
    assertEquals(fullCleanUp("​"), "")

  test("addQueryParam"):
    assertEquals(
      addQueryParam("https://example.com/path", "key", "value"),
      "https://example.com/path?key=value"
    )
    assertEquals(
      addQueryParam("https://example.com/path?key1", "key2", "value"),
      "https://example.com/path?key1&key2=value"
    )

  test("addQueryParams"):
    assertEquals(
      addQueryParams("https://example.com/path", Map.empty),
      "https://example.com/path"
    )
    assertEquals(
      addQueryParams("https://example.com/path", Map("key1" -> "value1", "key2" -> "value2")),
      "https://example.com/path?key1=value1&key2=value2"
    )
    assertEquals(
      addQueryParams("https://example.com/path?key1=value1", Map("key2" -> "value2")),
      "https://example.com/path?key1=value1&key2=value2"
    )
    assertEquals(
      addQueryParams("https://example.com/path?key1=value1&key2=value2", Map("key3" -> "value3")),
      "https://example.com/path?key1=value1&key2=value2&key3=value3"
    )
    assertEquals(
      addQueryParams(
        "https://example.com/path?encoded1=test%40example.com",
        Map("encoded2" -> "multiple words")
      ),
      "https://example.com/path?encoded1=test%40example.com&encoded2=multiple+words"
    )

  test("addQueryParams - replace existing param of same key"):
    assertEquals(
      addQueryParams("https://example.com/path?key=value1", Map("key" -> "value2")),
      "https://example.com/path?key=value2"
    )
    assertEquals(
      addQueryParams("https://example.com/path?key", Map("key" -> "value2")),
      "https://example.com/path?key=value2"
    )

  test("addQueryParams - parse malformed params"):
    assertEquals(
      addQueryParams("https://example.com/path?&&&", Map("key1" -> "value1")),
      "https://example.com/path?key1=value1"
    )
    assertEquals(
      addQueryParams("https://example.com/path?=value1&key2=&=&&key3=value3", Map("key1" -> "value1")),
      "https://example.com/path?key2=&key3=value3&key1=value1"
    )
