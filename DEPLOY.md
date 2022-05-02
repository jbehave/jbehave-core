# Deploy JBehave

## Clone JBehave Site

Clone jbehave-site to external path, eg to a sibling dir: 

````
$ git clone git@bitbucket.org:jbehave/jbehave-site.git ../site
````

## Deploy release version 

````
$ ../site/release.sh jbehave <version> <qualifier> <next>
````

where:

- version is the release version, e.g. 5.0
- qualifier is the type of release, stable or preview
- next is the next snapshot version, e.g. 5.1-SNAPSHOT


## Deploy snapshot version

````
$ ../site/shapshot.sh jbehave <version> 
````

where:

- version is the snapshot version, e.g. 5.1-SNAPSHOT
  


