#!/bin/sh

java -ea -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Csv P
java -ea -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Csv 150601V15 -s1
java -ea -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Csv 150601V15 -s2
java -ea -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Csv 160202-GOLD-40+70 -s1
java -ea -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Csv 160202-GOLD-40+70 -s2
java -ea -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Csv 160914-29-perepay-2 -s1
java -ea -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Csv 160914-29-perepay-2 -s2
java -ea -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Csv 161002-28-ost -s1
java -ea -cp ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar ru.nsc.interval.thermocompensation.intopt.Csv 161002-28-ost -s2

