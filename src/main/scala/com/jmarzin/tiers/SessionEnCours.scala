package main.scala.com.jmarzin.tiers

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object SessionEnCours {
  var listeCodesCollocs: List[String] = List()
  var listeCodesTitres: List[String] = List()

  def raz: Unit = {
    listeCodesCollocs = List()
    listeCodesTitres = List()
  }

  def ? : Boolean = {
    return true
    return !(listeCodesCollocs.isEmpty)
  }

  def collocDejaTraitee(colloc : Colloc) : Boolean = {
    return listeCodesCollocs.exists(colloc.code ==) && !colloc.code.equals(listeCodesCollocs.last)
  }

  def titreDejaTraite(titre : Titre) : Boolean = {
    if (listeCodesCollocs.contains(titre.colloc.code)) {
      if (listeCodesCollocs.last == titre.colloc.code) {
        return listeCodesTitres.contains(titre.code)
      } else {
        return true
      }
    } else {
      return false
    }
  }

  def memoriseColloc(colloc : Colloc) : Unit = {
    if (!listeCodesCollocs.exists(colloc.code ==)) {
      listeCodesCollocs = listeCodesCollocs :+ colloc.code
      listeCodesTitres = List()
    }
  }

  def memoriseTitre(titre : Titre) : Unit = {
    if (!listeCodesCollocs.last.equals(titre.colloc.code)) {
      memoriseColloc(titre.colloc)
      listeCodesTitres = List(titre.code)
    } else {
      if (!listeCodesTitres.exists(titre.code ==)) {
        listeCodesTitres = listeCodesTitres :+ titre.code
      }
    }
  }
}
