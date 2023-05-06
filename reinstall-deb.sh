#!/bin/sh

sudo apt-get purge lsq-cli
sudo dpkg -i `find 'lsq-pkg-parent/lsq-pkg-deb-cli/target/' -name 'lsq-cli_*.deb'`

