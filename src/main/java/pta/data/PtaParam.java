package pta.data;
import java.io.Serializable;

// Builder Pattern

public class PtaParam implements Serializable{

	private static final long serialVersionUID = 1L;

	public int getIterationNumber() {
		return IterationNumber;
	}

	public void setIterationNumber(int iterationNumber) {
		IterationNumber = iterationNumber;
	}

	public double getNearstRange() {
		return nearstRange;
	}

	public void setNearstRange(double nearstRange) {
		this.nearstRange = nearstRange;
	}

	public int getRoiSizex() {
		return roiSizex;
	}
	
	public int getRoiSizey() {
		return roiSizey;
	}

	public int[] getRoiSize() {
		int[] ret = new int[2];
		ret[0]=roiSizex;
		ret[1]=roiSizey;
		return ret;
	}
	
	public void setRoiSizex(int roiSizex) {
		this.roiSizex = roiSizex;
	}
	
	public void setRoiSizey(int roiSizey) {
		this.roiSizey = roiSizey;
	}
	
	public int getLinkageFrame() {
		return linkageFrame;
	}

	public void setLinkageFrame(int linkageFrame) {
		this.linkageFrame = linkageFrame;
	}

	public int getSearchPointIncrement() {
		return searchPointIncrement;
	}

	public void setSearchPointIncrement(int searchPointIncrement) {
		this.searchPointIncrement = searchPointIncrement;
	}

	public double getMinIntensity() {
		return minIntensity;
	}

	public void setMinIntensity(double minIntensity) {
		this.minIntensity = minIntensity;
	}
	
	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}
	
	public double getKurtosis() {
		return kurtosis;
	}

	public void setKurtosis(double kurtosis) {
		this.kurtosis = kurtosis;
	}
	
	public boolean isDo2dGaussfit() {
		return do2dGaussfit;
	}

	public void setDo2dGaussfit(boolean do2dGaussfit) {
		this.do2dGaussfit = do2dGaussfit;
	}
	
//	public void setDo2dGaussfitbyLMA(boolean do2dGaussfitbyLMA) {
//		this.do2dGaussfitbyLMA = do2dGaussfitbyLMA;
//	}
//	
//	public boolean isDo2dGaussfitbyLMA() {
//		return do2dGaussfitbyLMA;
//	}

	private int IterationNumber;
	private double nearstRange; // the distance range to recognize the particle as same particle.
	private int roiSizex,roiSizey;
	private int linkageFrame;
	private int searchPointIncrement;
	private double minIntensity;
	private int minSize;
	private double kurtosis;
	private boolean do2dGaussfit;
//	private boolean do2dGaussfitbyLMA;

	public static class Builder {
		// essential parameters
		private final int roiSizex,roiSizey;
		private boolean do2dGaussfit;
//		private boolean do2dGaussfitbyLMA;
		// option parameters; initialize as default
		private int IterationNumber = 1000;
		private double nearstRange = 0.5D; // the distance range to recognize the particle as same particle.
		private int linkageFrame = 0;
		private int searchPointIncrement = 1;
		private double minIntensity = 100;
		private int minSize = 5;
		private double kurtosis = -0.1D;
		
//		public Builder(int roiSize, boolean do2dGaussfit, boolean do2dGaussfitbyLMA) {
//			this.roiSizex = this.roiSizey = roiSize;
//			this.do2dGaussfit = do2dGaussfit;
//			this.do2dGaussfitbyLMA = do2dGaussfitbyLMA;
//		}
		
//		public Builder(int roiSizex,int roiSizey, boolean do2dGaussfit, boolean do2dGaussfitbyLMA) {
		public Builder(int roiSizex,int roiSizey, boolean do2dGaussfit) {

			this.roiSizex = roiSizex;
			this.roiSizey = roiSizey;
			this.do2dGaussfit = do2dGaussfit;
//			this.do2dGaussfitbyLMA = do2dGaussfitbyLMA;		
		}
		
		public Builder IterationNumber(int val) {IterationNumber = val>0?val:1000;return this;}
		public Builder nearstRange(double val) {nearstRange = val>0?val:1;return this;}
		public Builder linkageFrame(int val) {linkageFrame = val>0?val:1;return this;}
		public Builder serchPointIncrement(int val) {searchPointIncrement = val>0?val:1;return this;}
		public Builder minIntensity(double val) {minIntensity = val>0?val:0;return this;}
		public Builder minSize(int val) {minSize = val>1?val:1;return this;}
		public Builder kurtosis(double val) {kurtosis = val;return this;}
		
		public PtaParam build(){
			return new PtaParam(this);
		}
	}
	
	private PtaParam(Builder builder) {
		IterationNumber = builder.IterationNumber;
		nearstRange = builder.nearstRange;
		roiSizex = builder.roiSizex;
		roiSizey = builder.roiSizey;
		linkageFrame = builder.linkageFrame;
		searchPointIncrement = builder.searchPointIncrement;
		minIntensity = builder.minIntensity;
		minSize = builder.minSize;
		kurtosis = builder.kurtosis;
		do2dGaussfit = builder.do2dGaussfit;
//		do2dGaussfitbyLMA = builder.do2dGaussfitbyLMA;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IterationNumber:"+this.IterationNumber+"\n");
		sb.append("nearstRange:"+this.nearstRange+"\n");
		sb.append("roiSize-x:"+this.roiSizex+"\n");
		sb.append("roiSize-y:"+this.roiSizey+"\n");
		sb.append("linkageFrame:"+this.linkageFrame+"\n");
		sb.append("searchPoint:"+this.searchPointIncrement+"\n");
		sb.append("minIntensity:"+this.minIntensity+"\n");
		sb.append("minSize:"+this.minSize+"\n");
		sb.append("kurtosis:"+this.kurtosis+"\n");
		sb.append("do2DGauss:"+this.do2dGaussfit+"\n");
//		sb.append("do2DGaussByLMA:"+this.do2dGaussfitbyLMA+"\n");
		return sb.toString();
	}
	
	public double[] retParamAsArray() {
		double[] retParam = new double[9];
		retParam[0] = this.IterationNumber;
		retParam[1] = this.nearstRange;
		retParam[2] = this.roiSizex;
		retParam[3] = this.roiSizey;
		retParam[4] = this.linkageFrame;
		retParam[5] = this.searchPointIncrement;
		retParam[6] = this.minIntensity;
		retParam[7] = this.kurtosis;
		retParam[8] = this.do2dGaussfit?1:0;
//		retParam[9] = this.do2dGaussfitbyLMA?1:0;
		return retParam;
	}
}
