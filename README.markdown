Misc scala utilities shared by the lichess.org scala projects.

- `core` provides scala library extensions, opaque type improvements, and low-level utilities.
- `model` has common types and relies on `core` opaque types.
- `playJson` extends JsValue and offers handlers for common types.
- `lila` contains higher-level utilities and relies on all of the above.

Projects using scalalib include:

- [scalachess](https://github.com/lichess-org/scalachess)
- [lila](https://github.com/lichess-org/lila)
- [lila-ws](https://github.com/lichess-org/lila-ws)
- [lila-search](https://github.com/lichess-org/lila-search)
- [lichess-db](https://github.com/lichess-org/database)

[![](https://jitpack.io/v/lichess-org/scalalib.svg)](https://jitpack.io/#lichess-org/scalalib)
