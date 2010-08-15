
EXAMPLE=$1

if [ "$EXAMPLE" == "" ]; then
  echo "usage: upload-reports.sh <example>"
  exit;
fi

cd target 

cp -r jbehave-reports/view $EXAMPLE

zip -r $EXAMPLE.zip $EXAMPLE

scp $EXAMPLE.zip jbehave.org:

ssh jbehave.org "rm -r $EXAMPLE; unzip -uo $EXAMPLE.zip; rm -r /var/www/jbehave.org/reference/examples/$EXAMPLE; mv -f $EXAMPLE /var/www/jbehave.org/reference/examples"
