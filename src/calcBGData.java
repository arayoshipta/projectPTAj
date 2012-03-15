import java.util.List;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.ImageStatistics;


public class calcBGData {
	private double[] bgdata;
	private int bglen;

	public calcBGData(List<FPoint> plist, Calibration cal,ImagePlus imp) {
		FPoint lastPoint = plist.get(plist.size()-1); // get last point
		bglen = (lastPoint.getFrame()+PTA_.getSubBgLen())<=imp.getStackSize()?PTA_.getSubBgLen():(imp.getStackSize()-lastPoint.getFrame());
		bgdata = new double[bglen];
		imp.setSlice(lastPoint.getFrame()+1);
		int currentSlice = imp.getSlice();
		Roi preRoi = imp.getRoi();
		for (int b=0;b<bglen;b++) {
			imp.setRoi(lastPoint.retRoi());
			ImageStatistics is = imp.getStatistics();
			bgdata[b] = is.mean*is.area/cal.pixelWidth/cal.pixelHeight;
			imp.setSlice(currentSlice++);
		}
		imp.setRoi(preRoi);
	}
	
	public double aveBg() {
		double temp=0;
		for (int i=0;i<bglen;i++) {
			temp = temp + bgdata[i];
		}
		if (bglen !=0)
			return temp/bglen;
		else
			return 0;
	}
	
	public double[] getBGdata() {
		return bgdata;
	}
	
	public int getBGlen(){
		return bglen;
	}
}
