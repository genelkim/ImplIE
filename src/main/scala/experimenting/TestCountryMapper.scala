package experimenting

import tac.SingularCountryMapper

/**
 * Created by Gene on 5/9/2015.
 */
object TestCountryMapper {
  def main(args: Array[String]) {
    val mapper = new SingularCountryMapper()

    val queries = List(
      "Chinese",
      "U.S.",
      "American",
      "Kyrgis",
      "Kyrgyz",
      "Danish",
      "Denmark",
      "UAE",
      "Pakistani",
      "Canadian",
      "Canada",
      "pakistani",
      "china",
      "China",
      "chinese",
      "american",
      "u.s."
    )

    val resultsCL = queries.map(query => mapper.getCountryName(query))
    val resultsC = queries.map(query => mapper.getCountryName(query, useList=false))
    val resultsL = queries.map(query => mapper.getCountryName(query, lowercase = true))
    val results = queries.map(query => mapper.getCountryName(query, lowercase = true, useList = false))

    println("query\tcasesensitive/withlist\tcasesensitive\twithlist\tnone")
    for (i <- 0 until queries.length) {
      println(s"query ${queries(i)}\t${resultsCL(i)}\t${resultsC(i)}\t${resultsL(i)}\t${results(i)}")
    }
    
    val x = mapper.getCountryName("Dog")

    println("x: " + x)

    if(x == null) println("x equals null")

    x match {      
      case null => println("matches null")
      case _ => println("!matches null")
    }
    
  }
}
