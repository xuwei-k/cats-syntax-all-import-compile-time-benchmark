val common = Def.settings(
  scalaVersion := "3.6.4",
  crossScalaVersions += "2.13.16",
  libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0",
)

val xs = List[(String, String)](
  "functor" -> "List(2).void",
  "apply" -> "(Option(2), Option(3)).mapN(_ + _)",
  "flatMap" -> "Option(2).mproduct(a => Option(a + 3))",
  "traverse" -> "Option(List(2)).sequence",
  "foldable" -> "List(2).maximumOption",
  "monad" -> "List(2).iterateWhile(_ => false)",
).map {
  case (x, y) => s"import cats.syntax.$x._" -> y
}.zipWithIndex

def sourceCode(all: Boolean): Seq[String] = {
  (1 to 100).map { y =>
    val values = (1 to 100).flatMap { n =>
      if (all) {
        xs.map {
          case ((_, s), i) =>
            s"def x${n}_${i}: Unit = { import cats.syntax.all._ ; $s }"
        }
      } else {
        xs.map {
          case ((a, s), i) =>
            s"def x${n}_${i}: Unit = { $a ; $s }"
        }
      }
    }.mkString("\n")
    s"""|class A$y {
        |$values
        |}""".stripMargin
  }
}

val a1 = project
  .settings(
    common,
    Compile / sourceGenerators += task {
      sourceCode(true).zipWithIndex.map { case (src, n) =>
        val f = (Compile / sourceManaged).value / s"A$n.scala"
        IO.write(f, src)
        f
      }
    }
  )

val a2 = project
  .settings(
    common,
    Compile / sourceGenerators += task {
      sourceCode(false).zipWithIndex.map { case (src, n) =>
        val f = (Compile / sourceManaged).value / s"A$n.scala"
        IO.write(f, src)
        f
      }
    }
  )
