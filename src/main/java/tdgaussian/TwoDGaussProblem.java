package tdgaussian;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;


/**
 * This is the test program for two dimensional Gaussian function fitting.
 * @author araiyoshiyuki
 * @date 08/01/2015
 */
public class TwoDGaussProblem {
	
	private double[] data;
	private double[] newStart;
	private int data_width;
	private int[] optim_param;
	private LeastSquaresProblem lsp;

	/**
	 * @param data	input data
	 * @param newStart	initial values
	 * @param data_width	data width
	 * @param optim_param	[0]:maxEvaluation, [1]:maxIteration
	 */
	public TwoDGaussProblem(double[] data, double[] newStart, int data_width, int[] optim_param) {
		this.data = data;
		this.newStart = newStart;
		this.data_width = data_width;
		this.optim_param = optim_param;
		buildlsb();
	}
	
	/**
	 * build LeastSquareProblem by using constructor data
	 */
	private void buildlsb() {
		//construct two-dimensional Gaussian function
		TwoDGaussianFunction tdgf = new TwoDGaussianFunction(this.data_width,this.data.length);
		
		//prepare construction of LeastSquresProblem by builder
		LeastSquaresBuilder lsb = new LeastSquaresBuilder();

		//set model function and its jacobean
		lsb.model(tdgf.retMVF(), tdgf.retMMF());
		//set target data
		lsb.target(this.data);
		//set initial parameters
		lsb.start(this.newStart);
		//set upper limit of evaluation time
		lsb.maxEvaluations(this.optim_param[0]);
		//set upper limit of iteration time
		lsb.maxIterations(this.optim_param[1]);
		
		lsp = lsb.build();
	}
	
	/**
	 * Do two dimensional Gaussian fit
	 * @return return the fitted data as Optimum
	 */
	public Optimum fit2dGauss() {
		LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();
		Optimum lsoo = lmo.optimize(lsp);

		return lsoo;	
	}

	public static void main(String[] args) {						
        //entry the data (5x5)
		double[] inputdata = {
				0  ,12 ,25 ,12 ,0  ,
				12 ,89 ,153,89 ,12 ,
				25 ,153,255,153,25 ,
				12 ,89 ,153,89 ,12 ,
				0  ,12 ,25 ,12 ,0  ,
		};		
		
		//set initial parameters
		double[] newStart = {
				255,
				1,
				1,
				1,
				1,
				1
		};
		
		TwoDGaussProblem tdgp = new TwoDGaussProblem(inputdata, newStart, 5, new int[] {1000,100});
		
		try{
			//do LevenbergMarquardt optimization and get optimized parameters
			Optimum opt = tdgp.fit2dGauss();
			final double[] optimalValues = opt.getPoint().toArray();
			
			//output data
			System.out.println("v0: " + optimalValues[0]);
			System.out.println("v1: " + optimalValues[1]);
			System.out.println("v2: " + optimalValues[2]);
			System.out.println("v3: " + optimalValues[3]);
			System.out.println("v4: " + optimalValues[4]);
			System.out.println("v5: " + optimalValues[5]);
			System.out.println("Iteration number: "+opt.getIterations());
			System.out.println("Evaluation number: "+opt.getEvaluations());
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

}
