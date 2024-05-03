
# About

App classifying parcels by date and departments from the following inputs:
- https://files.data.gouv.fr/cadastre/etalab-cadastre/
- https://cadastre.data.gouv.fr/data/etalab-cadastre/

Generates the following output as `parcels-index.csv`:
```text
...
97611000BN0032,2024-01-01,976
97617000AH0219,2024-01-01,976
97617000AH0218,2023-10-01,976
97617000AH0217,2024-01-01,976
97617000AH0216,2024-01-01,976
```

The output is useful for finding the date and department for a given parcel ID to identify which file must be looked into for the parcel GeoJSON.

From a file `missing_parcel_ids_2023.txt` having some parcel IDs:
```sh
01033458ZB0427
01053000AN0130
01071000AD0200
01071000AP0084
```
we can look for a given parcel:
```bash
#!/usr/bin/env bash

echo "" > found_parcel_ids_2023.txt;

while read missing;do
  grep $missing parcels-index.csv >> found_parcel_ids_2023.txt;
done < missing_parcel_ids_2023.txt
```

# Pre-requisites

Install OpenJDK 21 and maven.

# Build the app

```sh
mvn clean install
```

# Run the app

```sh
java -Xmx7G -jar target/parcels-index-0.1.jar
```

At the end, `parcels-index.csv` will be created.
