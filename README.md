# thermocompensation
Application of interval optimization to thermocompensation

Build instruction:
cd ${THERMOCOMPENSATION}/java
mvn clean install

File example/P.csv contains data for 8 devices.
It contains 4 columns:
- chipNo    device id;
- temp      temperature sensor;
- out       required output of polynom evaluator
- freq      measured frequency used only for output

Run heuristic optimization:
cd ${THERMOCOMPENSATION}/java/optim
mvn exec:exec

Expected output:
........
-------------
14:  62 16 125 103 2 22 7 0 0 0 7 # f = 12000000 +- 9
7:  63 17 80 124 8 26 5 0 0 0 7 # f = 12000000 +- 14
9:  63 16 88 125 1 24 8 0 0 0 7 # f = 12000000 +- 20
18:  63 25 80 68 1 25 2 0 0 0 7 # f = 12000000 +- 17
12:  57 16 121 97 5 22 6 0 0 0 7 # f = 12000000 +- 7
11:  53 14 122 126 2 28 14 0 0 0 7 # f = 12000000 +- 13
4:  57 18 86 105 6 30 6 0 2 0 7 # f = 12000000 +- 11
20:  63 17 107 111 8 30 6 0 4 0 7 # f = 12000000 +- 7
chipNo: coefficients # f = freq +- delta
- chipNo        device id;
- coefficients  coefficients returned by optimization
- freq          average frequency
- delta         maximal delta between requred output and
                actual output for found coefficients.

Run interval optimization:
cd ${THERMOCOMPENSATION}/java/intopt
mvn exec:exec

Maven is not necessary to run interval optimization.
The result jar is ${THERMOCOMPENSATION}/java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar

Test it on short test:
cd ${THERMOCOMPENSATION}/example
java -ea -Djna.library.path=../lib -jar ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar P
Chips in ${THERMOCOMPENSATION}/example/P are optimized

If gnuplot is installed
cd ${THERMOCOMPENSATION}/example
java -ea -Djna.library.path=../lib -jar ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar P -g
Plots will be in
${THERMOCOMPENSATION}/example/P/Plot

Try long test
cd ${THERMOCOMPENSATION}/example
java -ea -Djna.library.path=../lib -jar ../java/intopt/target/intopt-0.1-SNAPSHOT-jar-with-dependencies.jar 150616V15 -s1 -e1.0 -g -p

