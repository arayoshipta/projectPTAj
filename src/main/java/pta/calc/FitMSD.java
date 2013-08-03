package pta.calc;

import ij.IJ;
import ij.measure.Calibration;
import ij.measure.CurveFitter;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import pta.data.MSDdata;
import pta.data.FPoint;
/**
 * Takes a pointlist (=tracks) and does MSD fanalysis. 
 * Output will be an ArrayList of MSDdata objects. 
 * 20130803
 * 
 * @author arayoshi, kota
 */
public class FitMSD {
	
	private List<List<FPoint>> pointlist;
	private Calibration cal;
	
	/**
	 * 
	 * @param pointlist List<List<FPoint>>, linked point list (tracks)
	 * @param cal calibration oblject. 
	 */
	public FitMSD(List<List<FPoint>> pointlist, Calibration cal){
		this.cal = cal;
		this.pointlist = pointlist;
	}
	
	/**
	 * All tracks are selected in this case. 
	 * @param leastlenTime
	 * @param isLinear
	 * @return An ArrayList of MSDData objects. 
	 */
	public ArrayList<MSDdata> doMSDanalysis(
			double leastlenTime,
			boolean isLinear
			){
		int[] selectedList = new int[pointlist.size()];
		for(int i=0;i<pointlist.size();i++) selectedList[i]=i;
		ArrayList<MSDdata> msdresults = doMSDanalysis(selectedList, leastlenTime, isLinear);
		return msdresults;
	}
	/**
	 * Among all tracks within the pointlist (=tracks), only those in the selectedlist 
	 * will be analyzed. selectedlist is an int[] with ID numbers. 		
	 * @param selectedList
	 * @param leastlenTime
	 * @param isLinear
	 * @return An ArrayList of MSDData objects.
	 */
	public ArrayList<MSDdata> doMSDanalysis(
			int[] selectedList, 
			double leastlenTime,
			boolean isLinear
			){
		if(cal.frameInterval == 0.0) {
			cal.frameInterval = 1;
			cal.setTimeUnit("frame");
		}
		int leastlen = (int)(leastlenTime/cal.frameInterval);
		leastlen = leastlen<=3?3:leastlen;		
		IJ.log("Tracks:" + pointlist.size());
		IJ.log("Frame Interval:" + cal.frameInterval);
		IJ.log("leastlenTime:" + leastlenTime);
		IJ.log("Least Length:" + leastlen);
		ArrayList<MSDdata> msdresults = new ArrayList<MSDdata>();
		for(int index:selectedList) {
			if(pointlist.get(index).size()<leastlen) continue; // if the length of pointlist is less than leastlen, skip it.

			CalcMSD cm = new CalcMSD(pointlist.get(index),leastlen, cal);

			double[] fullDF = cm.getDFrame();
			double[] fullMSD = cm.getMsdList();
			double[] x = Arrays.copyOfRange(fullDF, 0, leastlen);
			double[] y = Arrays.copyOfRange(fullMSD,0,leastlen);
			CurveFitter cv = new CurveFitter(x,y);
			if(isLinear) {
				cv.doFit(CurveFitter.STRAIGHT_LINE);
				//IJ.log(cv.getParams()[0]+" "+cv.getParams()[1]+" "+cv.getRSquared());
				msdresults.add(new MSDdata(index, pointlist.get(index), fullDF, fullMSD, cv.getParams()[0], cv.getParams()[1], cv.getRSquared()));
			} else {
				cv.doFit(CurveFitter.POLY2);
			//	IJ.log(cv.getParams()[0]+" "+cv.getParams()[1]+" "+cv.getParams()[2]+" "+cv.getRSquared());						
				msdresults.add(new MSDdata(index, pointlist.get(index), fullDF, fullMSD, cv.getParams()[0], cv.getParams()[1], cv.getParams()[2], cv.getRSquared()));
			}
		}
		return msdresults;		
	}
}
