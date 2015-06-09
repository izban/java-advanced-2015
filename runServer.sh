#!/bin/bash
cd java/examples/rmi
export CLASSPATH=../..

rmiregistry &
java examples.rmi.Server
