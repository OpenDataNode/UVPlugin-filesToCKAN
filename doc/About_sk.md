### Popis

Nahrá súbory do zadanej inštancie CKAN

### Konfiguračné parametre

| Meno | Popis |
|:----|:----|
|**Názov zdroja CKAN** | Názov zdroja vytvoreného v CKAN, má prednosť pred vstupom z e-distributionMetadata, a aj v prípade, ak nie je zadaný, použije virtuálnu cestu alebo symbolické meno ako názov zdroja |
|**Použije názov súboru ako CKAN meno** | Ak je checkbox aktívny, názov súboru je použitá ako meno zdroja v CKAN. Musí byť aktívny v prípade viacerých súborov na vstupe, inak DPU zlyhá |
|**Prepísať existujúce zdroje** | Ak je checkbox aktívny, existujúce zdroje sú prepísané |

### Vstupy a výstupy ###

|Meno |Typ | Dátová hrana | Popis | Povinné |
|:--------|:------:|:------:|:-------------|:---------------------:|
|distributionInput |i| RDFDataUnit| Distribučné metadáta z e-distributionMetadata ||