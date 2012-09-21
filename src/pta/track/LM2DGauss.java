package pta.track;

import ZS.Solve.LMfunc;

public class LM2DGauss implements LMfunc {

	
	public double val(double[] x, double[] a) {
		// TODO Auto-generated method stub
		assert x.length == 2;
		assert a.length == 6;

		double y;
		double expx, expy, argx, argy, sqr;

		argx = x[0]-a[3];
		argy = x[1]-a[4];
		expx = Math.exp(-argx*argx/(2*a[1]*a[1]));
		expy = Math.exp(-argy*argy/(2*a[2]*a[2]));
		sqr = a[1]*a[2];
		y = a[0]*expx*expy/(2*Math.PI*sqr);
		y += a[5];
		return y;
	}

	
	public double grad(double[] x, double[] a, int ak) {
		// TODO Auto-generated method stub
		assert x.length == 2;

		// i - index one of the K Gaussians

		double expx, expy, argx, argy, sqr;

		argx = x[0]-a[3];
		argy = x[1]-a[4];
		expx = Math.exp(-argx*argx/(2*a[1]*a[1]));
		expy = Math.exp(-argy*argy/(2*a[2]*a[2]));
		sqr = 2*Math.PI*a[1]*a[2];

		if (ak == 0)
			return expx*expy/sqr;

		else if (ak == 1) {
			return a[0]*expx*expy*(argx*argx/(sqr*a[1]*a[1]*a[1])-1/(sqr*a[1]));
		}

		else if (ak == 2) {
			return a[0]*expx*expy*(argy*argy/(sqr*a[2]*a[2]*a[2])-1/(sqr*a[2]));
		}
		
		else if (ak==3) {
			return a[0]*argx*expx*expy/(sqr*a[1]*a[1]);
		}
		
		else if (ak==4) {
			return a[0]*argy*expx*expy/(sqr*a[2]*a[2]);
		}
		
		else if (ak==5) {
			return 1;
		}

		else {
			System.err.println("bad ak");
			return 1.;
		}
	}
	
	public double[] initial() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Object[] testdata() {
		// TODO Auto-generated method stub
		return null;
	}

}
