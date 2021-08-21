---
title: Setup
has_children: false
nav_order: 3
---


## Building from Source

LSQ requires Maven to build.

### Building the Jar Bundle (jar-with-dependencies)

```bash
mvn -P bundle clean install

java -jar lsq-bundle/target/
```


### Building, Installing and Running the Debian Package

* You may first want to completely remove (i.e. purge) a prior installation
```
sudo apt-get purge lsq-cli
```

* The debian package is built with the `deb` profile
```bash
mvn -P debian clean install
```

* Install the deb package
```
sudo dpkg -i `find 'lsq-debian-cli/target/' -name 'lsq-cli_*.deb'`
```


