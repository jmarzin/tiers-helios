package main.scala.com.jmarzin.tiers

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object SessionEnCours {
  var collocsTraitees: List[String] = List()
  var collocEnCours : String = ""
  var titresTraites: List[String] = List()

  def raz: Unit = {
    collocsTraitees = List()
    collocEnCours = ""
    titresTraites = List()
  }

  def ? : Boolean = {
    return (collocsTraitees.nonEmpty || collocEnCours.nonEmpty || titresTraites.nonEmpty)
  }

  def collocDejaTraitee(colloc : Colloc) : Boolean = {
    return collocsTraitees.contains(colloc.code)
  }

  def titreDejaTraite(titre : Titre) : Boolean = {
    return Base.titreConnu(titre)
    //collocsTraitees.contains(titre.colloc.code) || (collocEnCours.equals(titre.colloc.code) &&
    //  titresTraites.contains(titre.code))
  }

  def memoriseColloc(colloc : Colloc) : Unit = {
    if (!collocDejaTraitee(colloc) && !colloc.code.equals("")) {
      collocsTraitees = collocsTraitees :+ colloc.code
      collocEnCours = ""
      titresTraites = List()
    }
  }

  def memoriseTitre(titre : Titre) : Unit = {
    if (!collocEnCours.equals(titre.colloc.code)) {
      val temp = titre.colloc.code
      titre.colloc.code = collocEnCours
      memoriseColloc(titre.colloc)
      titre.colloc.code = temp
      collocEnCours = titre.colloc.code
    }
    if (titre.code.nonEmpty && !titreDejaTraite(titre)) {
      titresTraites = titresTraites :+ titre.code
    }
  }
}
