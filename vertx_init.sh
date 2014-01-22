#!/bin/sh

DIRNAME=$(dirname $0)
cd ${DIRNAME}
DIRNAME=${PWD}
pwd

# Export VERTX_MODS to use target/mods
#export VERTX_MODS=${DIRNAME}/containers/vertx/mods

# Run init in each module to create module.link files
cd ${DIRNAME}/containers/vertx/vertx-mod-persistence
mvn vertx:init

cd ${DIRNAME}/containers/vertx/vertx-mod-persistence-cassandra
mvn vertx:init
