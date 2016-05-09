package pta.calc;
import ij.IJ;
import ij.measure.Calibration;

import java.util.ArrayList;
import java.util.List;

import pta.PTA;
import pta.data.FPoint;
/**
 * Core of the MSD calculation. 
 * Fitting will be done by the class FitMSD. 
 * 
 * @author arayoshi
 * modified by Kota 20130803
 *
 */
public class CalcMSD {

	private ArrayList<Double> msdList;
	private ArrayList<Double> dframe;
	private Calibration cal;
	private List<FPoint> pointlist;
	private int leastLength;

	public CalcMSD(final List<FPoint> pointlist,final int leastLength,Calibration cal) {
		this.pointlist = pointlist;
		this.leastLength = leastLength;
		this.cal = cal;
		run();
	}

	public void run(){
		try {
			if (pointlist.size()<leastLength) 
				return;
			int calcFrameLen = leastLength;
			calcFrameLen = calcFrameLen>pointlist.get(pointlist.size()-1).getFrame()-pointlist.get(0).getFrame()?
					pointlist.get(pointlist.size()-1).getFrame()-pointlist.get(0).getFrame():calcFrameLen;
					calcFrameLen = calcFrameLen<3?3:calcFrameLen;

					msdList = new ArrayList<Double>(0);
					dframe = new ArrayList<Double>(0);
					if(PTA.isDebug()) IJ.log("totalFrameLen ="+calcFrameLen);

					int n=pointlist.size();
					for(int k=1;k<=pointlist.get(pointlist.size()-1).getFrame();k++) { // shift value of k
						double len=0D;
						int cnt=0;	// count for data division
						for(int j=0;j<n;j++) {
							FPoint fp = pointlist.get(j);
							int l=1;
							// the second point index of j+l must be less than the length of the pointlist
							while((j+l)<n) {
								FPoint sp = pointlist.get(j+l);
								if(sp.getFrame() == fp.getFrame()+k) {
									len += ((sp.getCx()-fp.getCx())*(sp.getCx()-fp.getCx())+(sp.getCy()-fp.getCy())*(sp.getCy()-fp.getCy()));
									cnt++;
									if(PTA.isDebug())
										IJ.log("fp.frame:"+fp.getFrame()+", sp.frame:"+sp.getFrame()+", len="+len+", cnt="+cnt+", j="+j+"l="+l);
									break;
								} else {
									if(PTA.isDebug())
										IJ.log("Miss: fp.frame:"+fp.getFrame()+", sp.frame:"+sp.getFrame()+", len="+len+", cnt="+cnt+", j="+j+"l="+l);
								}
								l++;
							}
						}
						if(cnt !=0) {
							msdList.add(new Double(len/cnt));
							dframe.add(new Double(k*cal.frameInterval));
						}
						//			else
						//				msdList[k-1]=0;
						if(PTA.isDebug())
							IJ.log("k="+k);
					}
		} catch (Exception e) {
			IJ.log("calcMsd:"+e.toString());
		}
	}
	public double[] getMsdList() {
		double[] retd = new double[msdList.size()];
		int i=0;
		try {
			for(Double dval:msdList) {
				retd[i] = dval.doubleValue();
				i++;
			}
		} catch (Exception e) {
			IJ.log("getMsdList:"+e.toString());
		}
		return retd;
	}
	public double[] getDFrame() {
		double[] retd = new double[dframe.size()];
		int i=0;
		try {
			for(Double dval:dframe) {
				retd[i] = dval.doubleValue();
				i++;
			}
		} catch (Exception e) {
			IJ.log("getDFrame:"+e.toString());
		}
		return retd;
	}
}