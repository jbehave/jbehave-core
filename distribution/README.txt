To build site with standalone XSite:

1. Download http://repository.codehaus.org/org/codehaus/xsite/xsite-distribution/1.0/xsite-distribution-1.0-bin.zip
2. Unzip to local dir, eg ~/xsite, add env XSITE_HOME=~/xsite and $XSITE_HOME/bin to $PATH (as per usual Ant, Maven, etc convention)
3. xsite -Ssrc/site -mcontent/sitemap.xml -stemplates/skin.html -Rresources -otarget/xsite