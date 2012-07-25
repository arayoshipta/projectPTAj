package pta.measure;
public class PlRelation {

	public double x,y;
	private double plDistance;
	private int pixCntsOfl1l2;
	private int pixCntsOfl1p;
	
	public PlRelation(
			double l1x,double l1y,
			double l2x,double l2y,
			double px,double py){

		// calc distance from point to line
		double dx = l2x-l1x;
		double dy = l2y-l1y;
		double a = dx*dx+dy*dy;
		pixCntsOfl1l2 = Math.round((float)Math.sqrt(a));
		if(a==0) {
			plDistance = Math.sqrt((px-l1x)*(px-l1x)+(py-l1y)*(py-l1y));
			x=l1x;y=l1y;
		}
		double b = dx*(l1x-px)+dy*(l1y-py);
		double t = -(b/a);
		if (t<0.0D) t=0.0D;
		if (t>1.0D) t=1.0D;
		x=t*dx+l1x;
		y=t*dy+l1y;
		
		plDistance = Math.sqrt((x-px)*(x-px)+(y-py)*(y-py));
		double dpx = x-l1x;
		double dpy = y-l1y;
		double ap = dpx*dpx+dpy*dpy;
		pixCntsOfl1p = Math.round((float)Math.sqrt(ap));
	}
	
	public double retDistanceLineAndPoint() {
		return plDistance;
	}
	
	public int getPixCntsOfl1l2() {
		return pixCntsOfl1l2;
	}
	
	public int getPixCntsOfl1p() {
		return pixCntsOfl1p;
	}
}
