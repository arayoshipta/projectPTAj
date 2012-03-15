import ij.IJ;
import ij.measure.Calibration;

import java.util.ArrayList;
import java.util.List;

public class calcMSD {
	//	private double[] msdList;
	//	private double[] dframe;
	private ArrayList<Double> msdList;
	private ArrayList<Double> dframe;

	public calcMSD(final List<FPoint> pointlist,final int leastLength,Calibration cal) {
		try {
			if (pointlist.size()<leastLength) 
				return;
			int calcFrameLen = leastLength;
			calcFrameLen = calcFrameLen>pointlist.get(pointlist.size()-1).getFrame()-pointlist.get(0).getFrame()?
					pointlist.get(pointlist.size()-1).getFrame()-pointlist.get(0).getFrame():calcFrameLen;
					calcFrameLen = calcFrameLen<3?3:calcFrameLen;

					//		msdList = new double[calcFrameLen];
					//		dframe = new double[calcFrameLen];;
					msdList = new ArrayList<Double>(0);
					dframe = new ArrayList<Double>(0);
					if(PTA_.isDebug()) IJ.log("totalFrameLen ="+calcFrameLen);
					//		msdList[0] = 0D;
					//		dframe[0] = 0D;

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
									if(PTA_.isDebug())
										IJ.log("fp.frame:"+fp.getFrame()+", sp.frame:"+sp.getFrame()+", len="+len+", cnt="+cnt+", j="+j+"l="+l);
									break;
								} else {
									if(PTA_.isDebug())
										IJ.log("Miss: fp.frame:"+fp.getFrame()+", sp.frame:"+sp.getFrame()+", len="+len+", cnt="+cnt+", j="+j+"l="+l);
								}
								l++;
							}
						}
						if(cnt !=0) {

							//				msdList[k-1]=len/(cnt*cal.frameInterval);
							msdList.add(new Double(len/(cnt*cal.frameInterval)));
							dframe.add(new Double(k*cal.frameInterval));

						}
						//			else
						//				msdList[k-1]=0;
						if(PTA_.isDebug())
							IJ.log("k="+k);
						//				IJ.log("msdList["+(k-1)+"]="+msdList[k-1]+", cnt="+cnt);
						//			dframe[k-1] = k*cal.frameInterval;
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
