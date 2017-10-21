# An experiment with forking the existing process

To run:
1. `mvn clean package`
2. `java -Xmx<a value higher than memory available> -jar target/vfork-experiment-1.0-SNAPSHOT.jar <time to run> <number of forks>`
