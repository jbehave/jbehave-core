
def args = this.args

if ( args.length != 3 ){
	println("replaceAllInFiles: <filesEndingIn> <replacing> <with>")
	System.exit(-1)
}

def filesEndingIn = args[0]
def replacing = args[1]
def with = args[2]
                      
def currentDir = new File(".");
def fileText;
currentDir.eachFileRecurse(
  {file ->
    if (file.name.endsWith(filesEndingIn)) {
      fileText = file.text;
      fileText = fileText.replaceAll(replacing, with)
      file.write(fileText);
    }
  }
)