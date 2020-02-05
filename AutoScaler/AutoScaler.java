import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;

import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.net.InetSocketAddress;
import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;

/**
* Classe que cria e elimina web servers
 */
public class AutoScaler {

    static AmazonEC2      ec2;
    static ArrayList<String> instances = new ArrayList<String>();

    public static void main(String[] args) throws Exception {
        instances.add("i-0eadaea6eba38c194");
        init();
    	final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/getInstance", new ResolveInstance());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Endereco do AutoScaler: " + server.getAddress().toString());
	}

    /**
    * ================================ Comunicacao
     */
    static class ResolveInstance implements HttpHandler {
        
        @Override
        public void handle(HttpExchange t) throws IOException {
            try{

                System.out.println("An instance was requested");

                //String query = t.getRequestURI().getQuery();
                //System.out.println("> Query:\t" + query);

                //Request request = createRequest(query);
                  
                byte[] response = null;

                String instanceDNS = resolveInstance();
                System.out.println("DNS encontrado (handle): " + instanceDNS);
                response = instanceDNS.getBytes();

                t.sendResponseHeaders(200, response.length);

                OutputStream os = t.getResponseBody();              
                os.write(response);
                os.close();

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
    * ========================= Comandos
    */

    // cria uma nova instancia e guarda no array
    private static void newInstance() throws Exception {
    	String instanceId = launchInstance();
    	System.out.println("ID da nova intancia: " + instanceId);
    	instances.add(instanceId);
    }

    // termina uma instancia e retira do array
    private static void killInstance(String instanceId) throws Exception {
        terminateInstance(instanceId);
        instances.remove(instanceId);
        System.out.println("Instancia " + instanceId + " retirada");
        checkArrayInstances();
    }

    // procura uma instancia adequada para o pedido e retorna o seu DNS
    public static String resolveInstance() {
        for(String s : instances) {
            System.out.println("InstanceID: " + s);
            return getInstancePublicDnsName(s);
        }
        return "";
    }

    /**
    * ========================== Auxiliares
    */

    // Inicializa ec2
    private static void init() throws Exception {

        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
            if(credentials == null) {
                System.out.println("esta a null");
            }
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    // cria uma nova instancia
    private static String launchInstance() throws Exception{
        System.out.println("===========================================");
        System.out.println("Preparing to Launch a new instance...");

            init();
        

        try {
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
                    " Availability Zones.");
            /* 
             * using AWS USA. 
             */

            System.out.println("Starting a new instance...");
            RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

            runInstancesRequest.withImageId("ami-01dbc6e9d788383c0")
                               .withInstanceType("t2.micro")
                               .withMinCount(1)
                               .withMaxCount(1)
                               .withKeyName("CHAVESEMPRECERTA")
                               .withSecurityGroups("CNV-ssh+http");

            RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
            String newInstanceId = runInstancesResult.getReservation().getInstances()
                                      .get(0).getInstanceId();

        
            checkInstancesRunning();

            System.out.println("A new instance is running!");
            System.out.println("===========================================");
                                        
            return newInstanceId;
            
        } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }

        return null;
    }

    // termina uma instancia
    private static void terminateInstance(String newInstanceId) throws Exception {
        System.out.println("===========================================");
        System.out.println("Terminating the instance...");
        
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(newInstanceId);
        ec2.terminateInstances(termInstanceReq); 

        System.out.println("Instance terminanted!");
        System.out.println("===========================================");
    }

    // retorna o DNS publico da instancia com id instanceId
    public static String getInstancePublicDnsName(String instanceId) {
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        Set<Instance> allInstances = new HashSet<Instance>();
        
        for (Reservation reservation : reservations) {
          for (Instance instance : reservation.getInstances()) {
            if (instance.getInstanceId().equals(instanceId)) {
                System.out.println("DNS encontrado: " + instance.getPublicDnsName());
                return instance.getPublicDnsName();
            }
          }
        }
        
        return null;
    }

    /**
    * ========================== Debug
    */

    // verifica quantas instancias estao a correr no momento
    private static void checkInstancesRunning() {
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        Set<Instance> instances = new HashSet<Instance>();

        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }

        System.out.println("You have " + instances.size() + " Amazon EC2 instance(s) running.");
    } 

    // imprime o conteudo do array instances
    private static void checkArrayInstances() {
    	for(String s : instances) {
    		System.out.println("InstanceID: " + s);
    	}
    }

}