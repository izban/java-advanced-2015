#!/bin/bash
cd java/examples/rmi
export CLASSPATH=../..

java examples.rmi.Client $@
