cd ../examples
javac -cp .:~/BIT/:~/BIT/samples/: *.java
cd ../samples
javac Metrics.java
java Metrics ../../pt/ulisboa/tecnico/cnv/solver/AStarStrategy.class ../../pt/ulisboa/tecnico/cnv/solver/output/AStarStrategy.class
cd ../examples/output/
echo "Running the output file..."
java Hello
cd ../../samples

