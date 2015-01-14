package extractor

import edu.knowitall.tool.chunk.ChunkedToken
import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.trees.{TypedDependency, Tree}

import scala.collection.JavaConversions._

/**
 * Set of NP extraction functions.
 *
 * Various functions to pull out a noun phrase substring from extraction information.
 */
object NounPhraseFunctions {
  def firstNounPhraseAncestor(
    tree: Tree,
    tdl: List[TypedDependency],
    tag: TagInfo,
    tokenizedSentence: Seq[ChunkedToken],
    sentence: String,
    extractor: ImplicitRelationExtractor): IndexedSubstring = {

    if (tdl.size == 0) {
      return null
    }
    // get the leftmost and rightmost terms.
    val indexedWordTag = tree.`yield`().toList
                         .slice(tag.intervalStart, tag.intervalEnd)
                         .map(x => new IndexedWord(x))
    val (leftmost, rightmost) = getLeftAndRight(indexedWordTag(0),
      indexedWordTag(indexedWordTag.size - 1), tdl)

    // Get root of noun phrase that contains both the dependent and govenor words.
    val npRoot = getNPAncestor(tree, leftmost, rightmost)._1

    // Cut out the appropriate noun phrase from the sentence.
    // Phrase tokens don't have character offsets so get them from the chunked sentence.
    val phraseTokens = phraseTokensFromTree(npRoot)
    val firstChunk = tokenizedSentence(phraseTokens(0).index - 1)
    val lastChunk = tokenizedSentence(
      phraseTokens(phraseTokens.size - 1).index - 1)

    val (startIndex, endIndex, wordIndex) = extendToEnclosePunctuation(
      tree, sentence, firstChunk.offset,
      lastChunk.offset + lastChunk.string.length,
      phraseTokens(0).index - 1, phraseTokens(phraseTokens.size - 1).index,
      extractor)

    new IndexedSubstring(
      sentence.substring(startIndex, endIndex),
      phraseTokens(0).index - 1,
      wordIndex,
      startIndex,
      endIndex,
      sentence
    )
  }

  def expandFromSmallNP(
    tree: Tree,
    tdl: List[TypedDependency],
    tag: TagInfo,
    tokenizedSentence: Seq[ChunkedToken],
    sentence: String,
    extractor: ImplicitRelationExtractor): IndexedSubstring = {

    if (tdl.size == 0) {
      return null
    }
    // get the leftmost and rightmost terms.
    val indexedWordTag = tree.`yield`().toList
                         .slice(tag.intervalStart, tag.intervalEnd)
                         .map(x => new IndexedWord(x))
    // Start with NP that only includes the tag and the first extraction step.
    val firstTd = tdl match {
      case head::tail => head::Nil
      case Nil => Nil
    }
    val (smallLeftmost, smallRightmost) = getLeftAndRight(indexedWordTag(0),
      indexedWordTag(indexedWordTag.size - 1), firstTd)
    val npRoot = getNPAncestor(tree, smallLeftmost, smallRightmost)._1

    // left and right without np root.
    val (left, right) = getLeftAndRight(indexedWordTag(0),
      indexedWordTag(indexedWordTag.size - 1), tdl)

    // Cut out the appropriate noun phrase from the sentence.
    // Phrase tokens don't have character offsets so get them from the chunked sentence.
    val phraseTokens = phraseTokensFromTree(npRoot)
    val firstChunkIndex = Math.min(phraseTokens(0).index, left.index) - 1
    val lastChunkIndex = Math.max(phraseTokens(phraseTokens.size - 1).index, right.index()) - 1
    val firstChunk = tokenizedSentence(firstChunkIndex)
    val lastChunk = tokenizedSentence(lastChunkIndex)

    val (startIndex, endIndex, wordIndex) = extendToEnclosePunctuation(
      tree, sentence, firstChunk.offset,
      lastChunk.offset + lastChunk.string.length,
      firstChunkIndex, lastChunkIndex + 1,
      extractor)

    new IndexedSubstring(
      sentence.substring(startIndex, endIndex),
      firstChunkIndex,
      wordIndex,
      startIndex,
      endIndex,
      sentence
    )
  }

  def ignoreNP(
    tree: Tree,
    tdl: List[TypedDependency],
    tag: TagInfo,
    tokenizedSentence: Seq[ChunkedToken],
    sentence: String,
    extractor: ImplicitRelationExtractor): IndexedSubstring = {

    if (tdl.size == 0) {
      return null
    }
    // get the leftmost and rightmost terms.
    val indexedWordTag = tree.`yield`().toList
                         .slice(tag.intervalStart, tag.intervalEnd)
                         .map(x => new IndexedWord(x))

    // left and right without np root.
    val (left, right) = getLeftAndRight(indexedWordTag(0),
      indexedWordTag(indexedWordTag.size - 1), tdl)

    val firstChunkIndex = left.index - 1
    val lastChunkIndex = right.index() - 1
    val firstChunk = tokenizedSentence(firstChunkIndex)
    val lastChunk = tokenizedSentence(lastChunkIndex)

    val (startIndex, endIndex, wordIndex) = extendToEnclosePunctuation(
      tree, sentence, firstChunk.offset,
      lastChunk.offset + lastChunk.string.length,
      firstChunkIndex, lastChunkIndex + 1,
      extractor)

    new IndexedSubstring(
      sentence.substring(startIndex, endIndex),
      firstChunkIndex,
      wordIndex,
      startIndex,
      endIndex,
      sentence
    )
  }

  // Assume that the full tag noun phrase is a child of the noun phrase
  // containing the dep and gov.
  // If incorrect - TO-DO: expand dep to the full phrase.
  def getNPAncestor(tree: Tree, dep: IndexedWord,
    gov: IndexedWord): (Tree, Boolean, Boolean) = {
    if (tree.isLeaf) {
      val label = tree.label().toString
      if (label.equalsIgnoreCase(s"${dep.value()}-${dep.index()}")) {
        (tree, true, false)
      } else if (label.equalsIgnoreCase(s"${gov.value()}-${gov.index()}")) {
        (tree, false, true)
      } else {
        (tree, false, false)
      }
    } else {
      val childResults = tree.children()
                         .map(t => getNPAncestor(t, dep, gov))
      val correctChildren = childResults.filter(
        t => t._1.label().toString == "NP" && t._2 && t._3)
      if (correctChildren.size > 0) {
        return correctChildren(0)
      }
      val curResult = childResults.foldLeft((false, false))(
        (acc, cur) => (acc._1 || cur._2, acc._2 || cur._3))
      (tree, curResult._1, curResult._2)
    }
  }

  // Gets the indexed words that represent the left most and rightmost indices
  def getLeftAndRight(
    startLeft: IndexedWord,
    startRight: IndexedWord,
    tdl: List[TypedDependency]): (IndexedWord, IndexedWord) = {

    tdl.foldLeft(startLeft, startRight)((acc, cur) =>
      acc match {
        case (null, null) =>
          if (cur.dep.index() < cur.gov.index()) {
            (cur.dep, cur.gov)
          } else {
            (cur.gov, cur.dep)
          }
        case _ =>
          val (accleft, accright, curd, curg) = (acc._1, acc._2, cur.dep, cur.gov)
          List(accleft, accright, curd, curg)
          .foldLeft((accleft, accright))((acc, cur) => {
            val left = if (acc._1.index() < cur.index()) {
              acc._1
            } else {
              cur
            }
            val right = if (acc._2.index() > cur.index()) {
              acc._2
            } else {
              cur
            }
            (left, right)
          })
      })
  }

  def phraseTokensFromTree(tree: Tree): List[IndexedString] = {
    val labels = tree.`yield`().map(l => l.toString).toList
    // Only labels of leaf nodes will have a dash.
    // All others will be a name for a phrase type.
    labels.filter(l => l.contains("-"))
    .map(l => {
      val (string, negIndex) = l.splitAt(l.lastIndexOf('-'))
      new IndexedString(string, 0 - negIndex.toInt)
    })
  }

  /**
   * Determines the new offsets after extending the string extraction window
   * to close any open paired punctuation.  The specified punctuation to do this
   * is specified in extractor.conf in the field "enclosing-punctuation".
   *
   * @param tree Tree parse of the entire sentence.
   * @param sentence Entire source sentence string.
   * @param start Current starting character index of the extraction window. (inclusive)
   * @param end Current ending character index of the extraction window. (exclusive)
   * @param firstWordIndex Current starting parse index of the extraction window.
   * @param lastWordIndex Current ending parse index of the extraction window.
   * @param extractor ImplicitRelationExtractor which contains enclosing
   *                  punctuation and cached sentence tokenization info.
   * @return A triple of ints.  Th first and second are the character offsets
   *         of the sentence for extracting the phrase after extending to
   *         close all open punctuation.  The third is the parse index, which
   *         needs to be added to NounToNounRelations.
   */
  def extendToEnclosePunctuation(tree: Tree, sentence: String, start: Int, end: Int,
    firstWordIndex: Int, lastWordIndex: Int,
    extractor: ImplicitRelationExtractor): (Int, Int, Int) = {
    val tokens = phraseTokensFromTree(tree)
    val chunks = extractor.getTokens(sentence) // token chunks
    extractor.getEnclosingPunctuation.foldLeft(start, end, lastWordIndex)((punctAcc, punctCur) => {
      val lastOpen = tokens.lastIndexWhere(t => t.string.contains(punctCur.open), lastWordIndex)
      val lastClose = tokens.lastIndexWhere(t => t.string.contains(punctCur.close), lastWordIndex)
      lastOpen > lastClose && lastOpen > firstWordIndex match {
        case true =>
          val newLastWordIndex = tokens.indexWhere(t => t.string.contains(punctCur.close), lastWordIndex + 1)
          if (newLastWordIndex <= punctAcc._3) {
            return punctAcc
          }
          val newLastCharIndex = chunks(newLastWordIndex).offset +
            chunks(newLastWordIndex).string.length
          (punctAcc._1, newLastCharIndex, newLastWordIndex + 1)
        case false => punctAcc
      }
    })
  }
}
