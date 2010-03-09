
VERSION=$1

if [ "$VERSION" == "" ]; then
  echo "usage: upload-reference.sh <version>"
  exit;
fi

scp target/jbehave-$VERSION-bin.zip jbehave.org:

ssh jbehave.org "rm -r jbehave-$VERSION; unzip jbehave-$VERSION-bin.zip; rm -r /var/www/jbehave.org/reference/$VERSION;  mv jbehave-$VERSION/docs/ /var/www/jbehave.org/reference/$VERSION"
