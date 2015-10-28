Have a look ath the <threads> element within the pom.xml of this example.

By default, two threads are going to execute stories. There are three stories, so two will go in parallel, followed by the third on its own.

If you do mvn install -Dthreads=1 you're overiding the <threads> setting of the pom file.