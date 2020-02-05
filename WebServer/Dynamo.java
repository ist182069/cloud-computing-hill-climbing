import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Dynamo{
    
    String filename; 

    static AmazonDynamoDB dynamoDB; 

    public static void init() throws Exception {

        /*ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }*/
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
            //.withCredentials(credentialsProvider)
            .withRegion("us-east-1")
            .build();
        
        createTable();
    }
    
    public static void createTable() {
        try {
            String tableName = "Metrics";

            System.out.println("Creating DynamoDB table: " + tableName);

            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
		.withKeySchema(new KeySchemaElement().withAttributeName("imageInput").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("imageInput").withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L))
                .withKeySchema(new KeySchemaElement().withAttributeName("ID").withKeyType(KeyType.RANGE))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("ID").withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));            
//    .withKeySchema(new KeySchemaElement().withAttributeName("webRequest").withKeyType(KeyType.HASH))
              //  .withAttributeDefinitions(new AttributeDefinition().withAttributeName("webRequest").withAttributeType(ScalarAttributeType.S))
                //.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

            TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
            TableUtils.waitUntilActive(dynamoDB, tableName);

            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch(InterruptedException ie){

        }
    }

   }
