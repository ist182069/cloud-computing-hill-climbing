/* 2016-18 Extended by Luis Veiga and Joao Garcia */
/* 
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;

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
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;

/**
* A fonte de inspiracao para este codigo foram os exemplos do lab 5
* O objetivo desta biblioteca eh ter todas as funcoes que comunicam com instancias no mesmo local
*/
public class LoadBalancerCommands {

    /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      (~/.aws/credentials) where the sample code will load the
     *      credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     * WARNING:
     *      To avoid accidental leakage of your credentials, DO NOT keep
     *      the credentials file in your source directory.
     */

    public static AmazonEC2      ec2;
    Set<Instance> instances = new HashSet<Instance>();
    static String AUTOSCALERID = "i-022678d202f6a8142";
    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.PropertiesCredentials
     * @see com.amazonaws.ClientConfiguration
     */
    public static void init() throws Exception {

        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
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
        IamInstanceProfileSpecification profile = new IamInstanceProfileSpecification();        
        profile.setArn("arn:aws:iam::542788646743:instance-profile/cnv-project-role");
        ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }


    /**
    * ========================= Comandos
    */


    public static void terminateInstance(String newInstanceId) throws Exception {
        System.out.println("===========================================");
        System.out.println("Terminating the instance...");
        
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(newInstanceId);
        ec2.terminateInstances(termInstanceReq); 

        System.out.println("Instance terminanted!");
        System.out.println("===========================================");

    }

    public boolean ping (String instancePublicDNS){
        try{
            System.out.println("instancePublicDNS no ping: " + instancePublicDNS);

            URL url = new URL("http://" + instancePublicDNS + ":8000/" + "ping");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String pong = rd.readLine();
            if(conn != null){
                rd.close();
                conn.disconnect();
            }
            System.out.println("resposta do ping: " + pong);
            return pong.equals("pong");
        
        } catch(Exception e){
            System.out.println("Instancia do ping nao esta activa.");
        }
        return false;
    }

    public byte[] getResponse(Request req, String instanceId){
        try{
            System.out.println("instanceID: " + instanceId);
            String instancePublicDNS = getInstancePublicDnsName(instanceId);

            System.out.println("instancePublicDNS: " + instancePublicDNS);

            URL url = new URL("http://" + instancePublicDNS + ":8000/" + req.gets() + "?" + req.getQuery());
            System.out.println("URL: " + "http://" + instancePublicDNS + ":8000/" + req.gets() + "?" + req.getQuery());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            System.out.println("conn: " + conn.getContentLength());
        
        if(conn.getContentLength()==-1) {
                return null; 
        }
            byte[] response = new byte[conn.getContentLength()];

            DataInputStream rd = new DataInputStream(conn.getInputStream());

            int readBytes = 0;

            while(readBytes < conn.getContentLength()){
                readBytes += rd.read(response, readBytes, response.length - readBytes);
            }

            System.out.println("numero de bytes de resposta: " + readBytes);

            if(conn != null){
                rd.close();
                conn.disconnect();
            }

            return response;

        } catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    // TODO: esta a ir buscar um instancia qualquer desde que esteja ativa
    private String getInstance(String instanceId) {
        System.out.println("inicio");
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        System.out.println("ec2");
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        System.out.println("reservations");
        Set<Instance> allInstances = new HashSet<Instance>();
        
        for (Reservation reservation : reservations) {
            System.out.println("for1");
            
            for (Instance instance : reservation.getInstances()) {
                System.out.println("for2: " + instance.getInstanceId());
                
                try{
                    if(!instance.getPublicDnsName().equals("") && !instance.getPublicDnsName().equals(null) ) {
                        if (ping(instance.getPublicDnsName())) {
                            System.out.println("instancia encontrada: " + instance.getPublicDnsName());
                            return instance.getPublicDnsName();
                        }
                    }
                } catch(Exception e) {
                    System.out.println("Erro ao fazer ping a " + instance.getInstanceId());
                    continue;
                }
            }
        }
        return null;
    }

    /*private String getInstance() {        
        try {
            System.out.println("AutoScaler ID: " + AUTOSCALERID);
            String dnsAutoScaler = getInstancePublicDnsName(AUTOSCALERID);
            URL url = new URL("http://" + dnsAutoScaler + ":8000/getInstance");
            //URL url = new URL("http://" + autoScalerPublicDNS + "+ ":8000/getInstance");
            System.out.println("URL AS: http://" + "ec2-35-174-4-126.compute-1.amazonaws.com" + ":8000/getInstance");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            byte[] response = new byte[conn.getContentLength()];

            DataInputStream dis = new DataInputStream(conn.getInputStream());
            int readBytes = 0;
            while((readBytes = dis.read(response, 0, response.length)) != -1){
                dis.read(response);
            }

            return new String(response);
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";

    }*/


    /**
    * ======================= Auxiliares
    */

    private static void checkInstancesRunning() {
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        Set<Instance> instances = new HashSet<Instance>();

        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }

        System.out.println("You have " + instances.size() + " Amazon EC2 instance(s) running.");
    } 


    public String getInstancePublicDnsName(String instanceId) {
        System.out.println("inicio");
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        System.out.println("ec2");
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        System.out.println("reservations");
        Set<Instance> allInstances = new HashSet<Instance>();
        
        for (Reservation reservation : reservations) {
            System.out.println("for1");
            for (Instance instance : reservation.getInstances()) {
                System.out.println("for2: " + instance.getInstanceId());
                if (instance.getInstanceId().equals(instanceId)) {
                    System.out.println("AutoScaler encontrado: " + instance.getPublicDnsName());
                    return instance.getPublicDnsName();
                }
            }
        }
        return null;
    }
}
