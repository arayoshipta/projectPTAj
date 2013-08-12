package pta.data;

import java.util.List;

/**
 * For sotring MSD analysis results. One MSDdata object per track. 
 * 20130803
 * 
 * @author Kota
 *
 */
public class MSDdata {
	int id;
	double[] fullDFrames;
	double[] fullMSD;
	double a;
	double b;
	double c;
	String fittype;
	private double r;
	private List<FPoint> track;

	/**
	 * @param fullDFrames
	 * @param fullMSD
	 * @param a
	 * @param b
	 * @param fittype
	 */
	public MSDdata(int id, List<FPoint> track, double[] fullDFrames, double[] fullMSD, double a, double b, double r) {
		super();
		this.id = id;
		this.track = track;
		this.fullDFrames = fullDFrames;
		this.fullMSD = fullMSD;
		this.a = a;
		this.b = b;
		this.r = r;
		this.fittype = "Linear";
	}	

	/**
	 * @param fullDFrames
	 * @param fullMSD
	 * @param a
	 * @param b
	 * @param c
	 * @param fittype
	 */
	public MSDdata(int id, List<FPoint> track, double[] fullDFrames, double[] fullMSD, double a, double b,
			double c,  double r) {
		super();
		this.id = id;
		this.track = track;
		this.fullDFrames = fullDFrames;
		this.fullMSD = fullMSD;
		this.a = a;
		this.b = b;
		this.c = c;
		this.fittype = "Poly2";
	}
	public double getID() {
		return id;
	}	
	public double[] getFullDFrames() {
		return fullDFrames;
	}

	public double[] getFullMSD() {
		return fullMSD;
	}

	/**
	 * Fitting result. 
	 * if "Linear", then y = a + bx
	 * if "Poly2", then y = a + bx + cx^2
	 * 
	 * @return a
	 */
	public double getA() {
		return a;
	}

	/**
	 * Fitting result. 
	 * if "Linear", then y = a + bx
	 * if "Poly2", then y = a + bx + cx^2
	 * 
	 * @return b
	 */
	public double getB() {
		return b;
	}

	/**
	 * Fitting result. 
	 * only for "Poly2", then y = a + bx + cx^2
	 * 
	 * @return c
	 */	
	public double getC() {
		return c;
	}
	/**
	 * Fitting result. 
	 * Squared of residuals. 
	 * @return R^2
	 */
	public double getR() {
		return r;
	}	

	/**
	 * Fitting equation. 
	 * "Linear" or "Poly2"
	 */
	public String getFittype() {
		return fittype;
	}
	
	/**
	 * The track of this object. 
	 * 
	 * @return
	 */
	public List<FPoint> getTrack(){
		return track;
	}
}
