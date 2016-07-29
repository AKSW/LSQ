#!/bin/sh

sudo apt-get purge lsq-cli
sudo dpkg -i `find 'lsq-debian-cli/target/' -name 'lsq-cli_*.deb'`

