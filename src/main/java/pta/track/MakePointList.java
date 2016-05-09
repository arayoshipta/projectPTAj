package pta.track;
import ij.gui.*;
import ij.measure.Calibration;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import pta.data.FPoint;




public class MakePointList {

	private List<List<FPoint>> alldplist;
	private Overlay	pathlist;
	private List<List<FPoint>> pointlist;
	private int maxFrmLen;
	private Calibration cal;
	
	MakePointList(List<List<FPoint>> alldplist,Overlay pathlist,int maxFrmLen) {
		this.alldplist=alldplist;
		this.pathlist=pathlist;
		this.maxFrmLen = maxFrmLen;
		FPoint firstp = alldplist.get(0).get(0);
		this.cal = firstp.retCal();
	}
	
	public List<List<FPoint>> run(){
		int frm = 1; // frame number
		int cnt = 0; // point number
		pointlist = new ArrayList<List<FPoint>>();
		for(List<FPoint> plist : alldplist) {
			for(FPoint currentP : plist) {
				List<FPoint> p = new ArrayList<FPoint>();
				if(currentP.getPre() != null) // find first point.
					continue;
				GeneralPath gpath = new GeneralPath();
				int frmLen = 0;
				p.add(currentP);
				while(true) {
					FPoint postP = currentP.getPost();
					if(postP==null) {
						// if the next link = null
						gpath.moveTo(currentP.getCx()/cal.pixelWidth,currentP.getCy()/cal.pixelHeight);
						gpath.lineTo((float)currentP.getCx()/cal.pixelWidth,currentP.getCy()/cal.pixelHeight);
						break;
					} else if (postP!=null) {
						gpath.moveTo(currentP.getCx()/cal.pixelWidth,currentP.getCy()/cal.pixelHeight);
						gpath.lineTo(postP.getCx()/cal.pixelWidth,postP.getCy()/cal.pixelHeight);
						p.add(postP);
						currentP = postP;
					}
					frmLen++;
				}
				if(frmLen>maxFrmLen) {
				ShapeRoi sroi = new ShapeRoi(gpath);
				sroi.setStrokeColor(Color.cyan);
				pathlist.add(sroi);
				cnt++;
				}
				pointlist.add(p);
			}
			frm++;
		}
		
		return pointlist;
	}
}
