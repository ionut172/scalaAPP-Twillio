package models

case class Utilizator(tipUtilizator: String, cnp: String, nume: String, prenume: String, soldActual: Double, telefon: String) {
  // Metoda pentru a afișa informațiile utilizatorului
  def afiseazaInformatii(): Unit = {
    println(s"Tip utilizator: $tipUtilizator")
    println(s"CNP: $cnp")
    println(s"Nume: $nume")
    println(s"Prenume: $prenume")
    println(s"Sold Actual: $soldActual")
    println(s"Telefon: $telefon")
  }

  def actualizeazaSold(nouSold: Double): Utilizator = copy(soldActual = nouSold)

  def findByCNP(targetCNP: String): Option[Utilizator] =
    if (cnp == targetCNP) Some(this)
    else None
}

