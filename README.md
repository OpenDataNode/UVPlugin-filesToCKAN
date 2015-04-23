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

###Dialog configuration parameters###


|Parameter                        |Description                             |                                                        
|---------------------------------|----------------------------------------|
|**Overwrite existing resources** |If checked, existing resources are overwritten  |

***

###Configuration parameters###
|Parameter                             |Description                             |                                                        
|--------------------------------------|----------------------------------------|
|**dpu.uv-l-filesToCkan.secret.token**    |Token used to authenticate to CKAN, has to be set in backend.properties  |
|**dpu.uv-l-filesToCkan.catalog.api.url** | URL where CKAN api is located, has to be set in backend.properties |

***

### Inputs and outputs ###

|Name                |Type       |DataUnit                         |Description                        |
|--------------------|-----------|---------------------------------|-----------------------------------|
|filesInput |i |FilesDataUnit |File loaded to specified CKAN instance/  |

***

### Version history ###

|Version            |Release notes                                   |
|-------------------|------------------------------------------------|
|1.0.0              |First release                                   |


***

### Developer's notes ###

|Author            |Notes                 |
|------------------|----------------------|
|N/A               |N/A                   | 

