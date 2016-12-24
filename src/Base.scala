/**
  * Created by jmarzin-cp on 24/12/2016.
  */

object Base {
  def init(repertoire: String) : Unit = {
    SessionEnCours.listeCodesCollocs = List()
    SessionEnCours.listeCodesTitres = List()
  }
}
