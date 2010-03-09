#!/bin/sh

MVN_GOAL="mvn install:install-file"

if [ $# -lt 4 ]
then
	echo "usage: install-file.sh <groupId> <artifactId> <version> <file>"
	exit -1
fi

GROUP_ID=$1
ARTIFACT_ID=$2
VERSION=$3
FILE=$4
$MVN_GOAL -DgroupId=$GROUP_ID -DartifactId=$ARTIFACT_ID -Dversion=$VERSION -Dfile=$FILE -Dpackaging=jar -DgeneratePom=true 