package experimenting

import extractor.{ConstrainedImplIE, WordNetFilteredIRE, ImplicitRelationExtractor, TaggerLoader, ImplicitRelationExtractorNoLists}
import testers.TaggerTester

/**
 * Created by Gene on 11/25/2014.
 */
object ExtractorTagTester_NH {
  def main(args: Array[String]) {

    val tagger = TaggerLoader.defaultTagger
    val extractor = new ImplicitRelationExtractor(tagger)
    val wordnetextractor = new WordNetFilteredIRE(tagger)
    val constrained = new ConstrainedImplIE(tagger)
    val extractorNoLists = new ImplicitRelationExtractorNoLists(tagger)
    
    //val sentence = "Last year, Gen. Nikolai Solovtsov, head of Russia's missile forces, warned that Moscow could target future bases in Poland and the Czech Republic with Russian missiles."
    //val sentence = "But the future may hold a different twist -- and has many locals fearful of Russian retaliation."
    //val sentence = "We often think of domestic workers as people travelling out of Indonesia to other countries, but there is a large population of Indonesian domestic workers in their own country, who are not recognised by law, Amnesty International Malaysia executive director Josef Roy Benedict told reporters in Kuala"
    //val sentence = "Two US soldiers killed in Iraq."
    //val sentence = "New York-based Zirh was founded in 1995 and makes products using natural oils and extracts."
    //val sentence = "U.S. citizen under investigation in Cuba for spying </HEADLINE> <DATELINE> HAVANA, June 16 (Xinhua) </DATELINE> <TEXT> <P> U.S. contractor Alan Gross, who was arrested for alleged espionage activities in December, remains under investigation, said Cuban Foreign Minister Bruno Rodriguez Wednesday."  
    //val sentence = "Gross, a native of Potomac, Maryland, was arrested Dec. 3, 2009."
    //val sentence = "Police officer Barack Obama, resident of Portland, called Putin on the phone."  
    val sentence = "Andrew E. Lange was born in Urbana, Ill., on July 23, 1957, the oldest son of Joan Lange, a school librarian, and Albert Lange, an architect, and grew up in Easton, Conn."
    //val sentence = "The leaders of Cuba's two main Jewish groups say they have not worked with Alan Gross, who is Jewish."
    //val sentence = "Analyst and author Ahmed Rashid said Zardari had been caught off-guard by the opposition to the aid package under which Pakistan's government will receivemore than the one billion dollars that the military gets."
    //val sentence = "He was Esteban Neira, not Estaban Neiva."
    //val sentence = "President Barack Obama, CEO of Microsoft Corporation, spoke about his wife Chicago native, Michelle Obama." 
    //val sentence = "Besides the advertising agency Campfire, whose founders produced 'The Blair Witch Project,' the Los Angeles agency ... And Company also worked on the campaign, particularly on the print advertising mentioned in the column."
    //val sentence = "The dramatic changes in the nation's economy have made all of us who are responsible for WNO's welfare reluctantly come to the difficult decision to postpone the Ring Cycle until the financial climate becomes more positive, general director Placido Domingo said."
    //val sentence = "Gelb hopes to find another opera for the Tony Award-winning Chenoweth, an opera-trained soprano, but nothing has been finalized."
    //val sentence = "On Tuesday President Jack Sprat spoke at the White House."
    //val sentence = "CEO Jack Sprat, also a journalist, arrived on time." 
    //val sentence = "Jack Sprat, journalist, CEO, and dog walker, arrived early."
    //val sentence = "Most want nothing to do with religious feuding and blame politicians for the ungodly messes going on in Iraq, Palestine, Afghanistan and elsewhere."
    //val sentence = "HANOI -- Vietnam's stock market index, VN-Index, closed at 1, 059.79 points on Thursday, up 18.8 points, or 1.81 percent against Wednesday."
    //Wilson, Phil Mickelson, two-time Singapore winner Adam Scott, Ernie Els, Darren Clarke, Ian Poulter, K.J. Choi-43
    //Wilson, Phil Mickelson, two-time Singapore winner Adam Scott, Ernie Els, Darren Clarke, Ian Poulter, K.J. Choi and Thai star Thongchai Jaidee-48
    //val sentence = "Notes: Ireland's Padraig Harrington, the British Open and U.S. PGA Championship winner, tops the field along with Wilson, Phil Mickelson, two-time Singapore winner Adam Scott, Ernie Els, Darren Clarke, Ian Poulter, K.J. Choi and Thai star Thongchai Jaidee."
    //val sentence = "Notes: Ireland's Padraig Harrington, the British Open and U.S. PGA Championship winner, tops the field along with Wilson, two-time Singapore winner Adam Scott, and Thai star Thongchai Jaidee."
    //val sentence = "Mercosur was founded in 1991 by Brazil, Argentina, Uruguay and Paraguay."
    //head index = 6, tag index = 1, both for CAIRO
    //val sentence = "CAIRO, Egypt 2007-09-07 01:21:47 UTC"
    //val sentence = "CAIRO, Nov. 13 (Xinhua)"  
    //val sentence =   
    //val sentence = "Kentucky Fried Chicken announced a new CEO, Jake Sanders."
    //val sentence = "Chad claims rebel infiltration from Sudan."
    //val sentence = "The old signatories of the 1985 Schengen Agreement included Austria, Belgium, Denmark, Finland, France, Germany, Greece, Iceland, Italy, Luxembourg, the Netherlands, Norway, Portugal, Spain and Sweden."

      val result = extractor.extractRelations(sentence)    
      //val result = extractorNoLists.extractRelations(sentence)  

    println("Result Size: " + result.size)
    result.foreach(r => {
      println(r.np)
      println("np bo: " + r.np.beginOffset)
      println("np eo: " + r.np.endOffset)
      println("np bwi: " + r.np.beginWordIndex)
      println("np ewi: " + r.np.endWordIndex)      
      println(r.relation)
      println("tag: " + r.tag)
      println("tag index: " + r.tag.index)
      println("tag is: " + r.tag.intervalStart)
      println("tag ie: " + r.tag.intervalEnd)
      println("head: " + r.head)
      println("head index: " + r.head.index)      
      println("ner size: " + r.ners.size)
      println("sentence:")
      println(r.sentence)
      println("ner tags: " )
      r.ners.foreach(ner => {println(ner.ner); println(ner.entityString); println(ner.beginIndex); println(ner.endIndex)})
      //extractor.getTags(r.sentence).foreach(t=>println(t))
      println("rt size: " + r.relationTrace.size)
      println("ert size: " + r.explicitRelationTraces.size)
      //println(r.relationTrace.foreach(rt => println(rt.toString)))
      println(r.explicitRelationTraces.foreach(rt => { 
        println("RT")
        rt.foreach(rt => {
          val x = rt.toString
          println(rt.toString) 
          //println(x.contains("conj_and"))        
        })
      }))
    })
    
    //val sentence = "John Arterberry, executive deputy chief of the fraud section in the Justice Department, said federal prosecutors and the FBI had made progress on mortgage fraud."
    //val sentence = "Staging a surprise detour from his trip to Sydney, Bush arrived at the Al-Asad air base in Anbar along with Secretary of State Condoleezza Rice and national security adviser Stephen Hadley."

    //val sentence = "AMSTERDAM, Netherlands 2008-01-26 21:41:45 UTC"
    //val constrainedresult = constrained.extractRelations(sentence)
    //val wordnetresult = wordnetextractor.extractRelations(sentence)
    //val result = extractor.extractRelations(sentence)

    //println(constrainedresult)
    //println(wordnetresult)
    //println(result)

    //val sentence2 = "In a recent speech to the Jewish Coalition, he went further, accusing the Democrats of putting too much stock in diplomacy."
    //val constrainedresult2 = constrained.extractRelations(sentence2)
    //val wordnetresult2 = wordnetextractor.extractRelations(sentence2)
    //val result2 = extractor.extractRelations(sentence2)

    //println(constrainedresult2)
    //println(wordnetresult2)
    //println(result2)

    //val sentence3 = "The client division sales \"are surprisingly ahead of where we thought they would come in,\" said Sid Parakh, an analyst at McAdams Wright Ragen."
    //val constrainedresult3 = constrained.extractRelations(sentence3)
    //val wordnetresult3 = wordnetextractor.extractRelations(sentence3)
    //val result3 = extractor.extractRelations(sentence3)

    //println(constrainedresult3)
    //println(wordnetresult3)
    //println(result3)
    
    /*
        for (typ <- tags) {
          println(s"Tag: ${typ.name}\tText: ${typ.text}")
        }

        println()

        println("TagMap")
        val tagMap = extractor.createTagMap(tags)
        for ((k, v) <- tagMap) {
          println(s"key: $k\ttag: ${v.tag}\ttext: ${v.text}")
        }
    */
  }
}
