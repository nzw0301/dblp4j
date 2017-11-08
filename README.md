# DBLP4J

DBLP citation dataset parser by Java

## Run

```shell
$ mkdir -p src/main/resources
$ python download.py
$ gradle run
```

---

## About created files

### `papers.tsv`

Each line contains 3 fields:

1. paper id (0-origin index)
1. paper index (string, driven from original data)
1. conference name (string)

---
 
### `paper_conference_label.tsv`

Each line contains 2 fields:

1. paper index (int)
2. conference/journal name (string)

---

### `author_conference_label.tsv`

Each line contains 2 fields:

1. author name (string)
2. conference/journal names (string, each conference name is split by `TAB` )

---

### `paper_citation_edge.tsv`

Each line contains 2 fields:

1. source paper id (int)
2. target paper id (int)  

Each line represents a directed edge.

---

### `author_citation_edge.tsv`

Each line contains 2 fields:

1. source author name (string)
2. target author name (string)
3. the number of citations from source author (int)

Each line represents a weighted and directed edge.
