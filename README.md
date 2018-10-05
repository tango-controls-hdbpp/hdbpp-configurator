# Project hdbpp-configurator

Maven Java project

### Java GUI configurator for TANGO HDB++

 - The TANGO archiving system is a tool allowing TANGO users to store the readings coming from a TANGO based control system into a database.
 - The archived data are essential for the day by day operation of complex scientific facilities for instance.
 - They can be used for long term monitoring of subsystems, statistics, parameters correlation or comparison of operating setups over time.
 - To take advantage of the fast and lightweight event-driven communication provided by TANGO release 8 with the adoption of ZeroMQ,
	    a novel archiving system for the TANGO Controls framework, named HDB++, has been designed and developed,
	    resulting from a collaboration between Elettra and ESRF at the beginning.
 - HDB++ design allows TANGO users to store data with microsecond timestamp resolution into traditional database management systems
	    such as MySQL or into NoSQL databases such as Apache Cassandra. 


## Cloning

```
git clone git@github.com:tango-controls-hdbpp/hdbpp-configurator
```

## Download

[ ![Download](https://api.bintray.com/packages/tango-controls/maven/hdbpp-configurator/images/download.svg) ](https://bintray.com/tango-controls/maven/hdbpp-configurator/_latestVersion)

## Documentation 

[![Docs](https://img.shields.io/badge/Latest-Docs-orange.svg)](http://www.esrf.fr/computing/cs/tango/tango_doc/tools_doc/hdb++-configurator/index.html)


## Building and Installation

### Dependencies

The project has the following dependencies.

#### Project Dependencies 

* Jive.jar
* jhdbviewer.jar
* JTango.jar
* ATKCore.jar
* ATKWidget.jar
  

#### Toolchain Dependencies 

* javac 7 or higher
* maven
  


### Build

```
cd hdbpp-configurator
mvn package
```

### Run 
see start_gui file as example.
 - https://github.com/tango-controls-hdbpp/hdbpp-configurator/blob/master/start_gui
