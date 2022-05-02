ACTION=$1

if [ "$ACTION" == "" ] ; then
  echo "usage: rest.sh [import|export]"
  exit;
fi
  
PLUGIN="org.jbehave:jbehave-rest:5.1-SNAPSHOT"
PARAMS="-Djbehave.rest.rootURI=http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages -Djbehave.rest.username=jbehave -Djbehave.rest.password=jbehave"

if [ "$ACTION" == "export" ] ; then
	mvn $PLUGIN:export-from-filesystem $PARAMS -Djbehave.rest.resourcesSyntax=jbehave/3.0
else
	mvn $PLUGIN:import-to-filesystem $PARAMS
fi
