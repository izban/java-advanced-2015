rm -rf javadoc
mkdir javadoc

javadoc -d javadoc -private -link http://docs.oracle.com/javase/8/docs/api/ -cp java:lib/hamcrest-core-1.3.jar:lib/junit-4.11.jar:lib/quickcheck-0.6.jar: `find java -iname '*.java'`
