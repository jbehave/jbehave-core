#!/usr/bin/env bash
set -e

CWD=`pwd`
MVN="mvn -s $CWD/settings.xml" 
PROFILES=examples

$MVN -U clean install -P $PROFILES

