java:
	javac Main.java && java Main
.PHONY: java
j: java
.PHONY: j

clj:
	clj main.clj
.PHONY: clj
c: clj
.PHONY: c
