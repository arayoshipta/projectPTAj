import ij.measure.Calibration;

import java.util.List;


public class calcVelocity {
	private double[] velocity;
	private double[] frame;

	public calcVelocity(final List<FPoint> pl,Calibration cal) {
		if (pl.size()<2)
			return;
		velocity = new double[pl.size()-1];
		frame = new double[pl.size()-1];
		
		for(int index=0;index<pl.size()-1;index++) {
			double vel=0;
			vel = (pl.get(index).getCx()-pl.get(index+1).getCx())*(pl.get(index).getCx()-pl.get(index+1).getCx())+
			(pl.get(index).getCy()-pl.get(index+1).getCy())*(pl.get(index).getCy()-pl.get(index+1).getCy());
			vel = Math.sqrt(vel)/cal.frameInterval;
			velocity[index]=vel;
			frame[index] = index*cal.frameInterval;
		}
	}
	
	public double[] getVelocity() {
		return velocity;
	}
	public double[] getFrame() {
		return frame;
	}
}
