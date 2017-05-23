.. HDB++ Tools documentation master file, created by
   sphinx-quickstart on Tue Feb 28 13:23:46 2017.

   
.. Definitions
.. ------------

.. _TANGO:      http://www.tango-controls.org/
.. _HDB++:      http://www.tango-controls.org/community/projects/hdbplus/
.. _Cassandra:  http://cassandra.apache.org/
.. _MySql:      http://www.mysql.com/

.. |TangoLogo| image::  images/logo_tangocontrols.png
                        :scale: 80 %
   



   
`HDB++`_ Tools documentation
==============================

+--------------------------------------------------------+--------------+
| * `HDB++`_ is the TANGO_ History Database.             | |TangoLogo|  |
| * It can be used with Mysql_ or Cassandra_ database.   |              |
| * It is based on TANGO_ archive events.                |              |
+--------------------------------------------------------+--------------+

Contents:

.. toctree::
   :maxdepth: 4

   introduction
   configurator
   diagnostics
   strategies
   
.. warning::
    * Since release 2.0 the GUI manage the storage `strategy based on contexts <./strategies.html>`_.
    * It is compatible only with a release of HdbConfigurationManager and HdbEventSubscriber managing strategies too.
    
For classes information, see `Programmer References <prg_references/index.html>`_
