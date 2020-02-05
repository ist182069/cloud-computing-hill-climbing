public class LBandAS{

private static LoadBalancer loadBalancer;
public static void main(String[] args) throws Exception{
		
try{
		 	loadBalancer = LoadBalancer.getInstance();
		 	//AUTOSCALER TMB MAYBE
		 } catch(Exception e){
		 	e.printStackTrace();
		 }
	}



}
