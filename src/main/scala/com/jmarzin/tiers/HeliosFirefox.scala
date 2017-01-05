package main.scala.com.jmarzin.tiers

import java.util.concurrent.TimeUnit
import javax.swing.JOptionPane.showMessageDialog
import javax.swing.{JOptionPane, JPasswordField, JTextField}

import main.scala.com.jmarzin.tiers.Helios.listeTest
import org.openqa.selenium.{By, WebDriverException, WebElement}
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}

/**
  * Created by jmarzin-cp on 28/12/2016.
  */
trait HeliosFirefox extends FirefoxDriver{

  var nbAppelsCollocSuivante = 0
  var nbAppelsPremierTitreColloc = 0
  var nbAppelsTitreSuivantColloc = 0
  var listeTest: List[(Int ,List[Int])] = List()

  def enleveCollocTest :Unit = {
    if (!listeTest.isEmpty) {
      listeTest = listeTest.tail
    }
  }

  def enleveTitreTest :Unit = {
    if (listeTest.nonEmpty && listeTest.head._2.nonEmpty) {
      listeTest = (listeTest.head._1,listeTest.head._2.tail) :: listeTest.tail
    }
  }

  def init : Unit = {
    manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS)
    get("http://ulysse.dgfip")
    Thread.sleep(500)
    var utilisateur = ""
    var password = ""
    if (!AppTiers.developpement) {
      var nom = new JTextField()
      var okCxl = JOptionPane.showConfirmDialog(null, nom, "Votre nom d'utilisateur", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (okCxl == JOptionPane.OK_OPTION) {
        utilisateur = nom.getText
      } else {
        throw new IllegalArgumentException
      }
      var pf = new JPasswordField()
      okCxl = JOptionPane.showConfirmDialog(null, pf, "Votre mot de passe", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (okCxl == JOptionPane.OK_OPTION) {
        password = new String(pf.getPassword)
      } else {
        throw new IllegalArgumentException
      }
    } else {
      utilisateur = "jacques.marzin"
      password = "Ce1dpgpam2"
    }
    get("http://portailapplicatif.appli.impots")
    val handles = getWindowHandles.toArray
    switchTo.window(handles(handles.length-1).toString)
    val identifiant = findElements(By.id("identifiant"))
    if (!identifiant.isEmpty()) {
      identifiant.get(0).sendKeys(utilisateur)
      val pwd = findElement(By.id("secret_tmp"))
      pwd.sendKeys(password)
      findElement(By.className("valid")).click()
    }
    findElement(By.className("nom_appli"))
    if(!getPageSource().contains("http://helios.appli.impots")) {
      throw new WebDriverException()
    }
    get("http://helios.appli.impots")
//      findElementsByCssSelector("input")
//      if(getPageSource().contains("Choix du poste comptable")) {
//        findElement(By.xpath("//input[@value='031046']")).click()
//        findElementById("valider").click()
//        findElementsByCssSelector("input")
//      }
//      if(getPageSource().contains("AVERTISSEMENT")) {
//        findElement(By.className("inputcontinuer")).click()
//      }
//    } else {
//
//      showMessageDialog(null, "Connectez-vous sur Hélios\net ne touchez plus à rien !")
//    try {
      val myDynamicElement = new WebDriverWait(this, 10)
        .until(ExpectedConditions.presenceOfElementLocated(By.name("frmWork")))
//    } catch {
//      case e: Exception => {
//        var page = getPageSource
//        println (page.size)
//      }
//    }
    findElement(By.tagName("FRAME"))
    switchTo().frame("frmMenu")
    findElementByTagName("LINK")
    executeScript("m.go('172')")
  }

  def collocSuivante(titre : Titre) :Titre = {
    nbAppelsCollocSuivante += 1
    if (titre.colloc.code == "") {
      titre.colloc.rangDansLaPage = 0
      titre.colloc.rangPage = 0
    } else {
      titre.colloc.rangDansLaPage += 1
    }
    titre.code = ""

    // pour le test du Trait

    if (listeTest.nonEmpty) {
      if (titre.colloc.code.nonEmpty) {
        enleveCollocTest
      }
      titre.colloc.code = listeTest.head._1.toString
      if (listeTest.size == 1) {
        titre.colloc.derniereColloc = true
      } else {
        titre.colloc.derniereColloc = false
      }
      return titre
    }

    // pour la production
    if (getWindowHandles.size == 1) {
      switchTo().defaultContent
      switchTo().frame("frmWork")
      findElementByLinkText("valeurs").click
      while (getWindowHandles.size != 2) {
        Thread.sleep(100)
      }
      val handles = getWindowHandles.toArray
      switchTo.window((handles(handles.length - 1).toString))
      for( a <- 1 to titre.colloc.rangPage){
        findElementByXPath("//input[@value='>>']").click
        findElementByXPath("//tr[@idx]//a")
      }
    }
    val colbuds = findElementsByXPath("//tr[@idx]//a")
    titre.colloc.code = colbuds.get(titre.colloc.rangDansLaPage).getText
    AppTiers.collocEnCours = titre.colloc.code
    AppTiers.rangTitreCollocEnCours = 0
    if (titre.colloc.rangDansLaPage == colbuds.size - 1) {
      if (getPageSource.contains("value=\"&gt;&gt;\"")) {
        findElementByXPath("//input[@value='>>']").click
        findElementByXPath("//tr[@idx]//a")
        titre.colloc.rangPage += 1
        titre.colloc.rangDansLaPage = -1
      } else {
        titre.colloc.derniereColloc = true
      }
    } else {
      titre.colloc.derniereColloc = false
    }
    if (titre.colloc.derniereColloc) {
      print("\r\nDernière colloc " + titre.colloc.code)
    } else {
      print("\r\ncolloc " + titre.colloc.code)
    }
    return titre
  }

  def premierTitreColloc(titre : Titre) :Titre = {
    nbAppelsPremierTitreColloc += 1

    // cas de test du Trait

    if (listeTest.nonEmpty) {
      if (listeTest.head._2.nonEmpty) {
        titre.code = listeTest.head._2.head.toString
        titre.rangDansLaPage = 0
        titre.colloc.pasDeTitre = false
      } else {
        titre.code = ""
        titre.rangDansLaPage = -1
        titre.colloc.pasDeTitre = true
      }
      return titre
    }

    // pour la production

    findElementById("annuler").click
    while (getWindowHandles.size == 2) {
      Thread.sleep(100)
    }
    val handles = getWindowHandles.toArray
    switchTo.window((handles(handles.length - 1).toString))
    switchTo().frame("frmWork")
    findElementById("_1").sendKeys(titre.colloc.code)
    findElementById("_3").clear
    findElementById("_6").sendKeys("31/12/2016")
    val elementCache = findElementByName("RCE_CT_F07_CriteresRecherche_CAF.#piecesNonSoldees")
    if (elementCache.getAttribute("value") == "false") {
      findElementByName("RCE_CT_F07_CriteresRecherche_CAF.#piecesNonSoldees-checkbox").click}
    findElementByClassName("inputvalider").click
    findElementByClassName("inputvalider")
    if (getPageSource().contains("total  0 titre")) {
      titre.code = ""
      titre.rangDansLaPage = -1
      titre.colloc.pasDeTitre = true
    } else {
      val res = findElementsByClassName("soustitre2")
      for (i <- 0 to res.size-1) {
        var texte = res.get(i).getText
        if (texte.contains("Liste des titres (total")) {
          texte = texte.replaceFirst("L.*\\(total","")
          texte = texte.replaceFirst("titres\\)","").trim
          AppTiers.rangTitreCollocEnCours = 0
          if (texte.matches("\\d*")) {
            AppTiers.nbTitresCollocEnCours = texte.toInt
            AppTiers.collocEnCours = titre.colloc.code
          }
          else {
            AppTiers.nbTitresCollocEnCours = 0
          }
        }
      }
      titre.rangDansLaPage = 0
      litCodeTitre(titre)
      titre.colloc.pasDeTitre = false
    }
    return titre
  }

  def setDernierTitre(titre: Titre) : Titre = {

    // cas de test du Trait
    if (listeTest.nonEmpty) {
      if (listeTest.nonEmpty && listeTest.head._2.size == 1) {
        titre.dernierTitreDeLaColloc = true
      }
      enleveTitreTest
      return titre
    }

    // cas de production

    findElementByClassName("inputreset")
    val boutonsDetail = findElementsByXPath("//input[@value='Détail'][@name='OID']")
    titre.dernierTitreDeLaColloc = false
    if (titre.rangDansLaPage == boutonsDetail.size - 1 && !getPageSource().contains("value=\"&gt;&gt;\"")) {
      titre.dernierTitreDeLaColloc = true
    }
    return titre
  }

  def litCodeTitre(titre : Titre) : Unit = {
    //var texte = findElementsByXPath("//tr[@idx]/td[2]/parent::*/preceding-sibling::tr[th]").get(rang).getText
    //texte = texte.replaceFirst("N°Bordereau : ","").replaceFirst("Exercice ","")
    val codeTitre = findElementsByXPath("//tr[@idx]/td[2]").get(titre.rangDansLaPage).getText
    manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS)
    try {
      var anneeBordereau = findElementByXPath("//TR[TD='\n\t\t\t\t\t\t\t\t\t" +
        codeTitre.split(" - ")(0) +
        "\n\t\t\t\t\t\t\t\t\t-\n\t\t\t\t\t\t\t\t\t" +
        codeTitre.split(" - ")(1) +
        "\n\t\t\t\t\t\t\t\t']/preceding-sibling::TR[TH][1]").getText
      anneeBordereau = anneeBordereau.replaceFirst("N°Bordereau : ", "").replaceFirst("Exercice ", "").replaceAll(" ", "")
      titre.annee = anneeBordereau.split("-")(1)
      titre.bordereau = anneeBordereau.split("-")(0)
    } catch {
      case e: org.openqa.selenium.NoSuchElementException =>
    }
    manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS)
    titre.code = (titre.annee + "-" + titre.bordereau + "-" + codeTitre).replaceAll(" ","")
    //var codeTitre = findElementsByXPath("//tr[@idx]/td[2]").get(rang).getText
    //var bordereau = findElementByXPath("//TR[TD='"+codeTitre+"']/preceding-sibling::TR[TH][1]")
  }

  def litDebiteur(titre : Titre): Unit = {
    findElementByXPath("//input[@value='Retour']")
    val boutonsDetail = findElementsByXPath("//input[@value='Détail']")
    var zone = findElementByXPath("//input[@value='Détail']/parent::*/preceding-sibling::td[1]")
    titre.debiteur.identifiant = zone.getText.replaceAll("'","").trim
    boutonsDetail.get(1).click
    findElementByXPath("//input[@value='Retour']")
    zone = findElementByXPath("//td[label='Catégorie']/following-sibling::td[1]")
    titre.debiteur.categorie = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Nature juridique']/following-sibling::td[1]")
    titre.debiteur.natureJuridique = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Immatriculation']/following-sibling::td[1]")
    titre.debiteur.immatriculation = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Statut DGFIP']/following-sibling::td[1]")
    titre.debiteur.statutDgfip = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Civilité']/following-sibling::td[1]")
    titre.debiteur.civilite = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Nom/RS']/following-sibling::td[1]")
    titre.debiteur.nomRs = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Prénom']/following-sibling::td[1]")
    titre.debiteur.prenom = zone.getText.replaceAll("'"," ").trim
    if (titre.debiteur.prenom.equals(".")) {
      titre.debiteur.prenom = ""
    }
    zone = findElementByXPath("//td[label='Complément']/following-sibling::td[1]")
    titre.debiteur.complement = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Résident en France']/following-sibling::td[1]")
    titre.debiteur.residentEnFrance = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Date de naissance']/following-sibling::td[1]")
    titre.debiteur.dateDeNaissance = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Tél. Domicile']/following-sibling::td[1]")
    titre.debiteur.telDomicile = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Tél. Portable']/following-sibling::td[1]")
    titre.debiteur.telPortable = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='E-mail']/following-sibling::td[1]")
    titre.debiteur.emel = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//tr[td='Référence du compte par défaut']/following-sibling::tr[1]/td")
    titre.debiteur.compteParDefaut = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Code Actualité']/following-sibling::td[1]")
    titre.debiteur.codeActualite = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Date de  mise à jour']/following-sibling::td[1]")
    titre.debiteur.dateMiseAJour = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='NFP']/following-sibling::td[1]")
    titre.debiteur.nfp = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Date Statut DGFIP']/following-sibling::td[1]")
    titre.debiteur.dateStatutDgfip = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Adresse Principale']")
    titre.debiteur.adresse.adressePrincipale = zone.getText.replaceFirst("Adresse Principale","").replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='NPAI']")
    titre.debiteur.adresse.npai = zone.getText.replaceFirst("NPAI","").replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Date mise à jour']")
    titre.debiteur.adresse.dateMiseAJour = zone.getText.replaceFirst("Date mise à jour","").replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Orig. mise à jour']")
    titre.debiteur.adresse.origineMiseAJour = zone.getText.replaceFirst("Orig. mise à jour","").replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Cplt. Adresse (Bât, Rés...)']/following-sibling::td[1]")
    titre.debiteur.adresse.complementAdresse = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Adresse (N° et voie)']/following-sibling::td[1]")
    titre.debiteur.adresse.numeroEtVoie = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Localité (Lieu-dit, BP...)']/following-sibling::td[1]")
    titre.debiteur.adresse.localite = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='CP-Ville']/following-sibling::td[1]")
    titre.debiteur.adresse.cpVille = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Pays']/following-sibling::td[1]")
    titre.debiteur.adresse.pays = zone.getText.replaceAll("'"," ").trim
    //TODO gérer le cas où il y a plusieurs adresses
    if (getPageSource().contains("title=\"tiers consolidé\"")) {
      zone = findElementByCssSelector("nobr")
      titre.debiteur.dateConsolidation = zone.getText.replaceFirst("Date de consolidation :","").replaceAll("'"," ").trim
      titre.debiteur.consolide = "Oui"
      findElementByLinkText("Tiers Consolidé").click
      var codesTiersConsolides = findElementsByXPath("//td[@title='nom/rs complet']/preceding-sibling::td[1]")
      val retour = findElementByXPath("//input[@value='Retour']")
      var liste = ""
      for (a <- 0 to codesTiersConsolides.size - 1) {
        liste += codesTiersConsolides.get(a).getText + ";"
      }
      titre.debiteur.listeConsolidation = liste.substring(0, liste.length() - 1)
      var tableau = titre.debiteur.listeConsolidation.split(";").sorted.toList
      var nouvelleListe = ""
      tableau.foreach(nouvelleListe += _ + ";")
      titre.debiteur.listeConsolidation = nouvelleListe.substring(0, nouvelleListe.length()-1)
      retour.click
    } else {
      titre.debiteur.dateConsolidation = ""
      titre.debiteur.consolide = "Non"
      titre.debiteur.listeConsolidation = titre.debiteur.identifiant
    }
    findElementByXPath("//input[@value='Retour']").click
    findElementByXPath("//input[@value='Retour']").click
  }

  def litTitre(titre : Titre) : Titre = {

    // cas de test du Trait

    if (listeTest.nonEmpty) {
      return titre
    }

    // cas de production

    findElementByClassName("inputreset")
    val boutonsDetail = findElementsByXPath("//input[@value='Détail'][@name='OID']")
    //var texte = findElementsByXPath("//tr[@idx]/td[2]/parent::*/preceding-sibling::tr[1]").get(titre.rangDansLaPage).getText
    //texte = texte.replaceFirst("N°Bordereau : ","").replaceFirst("Exercice ","")
    //titre.code = (findElementsByXPath("//tr[@idx]/td[2]").get(titre.rangDansLaPage).getText + texte).replaceAll(" ","")
    boutonsDetail.get(titre.rangDansLaPage).click
    var page = getPageSource //Bordereau Titres
    var zone = findElementByXPath("//table[@class='tablezonesaisie']/descendant::tr[10]/td[2]")
    titre.dateEmission = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[@width='135px']")
    titre.resteARecouvrerPrincipal = zone.getText.replaceAll("[.,€]", "").toInt
    zone = findElementByXPath("//td[@width='120px']")
    titre.resteARecouvrerFrais = zone.getText.replaceAll("[.,€]", "").toInt
    zone = findElementByXPath("//td[@width='186px']/following-sibling::td[1]")
    titre.datePrescription = zone.getText
    if (getPageSource().contains("Tiers débiteur")) {
      findElementByLinkText("Tiers débiteur").click
      litDebiteur(titre)
    } else {
    //TODO traiter le cas où le lien Tiers débiteur n'existe pas
    }
    findElementByXPath("//input[@value='Retour']").click
    findElementByXPath("//input[@value='Détail'][@name='OID']")
    print("\r\nTitre "+ titre.code)
    if (titre.dernierTitreDeLaColloc) {
      print(" dernier")
    }
    return titre
  }
  def titreSuivantColloc(titre : Titre) : Titre = {
    nbAppelsTitreSuivantColloc += 1
    titre.rangDansLaPage += 1

    // cas de test du trait

    if (listeTest.nonEmpty) {
      titre.code = listeTest.head._2.head.toString
      return titre
    }

    // cas de production
    findElementByXPath("//input[@value='Détail'][@name='OID']")
    val boutonsDetail = findElementsByXPath("//input[@value='Détail'][@name='OID']")
    if (titre.rangDansLaPage > boutonsDetail.size - 1) {
      if (getPageSource.contains("value=\"&gt;&gt;\"")) {
        findElementByXPath("//input[@value='>>']").click
        findElementByXPath("//input[@value='Détail']")
        titre.rangDansLaPage = 0
      }
    }
    litCodeTitre(titre)
    AppTiers.rangTitreCollocEnCours += 1
    return titre
  }
}
