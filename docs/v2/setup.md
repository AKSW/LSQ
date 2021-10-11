---
title: Setup
has_children: false
nav_order: 3
---

## Building from Source

LSQ requires Maven to build.

In order to reduce build times for specific tasks there exist different profiles.
The following table summarizes those profiles:


| Build task                               | Required profile(s) | Example                               |
|------------------------------------------|---------------------|---------------------------------------|
| Libraries only                           | (none)              | `mvn clean install`                   |
| Jar bundle (uber jar)                    | dist                | `mvn -Pdist clean install`            |
| Embed spark                              | standalone          | `mvn -Pdist,standalone clean install` |
| Debian package (implies dist,standalone) | deb                 | `mvn -Pdeb clean install`             |

If you want a self-contained runnable jar file then build lsq using:
```bash
mvn -Pdist,standalone clean install
```

### Building the Jar Bundle (jar-with-dependencies)

The jar bundle contains a main class manifest and can thus be run with `java -jar`:
```bash
mvn -Pdist,standalone clean install

java -jar lsq-cli/target/lsq-cli-${VERSON}-jar-with-dependencies.jar
```
(Don't forget to substitute `${VERSION}` with its proper value)


### Building, Installing and Running the Debian Package

Installing the debian package makes the `lsq` command available (located at `/usr/bin/lsq`).


* You may first want to completely remove (i.e. purge) a prior installation
```
sudo apt-get purge lsq-cli
```

* The debian package is built with the `deb` profile
```bash
mvn -Pdeb clean install
```

* Install the deb package
The convenience script `./reinstall-deb.sh` searches for generated debian packages and installs them.
It assumes that only a single built package exists.

```bash
./reinstall-deb.sh

```

The script is a shorthand for:

```
sudo dpkg -i `find 'lsq-debian-cli/target/' -name 'lsq-cli_*.deb'`
```


