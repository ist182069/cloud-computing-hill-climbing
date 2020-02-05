import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;


import java.io.Serializable;

@DynamoDBTable(tableName="Metrics")
public class Metrics implements Serializable {

         private static final long serialVersionUID = 1L;
    private double basicBlocksStrategy;
    private double basicBlocksCoordinate;
    private double totalBasicBlocks;
    private double instructionCountStrategy;
    private double instructionCountCoordinate;
    private double methodCountStrategy;
    private double methodCountCoordinate;
    private double coordinateEffort;
    private double strategyEffort;
    private int memoryAllocsStrategy;
    private int memoryAllocsCoordinate;

    private String id;
    private int startX;
    private int startY;
    private int X0;
    private int Y0;
    private String strategy;
    private int X1;
    private int Y1;
    private String imageInput;

    public Metrics (){}

    public Metrics(String id, int startX, int startY, int X0, int Y0, int X1, int Y1, String strategy, String imageInput) {
            this.id = id;
            this.startX = startX;
            this.startY = startY;
            this.X0 = X0;
            this.Y0 = Y0;
            this.X1 = X1;
            this.Y1 = Y1;
            this.strategy = strategy;
            this.imageInput = imageInput;

    }
    @DynamoDBHashKey(attributeName="imageInput")
    public String getImageInput(){
            return imageInput;
    }

    public void setImageInput(String imageinput){
            imageInput = imageinput;
    }

    @DynamoDBRangeKey(attributeName="ID")
     public String getId() {
            return id;
    }

    public void setId(String id) {
            this.id = id;
    }

    @DynamoDBAttribute(attributeName="startX")
    public int getStartX() {
            return startX;
    }

    public void setStartX(int startX) {
            this.startX = startX;
    }

    @DynamoDBAttribute(attributeName="startY")
    public int getStartY() {
            return startY;
    }

    public void setStartY(int startY) {
            this.startY = startY;
    }

    @DynamoDBAttribute(attributeName="x0")
    public int getX0() {
            return X0;
    }

    public void setX0(int x0) {
            X0 = x0;
    }
    @DynamoDBAttribute(attributeName="y0")
    public int getY0() {
            return Y0;
    }

    public void setY0(int y0) {
            Y0 = y0;
    }

    @DynamoDBAttribute(attributeName="strategy")
    public String getStrategy() {
            return strategy;
    }
    public void setStrategy(String strategy) {
            this.strategy = strategy;
    }

    @DynamoDBAttribute(attributeName="x1")
    public int getX1() {
            return X1;
    }

    public void setX1(int x1) {
            X1 = x1;
    }

    @DynamoDBAttribute(attributeName="y1")
    public int getY1() {
            return Y1;
    }

    public void setY1(int y1) {
            Y1 = y1;
    }




    public void setBasicBlocksStrategy(double basicBlocksStrategy) {
            this.basicBlocksStrategy = basicBlocksStrategy;
    }

    @DynamoDBAttribute(attributeName="BasicBlocksStrategy")
    public double getBasicBlocksStrategy() {
            return basicBlocksStrategy;
    }

    @DynamoDBAttribute(attributeName="instructionCountStrategy")
    public double getInstructionCountStrategy() {
            return this.instructionCountStrategy;
    }

    public void setInstructionCountStrategy(double instructionCountStrategy) {
            this.instructionCountStrategy = instructionCountStrategy;
    }

    @DynamoDBAttribute(attributeName="instructionCountCoordinate")
    public double getInstructionCountCoordinate() {
            return this.instructionCountCoordinate;
    }

    public void setInstructionCountCoordinate(double instructionCountCoordinate) {
            this.instructionCountCoordinate = instructionCountCoordinate;
    }

    @DynamoDBAttribute(attributeName="methodCountStrategy")
    public double getMethodCountStrategy() {
            return this.methodCountStrategy;
    }

    public void setMethodCountStrategy(double methodCountStrategy) {
            this.methodCountStrategy = methodCountStrategy;
    }

    @DynamoDBAttribute(attributeName="methodCountCoordinate")
    public double getMethodCountCoordinate() {
            return this.methodCountCoordinate;
    }

    public void setMethodCountCoordinate(double methodCountCoordinate) {
            this.methodCountCoordinate = methodCountCoordinate;
    }

    public void setBasicBlocksCoordinate(double basicBlocksCoordinate) {
            this.basicBlocksCoordinate = basicBlocksCoordinate;
    }

    @DynamoDBAttribute(attributeName="basicBlocksCoordinate")
    public double getBasicBlocksCoordinate() {
            return basicBlocksCoordinate;
    }

    @DynamoDBAttribute(attributeName="totalBasicBlocks")
    public double getTotalBasicBlocks(){
            this.totalBasicBlocks = basicBlocksStrategy + basicBlocksCoordinate;
            return totalBasicBlocks;
    }


    public void setTotalBasicBlocks(double ttb) {
        this.totalBasicBlocks = ttb;
    }

    @DynamoDBAttribute(attributeName="strategyEffort")
    public double getStrategyEffort(){
            return (basicBlocksStrategy/totalBasicBlocks) * 100;
    }

    public void setStrategyEffort(double strategyEffort){
        this.strategyEffort = strategyEffort;
        }

    @DynamoDBAttribute(attributeName="coordinateEffort")
    public double getCoordinateEffort(){
            return (basicBlocksCoordinate/totalBasicBlocks) * 100;
    }

    public void setCoordinateEffort(double coordinateEffort){
        this.coordinateEffort = coordinateEffort;
    }

    public void setMemoryAllocsStrategy(int memoryAllocsStrategy) {
            this.memoryAllocsStrategy = memoryAllocsStrategy;
    }

    @DynamoDBAttribute(attributeName="memoryAllocsStrategy")
    public int getMemoryAllocsStrategy() {
            return memoryAllocsStrategy;
    }

    public void setMemoryAllocsCoordinate(int memoryAllocsCoordinate) {
            this.memoryAllocsCoordinate = memoryAllocsCoordinate;
    }

    @DynamoDBAttribute(attributeName="memoryAllocsCoordinate")
    public int getMemoryAllocsCoordinate() {
            return memoryAllocsCoordinate;
    }

}

