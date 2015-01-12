package tac

import java.io.{FileWriter, BufferedWriter, PrintWriter}
import java.nio.file.{Paths, Files}

import com.typesafe.config.ConfigFactory
import extractor.{ImplicitRelationExtractor, NERFilteredIRE, TaggerLoader}

import scala.io.Source

/**
 * Main method takes the sentence file specified in tac-extractor.conf and
 * outputs the extractions to a file.
 */
object TACDevelopmentRelationExtractor {
  case class InputLine(index: Int, docid: String, sentence: String)

  val config = ConfigFactory.load("tac-extractor.conf")
  val resultDir = config.getString("result-dir")
  val sentenceDir = config.getString("sentence-dir")
  val seqFilename = config.getString("sequence-file")
  // Use the fact that the current sequence number should be 1 greater than
  // the most recently generated sentence file.
  val seq = getSeqNum(seqFilename) - 1

  def main(args: Array[String]) {
    val (inputLines, (out, nullout)) =
      if (args.size > 1) {
        // Run subset of sentences.
        val sentenceMin = args(0).toInt
        val sentenceMax = args(1).toInt
        (subsetInput(sentenceMin, sentenceMax), outputs)
      } else if (args.size > 0) {
        // Restore/Continue run.
        val sequenceNum = args(0).toInt
        (incompleteInput(sequenceNum), appendOutputs(sequenceNum))
      } else {
        val result = (input, outputs)

        // Starting a new file, print the column headers.
        val columnHeaders = Array("Extraction Index", "Sentence Index", "DocId",
          "Entity(NP)", "Relation", "Slotfill(tag)", "Sentence")
        val nullColumnHeaders = Array("Extraction Index", "Sentence Index", "DocId",
          "Sentence")
        val header =
        result._2._1.println(columnHeaders.tail.foldLeft
                             (columnHeaders.head)
                             ((acc, cur) => acc + s"\t$cur"))
        result._2._2.println(nullColumnHeaders.tail.foldLeft
                             (nullColumnHeaders.head)
                             ((acc, cur) => acc + s"\t$cur"))
        result
      }

    val relationExtractor =
      new ImplicitRelationExtractor(TaggerLoader.defaultTagger)

    var i1 = 0
    var i2 = 0
    for (inputLine <- inputLines) {
      val extractions = relationExtractor.extractRelations(inputLine.sentence)
      if (extractions.length != 0) {
        for (extraction <- extractions) {
          // Extraction Index, Sentence Index, Docid, Entity(NP), Relation, Slotfill(tag), Sentence
          out.println(
            s"$i1\t${inputLine.index}\t${inputLine.docid}" +
              s"\t${extraction.np}\t${extraction.relation}\t${extraction.tag.asIndexedString}" +
              s"\t${inputLine.sentence}")
          i1 += 1
        }
      } else {
        // If no extraction, write to null outputs.
        nullout.println(
          s"$i2\t${inputLine.index}\t${inputLine.docid}\t${inputLine.sentence}")
        i2 += 1
      }
    }
    out.close()
  }


  def input = {
    // TODO: put the file name structure in the config so that the change can be
    // made in one place without breaking anything.
    val inputFilename = sentenceDir + seq + "-sentence-file"

    // Check if the file exists.
    if (!Files.exists(Paths.get(inputFilename))) {
      System.out.println(s"Sentence file $inputFilename doesn't exist!  " +
        s"Please run the TACDevelopmentSentenceExtractor first, " +
        s"or check that the sequence number in $seqFilename is correct.\n" +
        s"Exiting...")
      sys.exit(1)
    }

    // TODO: put the delimiter in config
    Source.fromFile(inputFilename).getLines().map(line => {
      val tokens = line.trim.split("\t")
      InputLine(tokens(0).toInt, tokens(1), tokens(2))
    })
  }

  def incompleteInput(sequenceNum: Int) = {
    val inputFilename = sentenceDir + sequenceNum + "-sentence-file"
    val outFilename = resultDir + sequenceNum + "-result-file"


    val lastSentenceProcessed = Source.fromFile(outFilename).getLines().toList.tail
      .foldLeft(-1)((acc, cur) =>
        Math.max(acc, cur.trim.split("\t")(1).toInt))

    // Remove up to the last sentence processed since later lines may be incomplete.
    val fixedFile = StringBuilder.newBuilder
    val lines = Source.fromFile(outFilename).getLines().toList
    fixedFile.append(lines.head + "\n")
    var done = false
    for (line <- lines.tail) {
      if (line.trim.split("\t")(0).toInt < lastSentenceProcessed && !done) {
        fixedFile.append(line)
      } else {
        done = true
      }
    }
    new PrintWriter("outFilename").println(fixedFile.mkString)

    // Get the input file.
    Source.fromFile(inputFilename).getLines()
      .filter(line => line.split("\t")(0).toInt >= lastSentenceProcessed)
      .map(line => {
      val tokens = line.trim.split("\t")
      InputLine(tokens(0).toInt, tokens(1), tokens(2))
    })
  }

  def subsetInput(min: Int, max: Int) = {
    val inputFilename = sentenceDir + seq + "-sentence-file"

    // Get the input file.
    Source.fromFile(inputFilename).getLines()
    .map(line => line.trim.split("\t"))
    .filter(tokens => {
      val index = tokens(0).toInt
      index >= min && index < max
    })
    .map(tokens => InputLine(tokens(0).toInt, tokens(1), tokens(2)))
  }


  def outputs = {
    val outFilename = resultDir + seq + "-result-file"
    val nullOutFilename = outFilename + "-nulls"

    // Check if the file exists.
    if (Files.exists(Paths.get(outFilename))) {
      System.out.println(s"Result file $outFilename already exists!  " +
        s"Please set the number in $seqFilename to a non conflicting " +
        s"sequence number.\nExiting...")
      sys.exit(1)
    }

    // If not, create new file with the given sequence num.
    (new PrintWriter(outFilename), new PrintWriter(nullOutFilename))
  }

  def appendOutputs(sequenceNum: Int) = {
    val outFilename = resultDir + sequenceNum + "-result-file"
    val nullOutFilename = outFilename + "-nulls"
    (new PrintWriter(new BufferedWriter(new FileWriter(outFilename, true))),
      new PrintWriter(new BufferedWriter(new FileWriter(nullOutFilename, true))))
  }

  // This method is a bit of a hack.
  // We know that the sentence num is incremented after the sentence extractor,
  // so we take one less than what is recorded there.
  def getSeqNum(sequenceFile: String) = Source.fromFile(sequenceFile)
    .getLines().next().trim.toInt
}
