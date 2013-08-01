package pta.data;

public class MSDdata {
	double[] fullDFrames;
	double[] fullMSD;
	double a;
	double b;
	double c;
	String fittype;
	private double r;

	/**
	 * @param fullDFrames
	 * @param fullMSD
	 * @param a
	 * @param b
	 * @param c
	 * @param fittype
	 */
	public MSDdata(double[] fullDFrames, double[] fullMSD, double a, double b, double r) {
		super();
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
	public MSDdata(double[] fullDFrames, double[] fullMSD, double a, double b,
			double c,  double r) {
		super();
		this.fullDFrames = fullDFrames;
		this.fullMSD = fullMSD;
		this.a = a;
		this.b = b;
		this.c = c;
		this.r = r;
		this.fittype = "Poly2";
	}
	public double[] getFullDFrames() {
		return fullDFrames;
	}

	public double[] getFullMSD() {
		return fullMSD;
	}

	public double getA() {
		return a;
	}

	public double getB() {
		return b;
	}

	public double getC() {
		return c;
	}

	public String getFittype() {
		return fittype;
	}
}
