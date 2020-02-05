    import java.awt.image.BufferedImage;

    import java.io.BufferedWriter;
    import java.io.File;
    import java.io.FileWriter;
    import java.io.FileNotFoundException;
    import java.io.PrintWriter;
    import java.io.IOException;
    import java.io.OutputStream;

    import java.lang.StringBuilder;

    import java.net.InetSocketAddress;

    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;

    import java.text.SimpleDateFormat;

    import java.util.ArrayList;
    import java.util.*;
    import java.util.concurrent.Executors;
    import java.util.HashMap;

    import javax.imageio.ImageIO;

    import com.sun.net.httpserver.Headers;
    import com.sun.net.httpserver.HttpExchange;
    import com.sun.net.httpserver.HttpHandler;
    import com.sun.net.httpserver.HttpServer;

    import pt.ulisboa.tecnico.cnv.solver.Solver;
    import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
    import pt.ulisboa.tecnico.cnv.solver.SolverFactory;
    import pt.ulisboa.tecnico.cnv.*;

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



    public class WebServer {

        private static Map<Long, Metrics> threadInfo = new HashMap<Long, Metrics>();
        private static DynamoDBMapper saveMetrics;

        public static void main(final String[] args) throws Exception {

            //final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8000), 0);

            final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

            server.createContext("/ASTAR", new ResolveRequest());
            server.createContext("/BFS", new ResolveRequest());
            server.createContext("/DFS", new ResolveRequest());
            server.createContext("/ping", new Ping()); 

            //Dynamo Init
            Dynamo.init();
            saveMetrics = new DynamoDBMapper(Dynamo.dynamoDB);

            // be aware! infinite pool of threads!
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();


            System.out.println(server.getAddress().toString());
        }


        public static Map<Long,Metrics> getHashMap()    {
            return threadInfo;
        }

        static class Ping implements HttpHandler {
            @Override
            public void handle(HttpExchange t) throws IOException {
                try{
                    System.out.println("ping");
                    String response = "pong";
                    t.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        static class ResolveRequest implements HttpHandler {
            @Override
            public void handle(final HttpExchange t) throws IOException {
                System.out.println("no ResolveRequest");
                // Get the query.
                final String query = t.getRequestURI().getQuery();

                //threadInfo.put(Thread.currentThread().getId(), new Metrics(Thread.currentThread().getId()));
                System.out.println("> Query:\t" + query);

                // Break it down into String[].
                final String[] params = query.split("&");

                // Store as if it was a direct call to SolverMain.
                final ArrayList<String> newArgs = new ArrayList<>();
                for (final String p : params) {
                    final String[] splitParam = p.split("=");
                    newArgs.add("-" + splitParam[0]);
                    newArgs.add(splitParam[1]);
                }
                
                newArgs.add("-d");

                // Store from ArrayList into regular String[].
                final String[] args = new String[newArgs.size()];
                int i = 0;
                for(String arg: newArgs) {
                    args[i] = arg;
                    i++;
                }

                SolverArgumentParser ap = null;
                try {
                    // Get user-provided flags.
                    ap = new SolverArgumentParser(args);
                }
                catch(Exception e) {
                    System.out.println(e);
                    return;
                }

                System.out.println("> Finished parsing args.");

                //Agora esta a thread mas dps tem de ser um numero aleatorio que define essa metrica na BD
                        threadInfo.put(Thread.currentThread().getId(), new Metrics(UUID.randomUUID().toString(),
                                Integer.parseInt(ap.getStartX().toString()), Integer.parseInt(ap.getStartY().toString()),
                                Integer.parseInt(ap.getX0().toString()), Integer.parseInt(ap.getY0().toString()),
                                Integer.parseInt(ap.getX1().toString()), Integer.parseInt(ap.getY1().toString()),
                                ap.getSolverStrategy().toString() , ap.getInputImage()
                                ));

                // Create solver instance from factory.
                final Solver s = SolverFactory.getInstance().makeSolver(ap);

                // Write figure file to disk.
                File responseFile = null;
                try {

                    final BufferedImage outputImg = s.solveImage();

                    final String outPath = ap.getOutputDirectory();

                    final String imageName = s.toString();

                    if(ap.isDebugging()) {
                        System.out.println("> Image name: " + imageName);
                    }

                    final Path imagePathPNG = Paths.get(outPath, imageName);
                    ImageIO.write(outputImg, "png", imagePathPNG.toFile());

                    responseFile = imagePathPNG.toFile();

                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                } catch (final IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                System.out.println("vai enviar");


                // Send response to browser.
                final Headers hdrs = t.getResponseHeaders();

                t.sendResponseHeaders(200, responseFile.length());

                hdrs.add("Content-Type", "image/png");

                hdrs.add("Access-Control-Allow-Origin", "*");
                hdrs.add("Access-Control-Allow-Credentials", "true");
                hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
                hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

                final OutputStream os = t.getResponseBody();
                Files.copy(responseFile.toPath(), os);
                
                os.close();
                Metrics getMetrics = threadInfo.get(Thread.currentThread().getId());
                checkIfExistsOnDB(getMetrics);
                threadInfo.remove(Thread.currentThread().getId());
                System.out.println("> Sent response to " + t.getRemoteAddress().toString());
            }
        }

	public static void checkIfExistsOnDB(Metrics metrics){
        Map<String, AttributeValue> collectedMetrics = new HashMap<String, AttributeValue>();

	  collectedMetrics.put(":strategy",new AttributeValue().withS(metrics.getStrategy()));
         collectedMetrics.put(":imageInput",new AttributeValue().withS(metrics.getImageInput()));
 collectedMetrics.put(":x0",new AttributeValue().withN(Integer.toString(metrics.getX0())));
  collectedMetrics.put(":x1",new AttributeValue().withN(Integer.toString(metrics.getX1())));
 collectedMetrics.put(":startX",new AttributeValue().withN(Integer.toString(metrics.getStartX())));
 collectedMetrics.put(":y0",new AttributeValue().withN(Integer.toString(metrics.getY0())));
  collectedMetrics.put(":y1",new AttributeValue().withN(Integer.toString(metrics.getY1())));
 collectedMetrics.put(":startY",new AttributeValue().withN(Integer.toString(metrics.getStartY())));


        DynamoDBQueryExpression<Metrics> queryExpression = new DynamoDBQueryExpression<Metrics>()
            .withKeyConditionExpression("imageInput = :imageInput")
            .withFilterExpression("strategy = :strategy and startX = :startX " +
                "and x0 = :x0 and x1 = :x1 and y0 = :y0 and y1 = :y1 and startY = :startY")
            .withExpressionAttributeValues(collectedMetrics);

        List<Metrics> exists = saveMetrics.query(Metrics.class, queryExpression);

        System.out.println("exists size: " + exists.size());

        System.out.println("It exists in the dynamo: " + (exists.size() > 0));

        if(!(exists.size() > 0)) {
            saveMetrics.save(metrics);
        }
        //return (exists.size() > 0);
    }

}

