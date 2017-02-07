package main.scala.com.jmarzin.tiers

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object SessionEnCours {
  var collocsTraiteesTitres: List[String] = List()
  var collocEnCoursTitres : String = ""
  var pageTitres: String = ""
  var titresTraites: List[String] = List()
  var collocsTraiteesArticles: List[String] = List()
  var collocEnCoursArticles : String = ""
  var pageArticles: String = ""
  var articlesTraites: List[String] = List()

  def raz(): Unit = {
    collocsTraiteesTitres = List()
    collocEnCoursTitres = ""
    titresTraites = List()
    collocsTraiteesArticles = List()
    collocEnCoursArticles = ""
    articlesTraites = List()
  }

  def ? : Boolean = {
    collocsTraiteesTitres.nonEmpty ||
      collocEnCoursTitres.nonEmpty ||
      titresTraites.nonEmpty ||
      collocsTraiteesArticles.nonEmpty ||
      collocEnCoursArticles.nonEmpty ||
      articlesTraites.nonEmpty
  }

  def collocDejaTraitee(typePiece: Symbol, colloc : Colloc) : Boolean = {
    if(typePiece == 'titre)
      collocsTraiteesTitres.contains(colloc.code)
    else
      collocsTraiteesArticles.contains(colloc.code)
  }

  def pieceDejaTraitee(typePiece: Symbol, piece: Piece) : Boolean = {
    var result = false
    if (typePiece == 'titre) {
      result = collocEnCoursTitres == piece.colloc.code && titresTraites.contains(piece.code)
    } else {
      result = collocEnCoursArticles == piece.colloc.code && articlesTraites.contains(piece.code)
    }
    if(!result) {
      result = Base.pieceConnue(piece)
    }
    result
  }

  def memoriseColloc(typePiece: Symbol, colloc : Colloc) : Unit = {
    if (!collocDejaTraitee(typePiece, colloc) && !colloc.code.equals("")) {
      if(typePiece == 'titre) {
        collocsTraiteesTitres = collocsTraiteesTitres :+ colloc.code
        collocEnCoursTitres = ""
        pageTitres = "1"
        titresTraites = List()
      } else {
        collocsTraiteesArticles = collocsTraiteesArticles :+ colloc.code
        collocEnCoursArticles = ""
        pageArticles = "1"
        articlesTraites = List()
      }
      Base.sauveSession()
    }
  }

  def memorisePiece(typePiece: Symbol, piece : Piece) : Unit = {
    if (typePiece == 'titre) {
      if (!collocEnCoursTitres.equals(piece.colloc.code)) {
        val temp = piece.colloc.code
        piece.colloc.code = collocEnCoursTitres
        memoriseColloc('titre, piece.colloc)
        piece.colloc.code = temp
        collocEnCoursTitres = piece.colloc.code
      }
      if (piece.code.nonEmpty && !Base.pieceConnue(piece)) {
        titresTraites = titresTraites :+ piece.code
        pageTitres = piece.page
      }
    } else {
      if (!collocEnCoursArticles.equals(piece.colloc.code)) {
        val temp = piece.colloc.code
        piece.colloc.code = collocEnCoursArticles
        memoriseColloc('role, piece.colloc)
        piece.colloc.code = temp
        collocEnCoursArticles = piece.colloc.code
      }
      if (piece.code.nonEmpty && !Base.pieceConnue(piece)) {
        articlesTraites = articlesTraites :+ piece.code
        pageArticles = piece.page
      }
    }
    Base.sauveSession()
  }
}
