
EXAMPLE=$1

if [ "$EXAMPLE" == "" ]; then
  echo "usage: upload-reports.sh <example>"
  exit;
fi

cd target 

cp -r jbehave-reports/view $EXAMPLE

zip -r $EXAMPLE.zip $EXAMPLE

scp $EXAMPLE.zip jbehave.org:

EXAMPLES="/var/www/jbehave.org/reference/examples"

ssh jbehave.org "rm -r $EXAMPLE; unzip -uo $EXAMPLE.zip; mkdir -p $EXAMPLES; rm -r $EXAMPLES/$EXAMPLE; mv -f $EXAMPLE $EXAMPLES"
