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
|**org.opendatanode.CKAN.secret.token**    |Token used to authenticate to CKAN, has to be set in backend.properties  |
|**org.opendatanode.CKAN.api.url** | URL where CKAN api is located, has to be set in backend.properties |
|**org.opendatanode.CKAN.http.header.[key]** | Custom HTTP header added to requests on CKAN |

####Deprecated parameters###

These parameters are deprecated and kept only for backward compatibility with version 1.0.X.
They will be removed in 1.1.0 of DPU.

|Parameter                             |Description                             |
|--------------------------------------|----------------------------------------|
|**dpu.uv-l-filesToCkan.secret.token**    | alias to _org.opendatanode.CKAN.secret.token_  |
|**dpu.uv-l-filesToCkan.catalog.api.url** | alias to _org.opendatanode.CKAN.api.url_ |
|**dpu.uv-l-filesToCkan.http.header.[key]** | alias to org.opendatanode.CKAN.http.header.[key] |

####Examples####
```INI
org.opendatanode.CKAN.secret.token = 12345678901234567890123456789012
org.opendatanode.CKAN.api.url = ﻿http://localhost:9080/internalcatalog/api/action/internal_api
org.opendatanode.CKAN.http.header.X-Forwarded-Host = www.myopendatanode.org
org.opendatanode.CKAN.http.header.X-Forwarded-Proto = https
```

***

### Inputs and outputs ###

|Name                |Type       |DataUnit                         |Description                        |
|--------------------|-----------|---------------------------------|-----------------------------------|
|filesInput |i |FilesDataUnit |File loaded to specified CKAN instance/  |

***

### Version history ###

|Version            |Release notes                                   |
|-------------------|------------------------------------------------|
|1.0.2              | Added possibility to define custom HTTP headers |
|1.0.1              | bug fixes and update in build dependencies |
|1.0.0              | First release                                   |


***

### Developer's notes ###

|Author            |Notes                 |
|------------------|----------------------|
|N/A               |N/A                   |

