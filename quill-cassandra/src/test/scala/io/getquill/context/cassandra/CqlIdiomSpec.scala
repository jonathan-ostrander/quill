package io.getquill.context.cassandra

import io.getquill._

class CqlIdiomSpec extends Spec {

  import mirrorContext._

  "query" - {
    "map" in {
      val q = quote {
        qr1.map(t => t.i)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT i FROM TestEntity"
    }
    "take" in {
      val q = quote {
        qr1.take(1)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity LIMIT 1"
    }
    "sortBy" in {
      val q = quote {
        qr1.sortBy(t => t.i)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity ORDER BY i ASC"
    }
    "all terms" in {
      val q = quote {
        qr1.filter(t => t.i == 1).sortBy(t => t.s).take(1).map(t => t.s)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s FROM TestEntity WHERE i = 1 ORDER BY s ASC LIMIT 1"
    }
    "returning" in {
      val q = quote {
        query[TestEntity].insert(_.l -> 1L).returning(_.i)
      }
      "mirrorContext.run(q).string" mustNot compile
    }
  }

  "distinct" - {
    "simple" in {
      val q = quote {
        qr1.distinct
      }
      "mirrorContext.run(q).string" mustNot compile
    }

    "distinct single" in {
      val q = quote {
        qr1.map(i => i.i).distinct
      }
      mirrorContext.run(q).string mustEqual
        "SELECT DISTINCT i FROM TestEntity"
    }

    "distinct tuple" in {
      val q = quote {
        qr1.map(i => (i.i, i.l)).distinct
      }
      mirrorContext.run(q).string mustEqual
        "SELECT DISTINCT i, l FROM TestEntity"
    }
  }

  "order by criteria" - {
    "asc" in {
      val q = quote {
        qr1.sortBy(t => t.i)(Ord.asc)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity ORDER BY i ASC"
    }
    "desc" in {
      val q = quote {
        qr1.sortBy(t => t.i)(Ord.desc)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity ORDER BY i DESC"
    }
    "ascNullsFirst" in {
      val q = quote {
        qr1.sortBy(t => t.i)(Ord.ascNullsFirst)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity ORDER BY i ASC"
    }
    "descNullsFirst" in {
      val q = quote {
        qr1.sortBy(t => t.i)(Ord.descNullsFirst)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity ORDER BY i DESC"
    }
    "ascNullsLast" in {
      val q = quote {
        qr1.sortBy(t => t.i)(Ord.ascNullsLast)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity ORDER BY i ASC"
    }
    "descNullsLast" in {
      val q = quote {
        qr1.sortBy(t => t.i)(Ord.descNullsLast)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity ORDER BY i DESC"
    }
  }

  "operation" - {
    "binary" in {
      val q = quote {
        qr1.filter(t => t.i == 1)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity WHERE i = 1"
    }
    "unary (not supported)" in {
      val q = quote {
        qr1.filter(t => !(t.i == 1))
      }
      "mirrorContext.run(q)" mustNot compile
    }
    "function apply (not supported)" in {
      val q = quote {
        qr1.filter(t => infix"f".as[Int => Boolean](t.i))
      }
      "mirrorContext.run(q)" mustNot compile
    }
  }

  "aggregation" - {
    "count" in {
      val q = quote {
        qr1.filter(t => t.i == 1).size
      }
      mirrorContext.run(q).string mustEqual
        "SELECT COUNT(1) FROM TestEntity WHERE i = 1"
    }
    "invalid" in {
      val q = quote {
        qr1.map(t => t.i).max
      }
      "mirrorContext.run(q)" mustNot compile
    }
  }

  "binary operation" - {
    "==" in {
      val q = quote {
        qr1.filter(t => t.i == 1)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity WHERE i = 1"
    }
    "&&" in {
      val q = quote {
        qr1.filter(t => t.i == 1 && t.s == "s")
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity WHERE i = 1 AND s = 's'"
    }
    ">" in {
      val q = quote {
        qr1.filter(t => t.i > 1)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity WHERE i > 1"
    }
    ">=" in {
      val q = quote {
        qr1.filter(t => t.i >= 1)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity WHERE i >= 1"
    }
    "<" in {
      val q = quote {
        qr1.filter(t => t.i < 1)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity WHERE i < 1"
    }
    "<=" in {
      val q = quote {
        qr1.filter(t => t.i <= 1)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity WHERE i <= 1"
    }
    "+" in {
      val q = quote {
        qr1.update(t => t.i -> (t.i + 1))
      }
      mirrorContext.run(q).string mustEqual
        "UPDATE TestEntity SET i = i + 1"
    }
    "invalid" in {
      val q = quote {
        qr1.filter(t => t.i * 2 == 4)
      }
      "mirrorContext.run(q)" mustNot compile
    }
  }

  "value" - {
    "string" in {
      val q = quote {
        qr1.filter(t => t.s == "s")
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity WHERE s = 's'"
    }
    "unit" in {
      case class Test(u: Unit)
      val q = quote {
        query[Test].filter(t => t.u == (())).size
      }
      mirrorContext.run(q).string mustEqual
        "SELECT COUNT(1) FROM Test WHERE u = 1"
    }
    "int" in {
      val q = quote {
        qr1.filter(t => t.i == 1)
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity WHERE i = 1"
    }
    "tuple" in {
      val q = quote {
        qr1.map(t => (t.i, t.s))
      }
      mirrorContext.run(q).string mustEqual
        "SELECT i, s FROM TestEntity"
    }
    "collection" in {
      val q = quote {
        qr1.filter(t => liftQuery(List(1, 2)).contains(t.i))
      }
      mirrorContext.run(q).string mustEqual
        "SELECT s, i, l, o FROM TestEntity WHERE i IN (?, ?)"
    }
    "null (not supported)" in {
      val q = quote {
        qr1.filter(t => t.s == null)
      }
      "mirrorContext.run(q)" mustNot compile
    }
  }

  "action" - {
    "insert" in {
      val q = quote {
        qr1.insert(lift(TestEntity("s", 1, 2L, None)))
      }
      mirrorContext.run(q).string mustEqual
        "INSERT INTO TestEntity (s,i,l,o) VALUES (?, ?, ?, ?)"
    }
    "update" - {
      "all" in {
        val q = quote {
          qr1.update(lift(TestEntity("s", 1, 2L, None)))
        }
        mirrorContext.run(q).string mustEqual
          "UPDATE TestEntity SET s = ?, i = ?, l = ?, o = ?"
      }
      "filtered" in {
        val q = quote {
          qr1.filter(t => t.i == 1).update(lift(TestEntity("s", 1, 2L, None)))
        }
        mirrorContext.run(q).string mustEqual
          "UPDATE TestEntity SET s = ?, i = ?, l = ?, o = ? WHERE i = 1"
      }
    }
    "delete" - {
      "filtered" in {
        val q = quote {
          qr1.filter(t => t.i == 1).delete
        }
        mirrorContext.run(q).string mustEqual
          "DELETE FROM TestEntity WHERE i = 1"
      }
      "all" in {
        val q = quote {
          qr1.delete
        }
        mirrorContext.run(q).string mustEqual
          "TRUNCATE TestEntity"
      }
      "column" in {
        val q = quote {
          qr1.map(t => t.i).delete
        }
        mirrorContext.run(q).string mustEqual
          "DELETE i FROM TestEntity"
      }
    }
  }

  "infix" - {
    "query" - {
      "partial" in {
        val q = quote {
          qr1.filter(t => infix"${t.i} = 1".as[Boolean])
        }
        mirrorContext.run(q).string mustEqual
          "SELECT s, i, l, o FROM TestEntity WHERE i = 1"
      }
      "full" in {
        val q = quote {
          infix"SELECT COUNT(1) FROM TestEntity ALLOW FILTERING".as[Query[Int]]
        }
        mirrorContext.run(q).string mustEqual
          "SELECT COUNT(1) FROM TestEntity ALLOW FILTERING"
      }
    }
    "action" - {
      "partial" in {
        val q = quote {
          qr1.filter(t => infix"${t.i} = 1".as[Boolean]).update(lift(TestEntity("s", 1, 2L, None)))
        }
        mirrorContext.run(q).string mustEqual
          "UPDATE TestEntity SET s = ?, i = ?, l = ?, o = ? WHERE i = 1"
      }
      "full" in {
        val q = quote {
          infix"TRUNCATE TestEntity".as[Query[Int]]
        }
        mirrorContext.run(q).string mustEqual
          "TRUNCATE TestEntity"
      }
    }
  }

  "collections operations" - {
    "map.contains" in {
      mirrorContext.run(mapFroz.filter(x => x.id.contains(1))).string mustEqual
        "SELECT id FROM MapFrozen WHERE id CONTAINS KEY 1"
    }
    "set.contains" in {
      mirrorContext.run(setFroz.filter(x => x.id.contains(2))).string mustEqual
        "SELECT id FROM SetFrozen WHERE id CONTAINS 2"
    }
    "list.contains" in {
      mirrorContext.run(listFroz.filter(x => x.id.contains(3))).string mustEqual
        "SELECT id FROM ListFrozen WHERE id CONTAINS 3"
    }
  }
}
