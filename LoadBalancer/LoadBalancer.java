import java.util.List;
import java.util.ArrayList;

import java.util.*;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;

import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;

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
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.InstanceState;
import java.lang.InterruptedException; 
import java.util.Timer;
import java.util.TimerTask;

public class LoadBalancer {
    static final long MAX_WORKLOAD = 10000000L; //TODO: inserir valor razoavel
    private static int numeroOperacoes = 0;
    private static double mediaWorkload = 0.0;
    private static LoadBalancer loadBalancer;
    private static LoadBalancerCommands commands;
    private static List<Request> requests = new ArrayList<Request>();
    private static Map<String, List<Request>> requestsPerInstance = new HashMap<>();    //lista de requests porque mesma instancia pode estar a 
    private static Map<String, Double> loadPerInstance = new HashMap<String, Double>();
    private static AutoScaler autoScaler;
	
    private static final long DELAY = 0;
    private static final long PERIOD = 5 * 1000;

    private static DynamoDBMapper DynamoDB;                    // executar varios pedidos
    public static LoadBalancer getInstance() throws Exception {
        if(loadBalancer == null){
            loadBalancer = new LoadBalancer();
        }
        return loadBalancer;
    }
    
    static TimerTask getSystemLoad = new TimerTask(){
        @Override
        public void run(){
             try{
                 autoScaler.checkThreshold();
             } catch(Exception e){
                 e.printStackTrace();
             }
        }
    };
    static Timer timer = new Timer();

    public static void main(String[] args) throws Exception{
		try{                        
            commands = new LoadBalancerCommands();
            commands.init();
            final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/climb", new SolveRequest());
            Dynamo.init(); 
            DynamoDB = new DynamoDBMapper(Dynamo.dynamoDB);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("Endereco do Load Balancer: " + server.getAddress().toString());
            autoScaler = AutoScaler.getInstance();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    //auto-scaler executa as verificacoes em background
    public static void callTimer() {
        timer.scheduleAtFixedRate(getSystemLoad, DELAY, PERIOD);
    }

    public static void cleanInstance(String id) {
        requestsPerInstance.remove(id);
        loadPerInstance.remove(id); 
    }

    //isto funciona como um metodo mas eh um handler
    static class SolveRequest implements HttpHandler {
        
        @Override
        public void handle(HttpExchange t) throws IOException {
            try{

                System.out.println("No handle");

                String query = t.getRequestURI().getQuery();

                System.out.println("> Query:\t" + query);

                Request request = createRequest(query);
                
                request = predictRequestLoad(request);

                //decide a melhor instancia,
                String id = decideBestInstance(request);
                
                //associar instancia a carga atual e request
                associarRequest(id, request);
                addLoad(id, request);
		        autoScaler.updateLoad(id, request.getCost());
                Double old = request.getCost();

                byte[] response = null;

                while(response == null) {
                    try {
                        response = commands.getResponse(request, id);
                    } catch (Exception e) {
                        System.out.println("TODO: PROCURAR NOVO WEBSERVER");
                    }
                }

                t.sendResponseHeaders(200, response.length);

                OutputStream os = t.getResponseBody();              
                os.write(response);
                os.close();

                medias(request);
                //no fim delete do request e remover carga
                removeAssociateRequest(id,request);
                removeLoadRequest(id,request);
                autoScaler.updateLoad(id, -1 * old);

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public synchronized static void medias(Request request){ 
		 List<Metrics> metrics = getMetricsFromDB(
        request.getxS(), request.getyS(),request.getx0(),request.gety0(),request.getx1(),request.gety1(),request.gets(),request.geti()
        );
    	request = getEstimatedCostForRequest(request,metrics);
    	double novoValor = request.getCost();
    	numeroOperacoes+=1;
        mediaWorkload = (mediaWorkload*numeroOperacoes + novoValor)/(numeroOperacoes);
        System.out.println("mediaWorkload: " + mediaWorkload);
    }
	
    public static Request predictRequestLoad(Request request){
        List<Metrics> metrics = getMetricsFromDB(
        request.getxS(), request.getyS(),request.getx0(),request.gety0(),request.getx1(),request.gety1(),request.gets(),request.geti()
        );
	
	   
        //Logica do que queremos fazer, buscar BBS, fazer medias..wtv..
        if(metrics.size() > 0) {
            System.out.println("Metricas not null");
    	    request = getEstimatedCostForRequest(request,metrics);
    	    return request;
        }
    	else {
            System.out.println("mediaWorkload no predictRequestLoad: " + mediaWorkload);
    		request.setCost(mediaWorkload);//Comeca a 0 e vai subindo.
    	}
        return request;        

    }

    public static Request getEstimatedCostForRequest(Request request, List<Metrics> metricas){
    	double instrucoesCountStrat = 0;
    	double alocacoesmemoria = 0;
    	double instructionCountCoordinate =0;

    	for(Metrics entry : metricas){
        	instructionCountCoordinate = entry.getInstructionCountCoordinate();
            System.out.println("instructionCountCoordinate: " + instructionCountCoordinate);
        	alocacoesmemoria = entry.getMemoryAllocsCoordinate();
            System.out.println("alocacoesmemoria: " + alocacoesmemoria);
        	instrucoesCountStrat = entry.getInstructionCountStrategy();	
            System.out.println("instrucoesCountStrat: " + instrucoesCountStrat);   
        }
    	double carga = 0.70 * instructionCountCoordinate + 0.25 * alocacoesmemoria + 0.05 * instrucoesCountStrat;

    	System.out.println("carga: " + carga);
    	request.setCost(carga);	

    	return request;
    }

    public synchronized static void addRequestToInstance(String instance, Request request) {
        if(requestsPerInstance == null) {
            System.out.println("atributo eh null");
        }
        List<Request> instanceRequests = requestsPerInstance.get(instance); // lista de pedidos ja associados
        if(instanceRequests == null) {
            System.out.println("array eh null");
            instanceRequests = new ArrayList<Request>();
        }
        if(request == null) {
            System.out.println("request eh null");
        }

        if(instanceRequests == null) {
            System.out.println("still null");
            
        }
        instanceRequests.add(request);                                      // adiciona novo pedido
        requestsPerInstance.put(instance, instanceRequests);                            // adiciona lista atualizada da instancia
        System.out.println("passou");
    }

    public synchronized static void removeRequestFromInstance(String instance, Request request) {
        List<Request> instanceRequests = requestsPerInstance.get(instance); // lista de pedidos ja associados
        instanceRequests.remove(request);                                   // remove pedido
        requestsPerInstance.put(instance, instanceRequests);                // adiciona lista atualizada da instancia
    }

    public static Map<String, String> getRequestParameters(String query){
        // Break it down into String[].
        String[] params = query.split("&");

        // Store as if it was a direct call to SolverMain.
        Map<String, String> newArgs = new HashMap<>();
        for (String p : params) {
            String[] splitParam = p.split("=");
            newArgs.put(splitParam[0], splitParam[1]);
        }

        return newArgs;
    }

    public static Request createRequest(String query) {
        Map<String, String> requestParameters = getRequestParameters(query);

        Request request = new Request(
            requestParameters.get("w"),
            requestParameters.get("h"),
            requestParameters.get("x0"),
            requestParameters.get("x1"),
            requestParameters.get("y0"),
            requestParameters.get("y1"),
            requestParameters.get("xS"),
            requestParameters.get("yS"),
            requestParameters.get("s"),
            requestParameters.get("i"),
            query);

        return request;

    }

   public static List<Metrics> getMetricsFromDB(String startX, String startY, String X0, String Y0,String X1, String Y1,String strategy,String imageInput){
     Map<String, AttributeValue> collectedMetrics = new HashMap<String, AttributeValue>();

          collectedMetrics.put(":strategy",new AttributeValue().withS(strategy));
         collectedMetrics.put(":imageInput",new AttributeValue().withS(imageInput));
        collectedMetrics.put(":x0",new AttributeValue().withN(X0));
        collectedMetrics.put(":x1",new AttributeValue().withN(X1));
        collectedMetrics.put(":startX",new AttributeValue().withN(startX));
        collectedMetrics.put(":y0",new AttributeValue().withN(Y0));
        collectedMetrics.put(":y1",new AttributeValue().withN(Y1));
        collectedMetrics.put(":startY",new AttributeValue().withN(startY));


        DynamoDBQueryExpression<Metrics> queryExpression = new DynamoDBQueryExpression<Metrics>()
            .withKeyConditionExpression("imageInput = :imageInput")
            .withFilterExpression("strategy = :strategy and startX = :startX " +
                "and x0 = :x0 and x1 = :x1 and y0 = :y0 and y1 = :y1 and startY = :startY")
            .withExpressionAttributeValues(collectedMetrics);

        if(DynamoDB == null) {
            System.out.println("eh null");
        }
        List<Metrics> exists = DynamoDB.query(Metrics.class, queryExpression);

        System.out.println("exists size: " + exists.size());

        System.out.println("It exists in the dynamo: " + (exists.size() > 0));


        return exists;
    
   }

    

    static String decideBestInstance(Request requestToProcess){
        boolean created = false;

        while(true) {
            ArrayList<String> aliveInstances = autoScaler.getInstances();

            for(String inst : aliveInstances) {
                try {
                    int instanceState = getInstanceStatus(inst);
                    if(instanceState == 16) { //Loop until the instance is in the "running" state.
                        double instanceLoad = loadPerInstance.get(inst);

                        if(instanceLoad + requestToProcess.getCost() < MAX_WORKLOAD) {
                            return inst;
                        }
                        else {
                            if(instanceLoad == 0) {
                                return inst;
                            }
                        }    
                    }                    
                } catch(Exception e) {
                    ArrayList<String> instances = autoScaler.getInstances();
                    if(instances.contains(inst)) {
                        addNewMappingLoad(inst);
                        return inst;
                    }
                }
            }
            try {
                if(!created) {
                    autoScaler.newInstance();
                    created = true;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Integer getInstanceStatus(String instanceId) {
        
        DescribeInstancesResult describeInstanceResult = initEC2ForThings(instanceId);
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

    public synchronized static DescribeInstancesResult initEC2ForThings(String instanceId) {
        DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest()
            .withInstanceIds(instanceId);
        DescribeInstancesResult describeInstanceResult = commands.ec2
            .describeInstances(describeInstanceRequest);

        return describeInstanceResult;
    }

    public synchronized static void addNewMappingLoad(String instanceId) {
        loadPerInstance.put(instanceId, Double.valueOf("0"));
    }

    public synchronized static void associarRequest(String instanceId, Request request){
        List<Request> requests = requestsPerInstance.get(instanceId);
        if(requests == null) {
            requests = new ArrayList<Request>();
        }
        requests.add(request);
        requestsPerInstance.put(instanceId, requests);
    }

    public synchronized static void addLoad(String instanceId, Request request) {
        Double load = loadPerInstance.get(instanceId);
        if(load == null) {
            load = request.getCost();
        }
        else {
            load += request.getCost();
        }
        loadPerInstance.put(instanceId, load);
    }

    public synchronized static void removeAssociateRequest(String instance, Request request){
        List<Request> requests = requestsPerInstance.get(instance);
        requests.remove(request);
        requestsPerInstance.put(instance, requests);
    }

    public synchronized static void removeLoadRequest(String instance, Request request){
        Double load = loadPerInstance.get(instance);
        load -= request.getCost();
        //requests.remove(request);
        loadPerInstance.put(instance, load);
    }

    public static Map<String, List<Request>> getRequestPerInstance()    {
        return null;
    }

}



