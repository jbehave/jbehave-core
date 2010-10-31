
VERSION=$1
QUALIFIER=$2

if [ "$VERSION" == "" ] || [ "$QUALIFIER" == "" ]; then
  echo "usage: upload-download.sh <version> <qualifier>"
  exit;
fi

DOWNLOADS="/var/www/jbehave.org/reference/downloads"

NEXUS="https://nexus.codehaus.org/content/repositories/releases"

for TYPE in "bin" "src"
do
INDEX="index-$TYPE-$VERSION.html"
VERSIONED="$DOWNLOADS/$TYPE/$VERSION"
URL="$NEXUS/org/jbehave/jbehave-distribution/$VERSION/jbehave-distribution-$VERSION-$TYPE.zip"
DOWNLOAD="<html><head><meta http-equiv=\"REFRESH\" content=\"0;url=$URL\"></head></html>"
echo $DOWNLOAD > target/$INDEX
scp target/$INDEX jbehave.org:
ssh jbehave.org "mkdir -p $VERSIONED; mv $INDEX $VERSIONED/index.html; cd $DOWNLOADS/$TYPE; rm $QUALIFIER; ln -s $VERSION $QUALIFIER"
done 

