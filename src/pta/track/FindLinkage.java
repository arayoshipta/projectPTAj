package pta.track;
import ij.*;
import ij.measure.Calibration;

import java.util.ArrayList;
import java.util.List;

import pta.data.FPoint;
import pta.data.PtaParam;



public class FindLinkage {

	private List<List<FPoint>> alldplist;
	private double linkageSize;
	private int linkageFrame;
	private ImagePlus imp;
	private Calibration cal;

	FindLinkage(List<List<FPoint>> alldplist,PtaParam ptap,ImagePlus imp){
		this.imp = imp;
		this.cal = imp.getCalibration();
		this.alldplist = alldplist;
		this.linkageSize = ptap.getNearstRange()*ptap.getRoiSize()*cal.pixelWidth;
		this.linkageFrame = ptap.getLinkageFrame()+1;
	}

	public void run(){

		IJ.showStatus("finding linkage...");
		IJ.showProgress(0);

		for(int i=0;i<alldplist.size()-1;i++) {
			List<FPoint> tmpflist = FPoint.findDuplication(alldplist.get(i), linkageSize); // pick point list and remove duplication

			for(FPoint tmpfFPoint:tmpflist) {
				try{
					List<FPoint> nearFPoint = new ArrayList<FPoint>(); // to store the group of particle within the size*linkFactor area
					boolean tf=true;
					int linknum=1;
					// to find the particle frame-to-frame
					while(tf) { // loop until particle will be found
						List<FPoint> tmpslist = FPoint.findDuplication(alldplist.get(i+linknum), linkageSize); // pick point list and remove duplication
						for(FPoint tmpsFPoint : tmpslist) {
							if(tmpfFPoint.getPost() == null 
									&& tmpsFPoint.getPre() == null 
									&& FPoint.isSamePoint(tmpfFPoint, tmpsFPoint, linkageSize)) 
								nearFPoint.add(tmpsFPoint); // add near points to the nearFPoint				
						}
						if(!nearFPoint.isEmpty()) { // if there are FPoints in the nearFPoint list
							double distance = 100000; // there is no meaning of the value 100000. something large value is needed
							FPoint nearP = null; // initialize

							for(FPoint tmpP : nearFPoint) {
								double tmpdis = FPoint.getDistance(tmpfFPoint,tmpP);
								if(tmpdis<distance) {
									distance = tmpdis; // to find the nearest particle
									nearP = tmpP;
								} 
							}

							if(tmpfFPoint.getPost()==null) {
								tmpfFPoint.setPost(nearP); // link the nearest particle 
								tmpfFPoint.getPost().setPre(tmpfFPoint);
							}
							tf=false; // if found the particle, break the do-while loop
						} else  // if there are no points in the nearFPoint list
							linknum++;
						if(linknum>linkageFrame || (linknum+i)>(alldplist.size()-1))
							tf=false;
					}
				} catch (Exception e) {
					IJ.log(e.toString());
				}
				IJ.showStatus(String.format("Proceeding frame %d/%d", i,imp.getStackSize()));
				IJ.showProgress((double)i/(imp.getStackSize()));
			}

		}
		IJ.showProgress(1);
	}
}