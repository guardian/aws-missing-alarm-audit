package example


object Model {
  object OptionPickler extends upickle.AttributeTagged {
    implicit def optionWriter[T: Writer]: Writer[Option[T]] =
      implicitly[Writer[T]].comap[Option[T]] {
        case None => null.asInstanceOf[T]
        case Some(x) => x
      }

    implicit def optionReader[T: Reader]: Reader[Option[T]] = {
      new Reader.Delegate[Any, Option[T]](implicitly[Reader[T]].map(Some(_))){
        override def visitNull(index: Int) = None
      }
    }
  }

  import OptionPickler._

  case class Repository(
    name: String,
  )

  case class Item(
    name: String,
    path: String,
    url: String,
    html_url: String,
    repository: Repository,
  )


  case class Search(
    total_count: Int,
    incomplete_results: Boolean,
    items: List[Item]
  )

  implicit val RepositoryRW: ReadWriter[Repository] = macroRW
  implicit val ItemRW: ReadWriter[Item] = macroRW
  implicit val SearchRW: ReadWriter[Search] = macroRW
}