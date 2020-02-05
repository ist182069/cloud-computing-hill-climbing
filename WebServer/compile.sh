export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS

rm *.class
rm pt/ulisboa/tecnico/cnv/solver/*.class
rm pt/ulisboa/tecnico/cnv/util/interpolator/*.class
rm pt/ulisboa/tecnico/cnv/util/*.class
#javac -cp .:~/BIT/:~/BIT/: pt/ulisboa/tecnico/cnv/server/*.java
javac -cp .:~/BIT/:~/BIT/: pt/ulisboa/tecnico/cnv/solver/*.java
javac -cp .:~/BIT/:~/BIT/: pt/ulisboa/tecnico/cnv/util/interpolator/*.java
javac -cp .:~/BIT/:~/BIT/: pt/ulisboa/tecnico/cnv/util/*.java

javac -cp .:~/:BIT/:aws-java-sdk-1.11.538/lib/aws-java-sdk-1.11.538.jar:aws-java-sdk-1.11.538/third-party/lib/* Dynamo.java
javac -cp .:~/:BIT/:aws-java-sdk-1.11.538/lib/aws-java-sdk-1.11.538.jar:aws-java-sdk-1.11.538/third-party/lib/* WebServer.java
javac -cp .:~/:BIT/:aws-java-sdk-1.11.538/lib/aws-java-sdk-1.11.538.jar:aws-java-sdk-1.11.538/third-party/lib/* Metrics.java
javac -cp .:~/:BIT/:aws-java-sdk-1.11.538/lib/aws-java-sdk-1.11.538.jar:aws-java-sdk-1.11.538/third-party/lib/* MetricsCollector.java

java -cp .:~:BIT MetricsCollector pt/ulisboa/tecnico/cnv/solver/SolverMain.class
java -cp .:~:BIT MetricsCollector pt/ulisboa/tecnico/cnv/solver/Coordinate.class
java -cp .:~:BIT MetricsCollector pt/ulisboa/tecnico/cnv/solver/AStarStrategy.class
java -cp .:~:BIT MetricsCollector pt/ulisboa/tecnico/cnv/solver/BFSStrategy.class
java -cp .:~:BIT MetricsCollector pt/ulisboa/tecnico/cnv/solver/DFSStrategy.class

java -cp .:~/:BIT/:aws-java-sdk-1.11.538/lib/aws-java-sdk-1.11.538.jar:aws-java-sdk-1.11.538/third-party/lib/* WebServer
