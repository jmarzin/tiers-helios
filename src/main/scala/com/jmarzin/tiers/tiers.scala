package main.scala.com.jmarzin.tiers

import java.awt.{Color, Point}

import scala.swing.BorderPanel.Position._
import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, BoxPanel, Button, ButtonGroup, Label, MainFrame, Orientation, RadioButton, SimpleSwingApplication}

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object tiers extends SimpleSwingApplication {

  var threadHelios : Thread = null

  def afficheAvancement(x: Any): String = x match {
    case 0 => " "
    case 1 => "1 titre traité"
    case y: Int => y.toString + " titres traités"
  }

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

    listenTo(bouton)
    listenTo()

    Timer(1000) {
      if (threadHelios == null) {
        message.text = afficheAvancement(0)
      } else if (threadHelios.isAlive) {
        message.text = afficheAvancement(Helios.nbTitresTraites)
      } else {
        message.text = "Hélios planté après " + afficheAvancement(Helios.nbTitresTraites)
        bouton.text = "Quitter"
      }
    }

    reactions += {
      case ButtonClicked(component) if component == bouton =>
        if (bouton.text.equals("Lancer")) {
          bouton.text = "Arrêter"
          reprendre.enabled = false
          recommencer.enabled = false
          if(recommencer.selected) {
            Base.vide
          }
          threadHelios = new Thread {
            override def run {
              try {
                Helios.parcours
              } catch {
                case _  => {
                }
              }
            }
          }
          threadHelios.start
        } else if (bouton.text.equals("Arrêter")) {
          Timer.stop
          message.text = "Interrompu après " + afficheAvancement(Helios.nbTitresTraites)
          bouton.text = "Quitter"
          threadHelios.interrupt
        } else if (bouton.text.equals("Quitter")) {
          sys.exit(0)
        }
    }
  }
}