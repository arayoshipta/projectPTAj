package pta.track;

import jaolho.data.lma.LMAMultiDimFunction;

public class LmaFittingWith2DGaussian extends LMAMultiDimFunction {

	@Override
	public double getY(double[] x, double[] a) {
		// TODO Auto-generated method stub
		double expx, expy, argx, argy, sqr;
		double y;

		argx = x[0]-a[3];
		argy = x[1]-a[4];
		expx = Math.exp(-argx*argx/(2*a[1]*a[1]));
		expy = Math.exp(-argy*argy/(2*a[2]*a[2]));
		sqr = a[1]*a[2];
		y = a[0]*expx*expy/(2*Math.PI*sqr);
		y += a[5];
		return y;
	}

	@Override
	public double getPartialDerivate(double[] x, double[] a, int parameterIndex) {
		switch (parameterIndex) {
		case 0: return x[0];
		case 1: return x[1];
		case 2: return x[2];
		case 3: return x[3];
		case 4: return x[4];
		case 5: return x[5];
		case 6: return 1;
		}
		throw new RuntimeException("No such parameter index: " + parameterIndex);
	}

}
