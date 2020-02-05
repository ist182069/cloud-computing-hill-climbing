export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS

rm *.class
javac -cp .:~/::aws-java-sdk-1.11.534/lib/aws-java-sdk-1.11.534.jar:aws-java-sdk-1.11.534/third-party/lib/* Metrics.java Request.java LoadBalancer.java LoadBalancerCommands.java Dynamo.java LBandAS.java
java -cp .:~/:BIT/:aws-java-sdk-1.11.534/lib/aws-java-sdk-1.11.534.jar:aws-java-sdk-1.11.534/third-party/lib/* LBandAS
