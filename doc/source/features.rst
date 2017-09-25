.. This file is source for HDB++ contexts and strategies documentation 


Strategies and Contexts
=======================

Contexts
---------
    * A context could be seen like a state of the controlled equipments.
    * For instance the contexts could be:
 
        +--------------+------------------------+
        | Shutdown     | Equipments are stopped |
        +--------------+------------------------+
        | Run          | Equipments are running |
        +--------------+------------------------+
        | Tests        | Equipments are in test |
        +--------------+------------------------+
        | . . .        | Equipments are . . .   |
        +--------------+------------------------+

    * The system provides the context **ALWAYS**


Strategies
-----------
    * A strategy defines a context or several contexts when the attribute must be stored.
    * For instance if the strategy is **Run** the attribute will be stored when the context will be **Run** and not stored otherwise.
    * If the strategy is **Run | Tests** the attribute will be stored when the context will be **Run** or **Tests**, and not stored otherwise.
    
    
TTL (Time To Live)
===================

    * The HDB++ storage is normally done forever.
    * But a feature name TTL provides to specify a period for the data. After this period the data will be deleted.
        It could be useful for sepecific tests or 