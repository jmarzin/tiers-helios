/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object SessionEnCours {
  var listeCodesCollocs: List[String] = List()
  var listeCodesTitres: List[String] = List()
  def ? : Boolean = {
    return true
    return !(listeCodesCollocs.isEmpty)
  }
}
