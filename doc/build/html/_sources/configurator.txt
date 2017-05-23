.. This file is source for HDB++ configurator documentation 


Configurator
==============

The configurator is separated in two parts
    - **Left side:** a tree (jive like) to select attributes to be stored.
    - **Right side:** a list of attributes managed by the selected archiver.
         This list contains attribute name and its strategy.

Adding attribute
-----------------
    * An attribute could be simply added to a subscriber by a simple selection in a JTree (jive like).
        use double click or **+** button
    * Select the expected archiver to assign the attribute.
    * Select the expected strategy.
    * Set the archiving event properties if needed or set the Event push by code button if it the case.   
    
    .. image:: images/AddAttribute.png

    
    
    
    
Adding several attributes
--------------------------
    * You can select several attributes.
    * To add them, just click on **+** button
    * You will not be able to set the archiving event properties (could be different for each attribute).
    * But you will be able to select the archiver, strategy and to Event push by code button if it is the case. 

    .. image:: images/AddAttributes.png

    
    
    
    
    
    
Adding a large number of attributes
------------------------------------

Two ways to add a large list of attribute

By loading a file
^^^^^^^^^^^^^^^^^^
        * By loading a file containing a list of attribute names.
            Use menu **File / Open** and select your file containing only an attribute list.

By code
^^^^^^^^
        * By code, using classes:
            * `org.tango.hdb_configurator.hdb_configurator.ManageAttributes <prg_references/org/tango/hdb_configurator/configurator/ManageAttributes.html>`_
            * `org.tango.hdb_configurator.configurator.HdbAttributes <prg_references/org/tango/hdb_configurator/configurator/HdbAttribute.html>`_
            
            .. literalinclude:: examples/MyAttributeManagement.java

            For more information, see `Programmer References <prg_references/index.html>`_

    
    
    
    
    
Start/Stop/Remove,....
------------------------
    * When attributes have been added to an archiver, you can easily:
        - Change strategy
        - Stop or pause archiving
        - Remove attributes
        - Change the archiver
    
    .. image:: images/StopAttribute.png

    
    
    
    
    
Multiple TANGO_HOST
--------------------
    * Start the Configurator tool with the same TANGO_HOST environment variable your HDB++ servers.
        Export an environment variable *HdbManager** with the configurator device name
            e.g. : HdbManager=tango/hdb/manager 

        By default, your JTree will represent this control system.
        You will be available to change this TANGO_HOST by a right click on root.
        Change the new TANGO_HOST and click OK.

    .. image:: images/ChangeTangoHost.png

        
    * Another way to work on 2 different control systems could be done by exporting
      another environment variable **EVENT_TANGO_HOST**
      
      **e.g. :** *EVENT_TANGO_HOST=id32:20000*

      The control system tree will represent this second control system at startup.

      
      
      
      
        
        
Archiver labels
----------------
    * By a free property **HdbConfigurator / ArchiverLabels** you can define a label each archiver.
        It will be used by the configurator tool to display information.
        If this property is not defined the GUI propse the archiver device name.

        
Using Jive
^^^^^^^^^^^        
    .. image:: images/ArchiverLabels.png
    
    
Using the Configuartor tool
^^^^^^^^^^^^^^^^^^^^^^^^^^^^    
    +-----------------------------------------+-----------------------------------------+
    | .. image:: images/ArchiverLabels-2.png  | .. image:: images/ArchiverLabels-3.png  |
    |    :scale: 60 %                         |    :scale: 75 %                         |
    +-----------------------------------------+-----------------------------------------+

    
    
    
    
    
    
Defining contexts
------------------
    * Use **Tools / Manage Strategies and Contexts** menu.
    * Add or remove contexts with descriptions.
    * Select the default context.
    
    .. image:: images/Contexts.png
    
    
Manage attribute strategy
-------------------------

By archiver
^^^^^^^^^^^
    * In the right side of the tool, the list of attributes managed by the selected archiver display the attribute name and its strategy.
    * You can change this strtegy using right click menu.
    * Then select the context(s) defining the new attribute strategy.
    
    .. Note:: If you click on head of column, the context will be selected for all attributes.

    .. image:: images/ChangeStrategy.png


By strategy
^^^^^^^^^^^
    * Use **Tools / Manage attributes by strategies** menu.
    * Attributes will be sorted by strategis.
    * To change strategy, you can select:
        - attribute(s)
        - members (all attributes of selected members)
        - families (all attributes of all members of selected families)
    * Then click on **Change Selection Strategy** and select new strategy as by archiver.
    
    .. image:: images/ByStrategy.png
    
    
    