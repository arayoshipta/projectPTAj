package pta.measure;

import java.util.List;

import pta.PTA;
import pta.data.FPoint;



public class FPointStatics {
	private double aveX,aveY;
	private double sdX,sdY;
	private int totalFrame;
	private double aveFI,sdFI;
	private double aveOffset,sdOffset;
	private double runlength;
	
	public FPointStatics(List<FPoint> pl,double bg) {
		
		aveX=0;aveY=0;sdX=0;sdY=0;
		totalFrame=0;
		aveFI=0;sdFI=0;aveOffset=0;sdOffset=0;
		runlength=0;
		
		int count=0;
		double preX=0,preY=0;
		for(FPoint p:pl) {
			if(count>0)
				runlength += Math.sqrt((preX-p.getCx())*(preX-p.getCx())+(preY-p.getCy())*(preY-p.getCy()));
			aveX+=p.getCx();
			aveY+=p.getCy();
			if(PTA.isRoiInt())
				aveFI+=p.getRoiInt();
			else
				aveFI+=p.getParam()[0];
			if(PTA.isBgSub())
				aveFI-=bg;
			aveOffset +=p.getParam()[5];
			preX=p.getCx();
			preY=p.getCy();
			count++;
		}
		aveX /= count;
		aveY /= count;
		aveFI /= count;
		aveOffset /= count;
		count=0;
		for(FPoint p:pl) {
			sdX += (p.getCx()-aveX)*(p.getCx()-aveX);
			sdY += (p.getCy()-aveY)*(p.getCy()-aveY);
			sdFI += (p.getParam()[0]-aveFI)*(p.getParam()[0]-aveFI);
			sdOffset += (p.getParam()[5]-aveOffset)*(p.getParam()[5]-aveOffset);
			count++;
		}
		sdX /= count;
		sdY /= count;
		sdFI /= count;
		sdOffset /= count;
		sdX = Math.sqrt(sdX);
		sdY = Math.sqrt(sdY);
		sdFI = Math.sqrt(sdFI);
		sdOffset = Math.sqrt(sdOffset);
		totalFrame = count;
	}

	public double getAveX() {
		return aveX;
	}

	public double getAveY() {
		return aveY;
	}

	public double getSdY() {
		return sdY;
	}

	public int getTotalFrame() {
		return totalFrame;
	}

	public double getSdFI() {
		return sdFI;
	}

	public double getSdOffset() {
		return sdOffset;
	}

	public double getSdX() {
		return sdX;
	}

	public double getAveFI() {
		return aveFI;
	}

	public double getAveOffset() {
		return aveOffset;
	}
	
	public double getRunlength() {
		return runlength;
	}
	
	public String toString() {
		// totalFrame averageX sdX averageY sdY averageFI sdFI averageOffset sdOffset
		return String.format("%d %f %f %f %f %f %f %f %f %f", 
				totalFrame,runlength,aveX,sdX,aveY,sdY,aveFI,sdFI,aveOffset,sdOffset);
	}
}
