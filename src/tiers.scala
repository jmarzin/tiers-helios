import java.awt.{Color, Point}

import com.sun.xml.internal.bind.v2.TODO

import scala.swing.BorderPanel.Position._
import scala.swing.event.{ButtonClicked, EditDone}
import scala.swing.{BorderPanel, BoxPanel, Button, ButtonGroup, Label, MainFrame, Orientation, RadioButton, SimpleSwingApplication, Swing, TextField}

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object tiers extends SimpleSwingApplication {
  def top = new MainFrame {

    Base.init("C:\\tiers\\titres")

    Helios.init

    title = "Récupération des RAR"
    location = new Point(1100,0)

    val mutex = new ButtonGroup
    val reprendre = new RadioButton("Reprendre la session précédente")
    val recommencer = new RadioButton("Tout récupérer")
    val radios = List(reprendre, recommencer)
    if (SessionEnCours.?) {
      mutex.buttons ++= radios
      mutex.select(reprendre)
    } else {
      mutex.select(recommencer)
      reprendre.visible = false
      recommencer.visible = false
    }
    val options = new BoxPanel(Orientation.Vertical) {
      contents ++= radios
    }

    val bouton = new Button {
      text = "Lancer"
    }

    val message = new Label{
      text = " "
      foreground = Color.red
    }

    contents = new BorderPanel {
      layout(options) = North
      layout(bouton) = Center
      layout(message) = South
    }

    listenTo(reprendre)
    listenTo(recommencer)
    listenTo(bouton)
    listenTo()

    reactions += {
      case ButtonClicked(component) if component == bouton =>
        bouton.text = "Arrêter"
        reprendre.enabled = false
        recommencer.enabled = false
    }


//    reactions += {
//      case EditDone(component) if component == code =>
//        if (centPremiersCodes.contains(code.text)) {
//          message.text = " "
//          bouton.doClick
//          code.enabled = false
//        } else {
//          message.text = "Code erroné ou déjà consommé"
//        }
//    TODO attendre l'ordre de lancement et lancer le traitement
//    TODO attendre l'ordre d'arrêt et arrêter le traitement
//    TODO attendre la fin du traitemenet et afficher le résultat
  }
}