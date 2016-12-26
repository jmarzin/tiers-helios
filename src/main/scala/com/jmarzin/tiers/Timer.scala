package main.scala.com.jmarzin.tiers

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object Timer {
  var t : javax.swing.Timer = null
  def stop {
    t.stop()
  }
  def apply(interval: Int, repeats: Boolean = true)(op: => Unit) {
    val timeOut = new javax.swing.AbstractAction() {
      def actionPerformed(e : java.awt.event.ActionEvent) = op
    }
    t = new javax.swing.Timer(interval, timeOut)
    t.setRepeats(repeats)
    t.start()
  }
}

