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
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.InstanceState;
/**
* Classe que cria e elimina web servers
 */
public class AutoScaler {

    static final int MAX_INSTANCES = 10;
    static final long MAX_WORKLOAD = 100000L;
    private static AutoScaler autoScaler = null;
    static AmazonEC2      ec2;
    static ArrayList<String> instances = new ArrayList<String>();
    private static Map<String, Double> loadPerInstance = new HashMap<String, Double>();
    private static HashMap<String, Date> instanceNameDateMapping = new HashMap<String, Date>();

    private double totalWorkLoadSum = 0.0;
    private AutoScaler() {
        try {
            launchInstance li = new launchInstance();
            li.start();
            while(li.isAlive());
            LoadBalancer.callTimer();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static AutoScaler getInstance() {
        if(autoScaler == null) {
            autoScaler = new AutoScaler();
        }
        return autoScaler;
    } 
    
    public static void checkThreshold() {
        //TODO: trocar valores becuz esta menor 0.75 e 0.30
        System.out.println("ENTROU NO THRESHOLD");
    	Iterator it = loadPerInstance.entrySet().iterator();
        long cargaMaximaSistema = MAX_WORKLOAD * instances.size();
        double maxSuportado = cargaMaximaSistema * 0.40;
        double minSuportado = cargaMaximaSistema * 0.10;

        double cargaInst = 0;
        double cargaReal = 0;
        String zeroWorkloadInstance = "";
        while(it.hasNext()) {
    	    System.out.println("DORMI NO CARRO");
            Map.Entry pair = (Map.Entry)it.next();
            cargaInst = (Double) pair.getValue();
            cargaReal += cargaInst;
            if((int)cargaInst <= 0) {
                zeroWorkloadInstance = (String) pair.getKey();
            }
            System.out.println("Servidor:" + pair.getKey() + " | " + "Workload:" + pair.getValue() +
                 "|" + "LaunchTime: " + instanceNameDateMapping.get(""+pair.getKey()));
        }
        System.out.println("Carga Suportada: " + maxSuportado + "    Carga Atual: " + cargaReal);

        /*TODO: verificar periodo da instancia*/
        if((cargaReal < minSuportado) && (instances.size() > 1) && !zeroWorkloadInstance.equals("")){
        	long diff = Calendar.getInstance().getTime().getTime() - instanceNameDateMapping.get(zeroWorkloadInstance).getTime();
        	System.out.println("diff: " + diff);
        	long minutes = diff / (60 * 1000) % 60;
        	System.out.println("minutes: " + minutes);
        	if((minutes%60) >= 55) {
	            System.out.println("zero: " + zeroWorkloadInstance);
	            terminateInstance ti = new terminateInstance(zeroWorkloadInstance);
	            ti.start();
	        }
        } 
        else if((cargaReal >= maxSuportado)) {
            try {
                if(instances.size() < MAX_INSTANCES) {
                    newInstance();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
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
    public static void newInstance() throws Exception {
    	if(instances.size() < MAX_INSTANCES) {
    		launchInstance li = new launchInstance();
        	li.start();
    	}
    }

    // termina uma instancia e retira do array
    /*private static void killInstance(String instanceId) throws Exception {
        terminateInstance(instanceId);
        instances.remove(instanceId);
        System.out.println("Instancia " + instanceId + " retirada");
        checkArrayInstances();
    }*/

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
   public static void init() throws Exception {

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
    static public class launchInstance extends Thread{
        public void run() {
            try {
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
        	        IamInstanceProfileSpecification profile = new IamInstanceProfileSpecification();	    
                    profile.setArn("arn:aws:iam::294305374682:instance-profile/cnv-project-role");


                    RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

                    runInstancesRequest.withImageId("ami-09047bcca9b0355dd")
                                       .withInstanceType("t2.micro")
                                       .withMinCount(1)
                                       .withMaxCount(1)
                                       .withKeyName("JB_WEBSERVER")
                                       .withSecurityGroups("CNV-ssh+http")
                                       .withIamInstanceProfile(profile);

                    RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
                    String newInstanceId = runInstancesResult.getReservation().getInstances()
                                              .get(0).getInstanceId();

                    synchronized(this) {
                        Date systemInitInstanceDate = Calendar.getInstance().getTime();         
                            instanceNameDateMapping.put(newInstanceId, systemInitInstanceDate);
                    }

                    runInstancesResult.getReservation().getInstances()
                                              .get(0).setLaunchTime(Calendar.getInstance().getTime());
                    checkInstancesRunning();

                    System.out.println("A new instance is running!");
                    System.out.println("===========================================");
                    Integer instanceState = -1;
                    addInstanceToArray(newInstanceId);                           
                    while(instanceState != 16) { //Loop until the instance is in the "running" state.
                        System.out.println("vai testar");
                        instanceState = getInstanceStatus(newInstanceId);
                        try {
                            Thread.sleep(5000);
                        } catch(InterruptedException e) {}
                    }
                    //return newInstanceId;
                    
                } catch (AmazonServiceException ase) {
                        System.out.println("Caught Exception: " + ase.getMessage());
                        System.out.println("Reponse Status Code: " + ase.getStatusCode());
                        System.out.println("Error Code: " + ase.getErrorCode());
                        System.out.println("Request ID: " + ase.getRequestId());
                }

			//                return null;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Integer getInstanceStatus(String instanceId) {
        AmazonEC2      ec2 = null;

        try {
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

        } catch(Exception e) {
            e.printStackTrace();
        }

        DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest()
            .withInstanceIds(instanceId);
        DescribeInstancesResult describeInstanceResult = ec2
            .describeInstances(describeInstanceRequest);
        InstanceState state = null;
        
        for (Reservation reservation : describeInstanceResult.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                if (instance.getInstanceId().equals(instanceId)) {
                    System.out.println("Instance encontrada: " + instance.getPublicDnsName());
                    state = instance.getState();
                }
            }
        }
        return state.getCode();
    }

    public static synchronized void addInstanceToArray(String id) {
        instances.add(id);
    }

    public static synchronized void deleteInstanceFromArray(String id) {
        instances.remove(id);
        loadPerInstance.remove(id);
        instanceNameDateMapping.remove(id);
        LoadBalancer.cleanInstance(id);
    }

    // termina uma instancia
    public static class terminateInstance extends Thread {
        private String newInstanceId;

        public terminateInstance(String id) {
            newInstanceId = id;
        }

        public void run() {
            try {
                System.out.println("===========================================");
                System.out.println("Terminating the instance...");
                
                TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
                termInstanceReq.withInstanceIds(newInstanceId);
                ec2.terminateInstances(termInstanceReq); 
                deleteInstanceFromArray(newInstanceId);

                System.out.println("Instance terminanted!");
                System.out.println("===========================================");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
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

    public ArrayList<String> getInstances() {
        return this.instances;
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

    public synchronized static void updateLoad(String instance, Double load) {
        if(loadPerInstance.get(instance) == null) {
            loadPerInstance.put(instance, load);
            System.out.println("com get a null new Load: " + load);
        }
        else {
            Double d = loadPerInstance.get(instance) + load;
            System.out.println("new Load: " + d);
            loadPerInstance.remove(instance);
            loadPerInstance.put(instance, d);

        }
    }

}

