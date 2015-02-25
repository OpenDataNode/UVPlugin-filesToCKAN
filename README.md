# L-FilesToCkan #
----------

###General###

|                              |                                                               |
|------------------------------|---------------------------------------------------------------|
|**Name:**                     |L-FilesToCkan                                             |
|**Description:**              |Loads files to the specified CKAN instance. |
|                              |                                                               |
|**DPU class name:**           |FilesToCkan     | 
|**Configuration class name:** |FilesToCkanConfig_V1                           |
|**Dialogue class name:**      |FilesToCkanVaadinDialog | 

***

###Configuration parameters###


|Parameter                        |Description                             |                                                        
|---------------------------------|----------------------------------------|
|**secret_token** |Token used to authenticate to CKAN, has to be set in backend.properties  |
|**catalogApiLocation** | URL where CKAN api is located, has to be set in backend.properties |

***

### Inputs and outputs ###

|Name                |Type       |DataUnit                         |Description                        |
|--------------------|-----------|---------------------------------|-----------------------------------|
|filesInput |i |FilesDataUnit |File loaded to specified CKAN instance/  |

***

### Version history ###

|Version            |Release notes                                   |
|-------------------|------------------------------------------------|
|1.3.0              |N/A                                             |                                


***

### Developer's notes ###

|Author            |Notes                 |
|------------------|----------------------|
|N/A               |N/A                   | 

