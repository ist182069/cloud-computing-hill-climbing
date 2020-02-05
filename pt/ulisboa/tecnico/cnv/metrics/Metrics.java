package pt.ulisboa.tecnico.cnv.metrics;

public class Metrics {

	private long id;

	private double basicBlocksStrategy;
	private double basicBlocksCoordinate;
	private double totalBasicBlocks;
	private double instructionCountStrategy;	
	private double instructionCountCoordinate;
	private double methodCountStrategy;
	private double methodCountCoordinate;
	
	private int memoryAllocsStrategy;
        private int memoryAllocsCoordinate;
	
	public Metrics(long id) {
		this.id = id;
	}
	
	public void setBasicBlocksStrategy(double basicBlocksStrategy) {
		this.basicBlocksStrategy = basicBlocksStrategy;
	}
	
	public double getBasicBlocksStrategy() {
		return basicBlocksStrategy;
	} 

	public double getInstructionCountStrategy() {
		return this.instructionCountStrategy;
	}

	public void setInstructionCountStrategy(double instructionCountStrategy) {
		this.instructionCountStrategy = instructionCountStrategy;
	}

        public double getInstructionCountCoordinate() {
                return this.instructionCountCoordinate;
        }

        public void setInstructionCountCoordinate(double instructionCountCoordinate) {
                this.instructionCountCoordinate = instructionCountCoordinate;
        }

	public double getMethodCountStrategy() {
		return this.methodCountStrategy;
	}
	
	public void setMethodCountStrategy(double methodCountStrategy) {
		this.methodCountStrategy = methodCountStrategy;
	}

	public double getMethodCountCoordinate() {
		return this.methodCountCoordinate;
	}

	public void setMethodCountCoordinate(double methodCountCoordinate) {
		this.methodCountCoordinate = methodCountCoordinate;
	}

	public void setBasicBlocksCoordinate(double basicBlocksCoordinate) {
		this.basicBlocksCoordinate = basicBlocksCoordinate;
	}

	public double getBasicBlocksCoordinate() {
		return basicBlocksCoordinate;
	}

	public double getTotalBasicBlocks(){
	 	this.totalBasicBlocks = basicBlocksStrategy + basicBlocksCoordinate;
		return totalBasicBlocks;
	}

	public double getStrategyEffort(){
		return (basicBlocksStrategy/totalBasicBlocks) * 100;
	}
	

	public double getCoordinateEffort(){
		return (basicBlocksCoordinate/totalBasicBlocks) * 100;
	}

	public void setMemoryAllocsStrategy(int memoryAllocsStrategy) {
		this.memoryAllocsStrategy = memoryAllocsStrategy;
	}
	
	public int getMemoryAllocsStrategy() {
		return memoryAllocsStrategy;
	}
    
        public void setMemoryAllocsCoordinate(int memoryAllocsCoordinate) {
        	this.memoryAllocsCoordinate = memoryAllocsCoordinate;
        }
    
        public int getMemoryAllocsCoordinate() {
    	        return memoryAllocsCoordinate;
        }

}
