package almond.echo

import almond.interpreter.{Completion, ExecuteResult, Interpreter}
import almond.interpreter.api.{DisplayData, OutputHandler}
import almond.interpreter.input.InputManager
import almond.protocol.KernelInfo

final class EchoInterpreter extends Interpreter {

  def kernelInfo(): KernelInfo =
    KernelInfo(
      "echo",
      "0.1",
      KernelInfo.LanguageInfo(
        "echo",
        "1.0",
        "text/echo",
        "echo",
        "text" // ???
      ),
      "Echo kernel"
    )

  @volatile private var count = 0

  def execute(
    code: String,
    storeHistory: Boolean,
    inputManager: Option[InputManager],
    outputHandler: Option[OutputHandler]
  ): ExecuteResult =
    if (code.startsWith("print "))
      outputHandler match {
        case None =>
          ExecuteResult.Error("No output handler found")
        case Some(handler) =>
          handler.stdout(code.stripPrefix("print "))
          if (storeHistory)
            count += 1
          ExecuteResult.Success()
      }
    else {
      if (storeHistory)
        count += 1
      ExecuteResult.Success(
        DisplayData.text("> " + code)
      )
    }

  def currentLine(): Int =
    count

  override def complete(code: String, pos: Int): Completion = {

    // try to complete 'print' at the beginning of the cell

    val firstWord = code.takeWhile(_.isLetter)

    val completePrint = pos <= firstWord.length &&
      firstWord.length < "print".length &&
      "print".take(firstWord.length) == firstWord &&
      code.lift(firstWord.length).forall(_.isSpaceChar)

    if (completePrint)
      Completion(0, firstWord.length, Seq("print"))
    else
      Completion.empty(pos)
  }

}