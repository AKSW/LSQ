CWD = $(shell pwd)

POM = -f pom.xml
# Maven Clean Install Skip ; skip tests, javadoc, scaladoc, etc
MS = mvn -DskipTests -Dmaven.javadoc.skip=true -Dskip
MCIS = $(MS) clean install
MCCS = $(MS) clean compile

VER = $(error specify VER=releasefile-name e.g. VER=1.9.7-rc2)
loud = echo "@@" $(1);$(1)

# Source: https://stackoverflow.com/questions/4219255/how-do-you-get-the-list-of-targets-in-a-makefile
.PHONY: help

.ONESHELL:
help:   ## Show these help instructions. Use [make <goal> ARGS='-U'] to pass maven args.
	@sed -rn 's/^([a-zA-Z_-]+):.*?## (.*)$$/"\1" "\2"/p' < $(MAKEFILE_LIST) | xargs printf "make %-20s# %s\n"

distjar: ## Create only the standalone jar-with-dependencies of rpt
	$(MCCS) $(POM) package -Pstandalone,dist -pl :lsq-pkg-uberjar-cli -am $(ARGS)
	file=`find '$(CWD)/lsq-pkg-parent/lsq-pkg-uberjar-cli/target' -name '*-jar-with-dependencies.jar'`
	printf '\nCreated package:\n\n%s\n\n' "$$file"

rpm-rebuild: ## Rebuild the rpm package (minimal build of only required modules)
	$(MCIS) $(POM) -Prpm -am -pl :lsq-pkg-rpm-cli $(ARGS)

rpm-reinstall: ## Reinstall rpm (requires prior build)
	@p1=`find lsq-pkg-parent/lsq-pkg-rpm-cli/target | grep '\.rpm$$'`
	sudo rpm -U "$$p1"

rpm-rere: rpm-rebuild rpm-reinstall ## Rebuild and reinstall rpm package


deb-rebuild: ## Rebuild the deb package (minimal build of only required modules)
	$(MCIS) $(POM) -Pdeb -am -pl :lsq-pkg-deb-cli $(ARGS)

deb-reinstall: ## Reinstall deb (requires prior build)
	@p1=`find lsq-pkg-parent/lsq-pkg-deb-cli/target | grep '\.deb$$'`
	sudo dpkg -i "$$p1"

deb-rere: deb-rebuild deb-reinstall ## Rebuild and reinstall deb package


docker: ## Build Docker image
	$(MCIS) $(POM) -am -pl :lsq-pkg-docker-cli $(ARGS)
	cd lsq-pkg-parent/lsq-pkg-docker-cli && $(MS) jib:dockerBuild && cd ../..

selftest: ## Self-test. Requires lsq command and sparql endpoint under localhost:8890/sparql
	inputLog="lsq-core/src/test/resources/logs/issue54.combined.log"
	rdfLog="/tmp/lsq.selftest.log.trig"
	benchConf="/tmp/lsq.selftest.bench.conf.ttl"
	runConf="/tmp/lsq.selftest.run.conf.ttl"
	lsq rx probe "$$inputLog"
	lsq rx rdfize -e http://server.from/which/the/log/is/from "$$inputLog" > "$$rdfLog"
	lsq rx benchmark create -d myDatasetLabel -e http://localhost:8890/sparql -o > "$$benchConf"
	lsq rx benchmark prepare -c "$$benchConf" -o > "$$runConf"
	lsq rx benchmark run -c "$$runConf" "$$rdfLog"

release-bundle: ## Create files for Github upload
	@set -eu
	ver=$(VER)
	$(call loud,$(MAKE) deb-rebuild)
	p1=`find lsq-pkg-parent/lsq-pkg-deb-cli/target | grep '\.deb$$'`
	$(call loud,cp "$$p1" "rpt-$${ver/-/\~}.deb")
	$(call loud,$(MAKE) rpm-rebuild)
	p1=`find lsq-pkg-parent/lsq-pkg-rpm-cli/target | grep '\.rpm$$'`
	$(call loud,cp "$$p1" "rpt-$$ver.rpm")
	$(call loud,$(MAKE) distjar)
	file=`find '$(CWD)/lsq-pkg-parent/lsq-pkg-uberjar-cli/target' -name '*-jar-with-dependencies.jar'`
	$(call loud,cp "$$file" "rpt-$$ver.jar")
	$(call loud,$(MAKE) docker)
	$(call loud,docker tag aksw/rpt aksw/rpt:$$ver)
	$(call loud,gh release create v$$ver "rpt-$${ver/-/\~}.deb" "rpt-$$ver.rpm" "rpt-$$ver.jar")
	$(call loud,docker push aksw/rpt:$$ver)
	$(call loud,docker push aksw/rpt)

