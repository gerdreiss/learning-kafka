import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import fs2.Stream
import fs2.io.stdout
import fs2.text.lines
import fs2.text.utf8
import io.circe.Json
import io.circe.jawn.CirceSupportParser
import org.http4s.Request
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.literals.uri
import org.typelevel.jawn.Facade
import org.typelevel.jawn.fs2.*
import org.typelevel.jawn.AsyncParser

object WikimediaChangesProducer extends IOApp:

  given Facade[Json] = new CirceSupportParser(None, false).facade

  // FIXME: this one fails with:
  //  org.typelevel.jawn.ParseException: expected json value got ':ok
  def run(args: List[String]): IO[ExitCode] =
    Stream
      .resource(EmberClientBuilder.default[IO].build)
      .flatMap {
        _.stream(Request[IO](uri = uri"https://stream.wikimedia.org/v2/stream/recentchange"))
          .flatMap(_.body.chunks.parseJson(AsyncParser.ValueStream))
      }
      .map(_.spaces2)
      .through(lines)
      .through(utf8.encode)
      .through(stdout)
      .compile
      .drain
      .as(ExitCode.Success)
