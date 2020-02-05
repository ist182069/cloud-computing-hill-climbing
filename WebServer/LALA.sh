

export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS

javac -cp .:~/BIT/:~/BIT/: pt/ulisboa/tecnico/cnv/server/*.java
javac -cp .:~/BIT/:~/BIT/: pt/ulisboa/tecnico/cnv/solver/*.java
javac -cp .:~/BIT/:~/BIT/: pt/ulisboa/tecnico/cnv/util/interpolator/*.java
javac -cp .:~/BIT/:~/BIT/: pt/ulisboa/tecnico/cnv/util/*.java

javac -cp .:~/:BIT pt/ulisboa/tecnico/cnv/server/WebServer.java
javac -cp .:~/:BIT Metrics2.java

java -cp .:~:BIT Metrics2 pt/ulisboa/tecnico/cnv/solver/AStarStrategy.class
java -cp .:~:BIT Metrics2 pt/ulisboa/tecnico/cnv/solver/BFSStrategy.class
java -cp .:~:BIT Metrics2 pt/ulisboa/tecnico/cnv/solver/DFSStrategy.class

java -cp .:~:BIT pt.ulisboa.tecnico.cnv.server.WebServer

