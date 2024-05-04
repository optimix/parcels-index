
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

The app can be run a second time to look search for given parcels.

# Pre-requisites

Install OpenJDK 21 and maven.

# Build the app

```sh
mvn clean install
```

# Run the app to generate `parcels-index.csv`

```sh
java -Xmx7G -jar target/parcels-index-0.1.jar
```

At the end, `parcels-index.csv` will be created.

# Find parcel IDs in `parcels-index.csv`

Prepare a file with parce IDs to look for - for example file `missing_parcel_ids_2023.txt`:
```text
01033458ZB0427
01053000AN0130
01071000AD0200
...
```

Split the `parcels-index.csv` in smaller files:
```text
split -l 20000000 parcels-index.csv parcels-index-part-
```

This will generate several files:
```text
parcels-index-part-aa
parcels-index-part-ab
parcels-index-part-ac
parcels-index-part-ad
parcels-index-part-ae
```

Find parcels from the `missing_parcel_ids_2023.txt` with a small script:
```bash
#!/usr/bin/env bash

echo -n "" > parcels-matches.csv

for part in parcels-index-part-*;do
  java -Xmx7G -jar target/parcels-index-0.1.jar missing_parcel_ids_2023.txt $part
done
```

At the end, `parcels-matches.csv` will be created.
