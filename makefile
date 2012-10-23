# Knight Guan
# RoboCup2011 SEU_REDSUN

src := $(wildcard *.java  */*.java */*/*.java */*/*/*.java */*/*/*/*.java)
opt := -d .
path := -Djava.ext.dirs=./lib

all:
	javac -Xlint:unchecked $(path) $(opt) $(src)

.PHONY: clean

clean:
	rm -f *.class  */*.class  */*/*.class  */*/*/*.class  */*/*/*/*.class
	rm -f *~       */*~
	rm -r test-results/
