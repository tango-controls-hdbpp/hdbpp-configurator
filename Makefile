#+======================================================================
# $Source: /segfs/tango/cvsroot/jclient/jblvac/src/jblvac/Makefile,v $
#
# Project:      Configurator and diagnotics for Tango HDB++
#
# Description:  Makefile to generate the JAVA Tango classes package
#
# $Author: verdier $
#
# $Version: $
#
# $Log: Makefile,v $
#
# copyleft :    European Synchrotron Radiation Facility
#               BP 220, Grenoble 38043
#               FRANCE
#
#-======================================================================

MAJOR_RELEASE = 2
MINOR_RELEASE = 2
APPLI_VERS	=	$(MAJOR_RELEASE).$(MINOR_RELEASE)

PACKAGE      = org.tango.hdb_configurator
PACKAGE_HOME = org/tango/hdb_configurator
JAR_NAME = hdb_configurator

TANGO_HOME	=	/segfs/tango

SVN_CONFIGURATOR_PATH = $(SVN_TCS)/archiving/hdb++/gui/java-configurator
SVN_TAG_REV =	Release-$(MAJOR_RELEASE).$(MINOR_RELEASE)

#-------------------------------------------------------------------
#
#-----------------------------------------------------------------


all:

POGO=$(TANGO_HOME)/release/java/appli/org.tango.pogo.jar
UPDATE_CLASS=org.tango.pogo.pogo_gui.tools.UpdateRelease
SRC_HOME=src/main/java
version:
	@echo "-----------------------------------------"
	@echo "	Patching Version"
	@echo "-----------------------------------------"
	java -classpath $(POGO) \
	    $(UPDATE_CLASS) \
		-file      $(SRC_HOME)/$(PACKAGE_HOME)/common/SplashUtils.java \
	 	-release   $(APPLI_VERS) \
		-title     "$(PACKAGE).tools Release Notes" \
		-package   $(PACKAGE).common \
		-note_path $(SRC_HOME)/$(PACKAGE_HOME)/common


DOC_HEADER=	"HDB++ Java Configurator"
DOC_DIR=./doc/prg_references
documentation:
	javadoc 				\
	-version -author		\
	-public					\
	-windowtitle "HDB++ Java Classes" \
	-header $(DOC_HEADER)	\
	-d $(DOC_DIR)			\
	-link  .				\
	-group "HDB++ configurator classes"    "org.tango.hdb_configurator.configurator" \
	-group "HDB++ diagnostics classes"     "org.tango.hdb_configurator .diagnostics"  \
	-group "HDB++ common classes"          "org.tango.hdb_configurator .common"  \
	-overview overview.html	\
		$(SRC_HOME)/org/tango/hdb_configurator/configurator/*.java \
		$(SRC_HOME)/org/tango/hdb_configurator/diagnostics/*.java  \
		$(SRC_HOME)/org/tango/hdb_configurator/common/*.java
	java -classpath $(POGO) pogo.make_util.ReleaseNote2html -html  "HDB++ Configurator Release Note"
	@mv ReleaseNote.html $(DOC_DIR)



HDB_DIR=hdb++-configurator
install_doc:
ifndef DOC_PATH
		@echo "DOC_PATH is not set !"
else
		@echo "Install documentation in  $(DOC_PATH)/$(HDB_DIR)"
		@rm -f $(DOC_PATH)/$(HDB_DIR).old
		@mv $(DOC_PATH)/$(HDB_DIR)    $(DOC_PATH)/$(HDB_DIR).old
		@cp -R doc/build/html  $(DOC_PATH)/$(HDB_DIR)
endif
