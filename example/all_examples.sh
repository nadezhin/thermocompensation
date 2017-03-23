#!/bin/sh

java -ea -Djna.library.path=../lib -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Application2 P -g -manuf > P/m.log
mv P/Plot P/Plotm
java -ea -Djna.library.path=../lib -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Application2 P -g -spec  > P/s.log
mv P/Plot P/Plots

./run_example.sh 150601V15
./run_example.sh 160202-GOLD-40+70
./run_example.sh 160914-29-perepay-2
./run_example.sh 161002-28-ost

