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
|**CKAN resource name** |Resource name to create in CKAN  |
|**Use file name as CKAN resource name** |If checked, file name is used as resource name in CKAN. Must be checked if multiple files on input, otherwise DPU fails  |
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
|1.2.0-SNAPSHOT              | Added possibility to define custom HTTP headers using dpu.uv-l-filesToCkan.http.header.key = value in UV configuration file |
|1.1.0              | Added possibility to name CKAN resource when one file on input |
|                   | Changes in DPU API v 2.1.0, new actor ID parameter is sent to CKAN if available |
|1.0.1              | bug fixes and update in build dependencies |
|1.0.0              | First release                                   |


***

### Developer's notes ###

|Author            |Notes                 |
|------------------|----------------------|
|N/A               |N/A                   |

