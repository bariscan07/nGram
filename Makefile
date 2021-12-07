JAVAC=/usr/lib/jvm/java-14-openjdk/bin/javac
JAVA=/usr/lib/jvm/java-14-openjdk/bin/java

run: nGramTest.class
	$(JAVA) nGramTest

nGramTest.class: nGramTest.java
	$(JAVAC) nGramTest.java
