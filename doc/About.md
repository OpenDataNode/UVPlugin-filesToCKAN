### Description

Loads files to the specified CKAN instance

### Configuration Parameters

| Name | Description |
|:----|:----|
|**CKAN resource name** | Resource name to create in CKAN, this has precedence over input from e-distributionMetadata, and if even that is not set, it will use VirtualPath or symbolic name as resource name|
|**Use file name as CKAN resource name** | If checked, file name is used as resource name in CKAN. Must be checked if multiple files on input, otherwise DPU fails|
|**Overwrite existing resources** | If checked, existing resources are overwritten|

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|filesInput        |i| FilesDataUnit| File loaded to specified CKAN instance|x|
|distributionInput |i| RDFDataUnit| Distribution metadata produced by e-distributionMetadata||