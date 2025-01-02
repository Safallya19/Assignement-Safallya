In the repo Assignement-Safallya

please find respective program under package :- wolf

   client program :- DirectoryMonitorClient

   Server program :- MultiClientServer

 

   config file for server and client are under config folder:- config

    client config file:- client-config.properties

    server config file:- server-config.properties

 

    1) client config file:-

    directoryPath :- The directory where any files need to be monitor.

    keyPattern :- The key pattern that will accept the key pattern from file.

                  ^[a-zA-Z_]+$ : Accepts any string with under score , will not accept numbers.

    serverAddress:- server address for client program as it was running in local so 'localhost'   

    serverPort:- Port number   

 

 

    2)  server config file:-

 

    serverAddress=server address for client program as it was running in local so 'localhost

    serverPort=Port number

    maxThreads=For multi threading the thread count.

 

 

    How to build the program and run :-

 

    1) setup the config(client & server) file path in the program arugment of IDE before compling or running the program.

    2) Create the directory in the local or remote and update the path in the client config file(client-config.properties) attribute 'directoryPath'

    3) There are test file provided for testing the program test-2.txt and test-file.txt

    4) Once the program is up and running copy & paste the file in the monitoring directory.

    5) The output file will be created the program classpath directory.

   
