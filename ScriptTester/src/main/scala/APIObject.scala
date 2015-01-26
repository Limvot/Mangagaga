object APIObject {

  def instance() = this
  def note(theNote: String) = System.out.println(theNote)
  
  def download(filePath: String): String =  {
    System.out.println("APIObject Downloading: " + filePath)
    Utilities.download(filePath)
  }

  def readFile(absolutePath: String): String = {
    System.out.println("APIObject Reader Path is: " + absolutePath)
    try {
      return Utilities.readFile(absolutePath)
    } catch {
      case e:Exception => System.out.println("Could not open in APIObject:readFile" + e.toString)
    }
    "FAILURE"
  }
  def slice(toSlice: String, a: Int, b: Int): String = toSlice.substring(a,b)
}
