#!/bin/bash
cd java/examples/rmi
export CLASSPATH=../..

javac Server.java Client.java
rmic -d $CLASSPATH examples.rmi.AccountImpl examples.rmi.BankImpl examples.rmi.RemotePerson
