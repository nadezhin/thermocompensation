#!/bin/sh

java -ea -Djna.library.path=../lib -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Application2 $1 -s1 -g -manuf > $1/s1-m.log
mv $1/Plot1 $1/Plot1m
java -ea -Djna.library.path=../lib -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Application2 $1 -s1 -g -spec  > $1/s1-s.log
mv $1/Plot1 $1/Plot1s
java -ea -Djna.library.path=../lib -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Application2 $1 -s2 -g -manuf > $1/s2-m.log
mv $1/Plot2 $1/Plot2m
java -ea -Djna.library.path=../lib -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Application2 $1 -s2 -g -spec  > $1/s2-s.log
mv $1/Plot2 $1/Plot2s

