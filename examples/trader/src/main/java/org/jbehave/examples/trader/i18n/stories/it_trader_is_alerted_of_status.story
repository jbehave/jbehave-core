Scenario: 
Per assicurare una risposta rapida
In qualità di trader che parla italiano
Voglio controllare i prezzi delle azioni

Dato che ho un'azione con simbolo STK1 e una soglia di 15.0
Quando l'azione e' scambiata al prezzo di 5.0
Allora lo status di allerta e' OFF
Quando l'azione e' scambiata al prezzo di 11.0
Allora lo status di allerta e' OFF
Quando l'azione e' scambiata al prezzo di 16.0
Allora lo status di allerta e' ON
Quando l'azione e' scambiata al prezzzzzzzzo di 20.0
Allora lo status di allerta e' ON

Scenario:

Dato che ho una tabella
!!uno!!due!!
!11!12!
!21!22!
Allora la tabella ha 2 righe
E alla riga 1 e colonna uno troviamo: 11
E alla riga 1 e colonna due troviamo: 12
E alla riga 2 e colonna uno troviamo: 21
E alla riga 2 e colonna due troviamo: 22
