package experimenting

import edu.knowitall.repr.sentence.{Chunked, Chunker, Lemmatized, Lemmatizer, Sentence}
import edu.knowitall.taggers.{NamedGroupType, ParseRule, TaggerCollection}
import edu.knowitall.tool.chunk.OpenNlpChunker
import edu.knowitall.tool.stem.MorphaStemmer
import extractor.ImplicitRelationExtractor

object KnowItAllTaggerExample {
  val pattern = """
Animal := KeywordTagger {
classical sing teacher
cat
kitten
dog
red dog
I have a
have a red dog
St\. Francis Xavier University
St Francis Xavier University
St . Francis Xavier University
Xavior
Francis Xavier
Xavior University
St
St .
St. Francis
St Francis
St\.
St\. Francis
cliff has a
puppy
Madame Violetta
Scientology
}

SpaceForDot := KeywordTagger {
St. Edward's University
}

SpaceBeforeApostrophie := KeywordTagger {
St . Edward  's University
}

SpaceBeforeAndAfterApost := KeywordTagger {
St . Edward ' s University
}

Color := KeywordTagger{
blue
red
yellow
green
}
ColorfulAnimal := PatternTagger {
//namedGroup color will yield a Type object
//that is linked to the ColorfulAnimal Type object
(<color>:<type='Color'>) <type='Animal'>
}
ColorfulAnimalAction := TypePatternTagger{
//TypePatternTagger supports @ syntax to capture
// the entire Type
@ColorfulAnimal <pos='VBD'>
}
                """
  val input = """
I have a red dog, a
Cliff has a yellow puppy.
The yellow puppy ran.
Madame Violetta, a classically singing teacher, instructs a student on how to sing "Be-Bop-a-Lula," which is ultimately delivered in comic clucks and barks interspersed with coloratura flashes.
puppy dog cat kitten
J. Brendan Murphy, a specialist on tectonic cycles at St. Francis Xavier University in Nova Scotia, echoed that view.
J. Brendan Murphy, a specialist on tectonic cycles at St Francis Xavier University in Nova Scotia, echoed that view.
J. Brendan Murphy, a specialist on tectonic cycles at St. Edward's University in Nova Scotia, echoed that view.
"""
  val chunker = new OpenNlpChunker()
  def process(text: String): Sentence with Chunked with Lemmatized = {
    new Sentence(text) with Chunker with Lemmatizer {
      val chunker = KnowItAllTaggerExample.this.chunker
      val lemmatizer = MorphaStemmer
    }
  }
  def main(args: Array[String]){
    val rules = new ParseRule[Sentence with Chunked with Lemmatized].parse(pattern).get
    val t = rules.foldLeft(new TaggerCollection[Sentence with Chunked with Lemmatized]()){ case (ctc, rule) => ctc + rule }
    val lines = input.split("\n").map(f => f.trim()).filter(f => f!= "").toList
    for (line <- lines){
      val types = t.tag(process(line)).toList
      println("Line: " + line)
      for(typ <- types){
        val interval = typ.tokenInterval
        println("TaggerName: " +typ.name + "\tTypeInterval: " + typ.tokenInterval + s"\tIntervalStart: ${interval.start}\tIntervalEnd: ${interval.end}" + "\t TypeText: " + typ.text)
      }
      //filter out the NamedGroupTypes
      for(typ <- types.filter(p => p.isInstanceOf[NamedGroupType])){
        val namedGroupType = typ.asInstanceOf[NamedGroupType]
        if(namedGroupType.groupName == "color"){
          println("COLOR:\t" + namedGroupType.text)
        }
      }
    }
/*
    val extractor = new NounToNounRelationExtractor(t)
    val sentence = "The lawsuits alleged that Scientology churches around the world have " +
      "been bombarded with thousands of harassing phone calls, millions of malicious and obscene e-mails, " +
      "and bomb and death threats by members of Anonymous."
    extractor.extractRelations(sentence)
*/
  }
}