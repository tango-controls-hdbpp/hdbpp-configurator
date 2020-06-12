#+======================================================================
#
# Project:      Configurator and diagnotics for Tango HDB++
#
# Description:  Makefile to generate the JAVA Tango classes documentation
#
# Author:       Pascal Verdier
# copyright:    European Synchrotron Radiation Facility
#               BP 220, Grenoble 38043
#               FRANCE
#
#-======================================================================

all:


SRC_HOME=src/main/java
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
        $(SRC_HOME)/org/tango/hdb_configurator/statistics/*java \
        $(SRC_HOME)/org/tango/hdb_configurator/common/*.java



HDB_DIR=hdb++-configurator
install_doc:
ifndef DOC_PATH
		@echo "DOC_PATH is not set !"
else
		@echo "Install documentation in  $(DOC_PATH)/$(HDB_DIR)"
		@rm -rf $(DOC_PATH)/$(HDB_DIR).old
		@mv $(DOC_PATH)/$(HDB_DIR)    $(DOC_PATH)/$(HDB_DIR).old
		@mkdir $(DOC_PATH)/$(HDB_DIR)
		@cp -R doc/*  $(DOC_PATH)/$(HDB_DIR)
endif
