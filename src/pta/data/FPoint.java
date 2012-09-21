package pta.data;
import ij.gui.Roi;
import ij.measure.Calibration;

import java.awt.Color;
import java.io.Serializable;
import java.util.*;

public class FPoint implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[] param = new double[6];	// array for parameter
	private double roiInt;
	private int[] info = new int[2];	// array for info and itteration number
	private int sx,sy; // "Pixel" coordinate values for Roi
	private Color color;
	private double cx,cy; // "Unit" (c.f. "micron", "mm") coordinate values for position
	private FPoint pre;
	private FPoint post;
	private int frame;
	private int sizex;
	private int sizey;
	private double pixelHeight;
	private double pixelWidth;
	private double frameInterval;
	private String xyUnit;
	private String timeUnit;
	
	FPoint() {this.pre=null;this.post=null;}
	
	public FPoint(int sx, int sy, double[] param, double roiInt, int[] info, int[] size, Color color,int frame,Calibration cal) {
		this.setParam(param);
		this.setRoiInt(roiInt);
		this.setInfo(info);
		this.setSx(sx);
		this.setSy(sy);
		this.setSizex(size[0]);
		this.setSizey(size[1]);
		this.setColor(color);
		pixelWidth = cal.pixelWidth;
		pixelHeight = cal.pixelHeight;
		frameInterval = cal.frameInterval;
		xyUnit = cal.getUnit();
		timeUnit = cal.getTimeUnit();
		setCx(param[3]);
		setCy(param[4]);
		setPre(null);
		setPost(null);
		setFrame(frame);
	}

	private void setRoiInt(double roiInt) {
		// TODO Auto-generated method stub
		this.roiInt = roiInt;
	}

	public void setParam(double[] param) {
		this.param = param;
		setCx(param[3]);
		setCy(param[4]);
	}

	public double[] getParam() {
		return param;
	}

	public void setInfo(int[] info) {
		this.info = info;
	}

	public int[] getInfo() {
		return info;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}
	
	public static boolean isSamePoint(FPoint fp, FPoint sp, double criteria) {
		double subx = fp.getCx()-sp.getCx();
		double suby = fp.getCy()-sp.getCy();
		double length = Math.sqrt(subx*subx+suby*suby);
		if (criteria < 0) criteria = 0;
		return length<criteria?true:false;
	}
	
	public static List<FPoint> findDuplication(List<FPoint> dplist,double criteria) {
		for(int j=0;j<dplist.size();j++){
			for(int k=0;k<dplist.size();k++) {
				if (k!=j) {
					if(isSamePoint(dplist.get(k),dplist.get(j),criteria)) {
						dplist.remove(k);
						k--;
					}
				}
			}
		}
		return new ArrayList<FPoint>(dplist);
	}

	public static double getDistance(FPoint fp, FPoint sp) {
		double subx = fp.getCx()-sp.getCx();
		double suby = fp.getCy()-sp.getCy();
		return Math.sqrt(subx*subx+suby*suby);		
	}
	public void setCy(double cy) {
		this.cy = cy;
	}

	public double getCy() {
		return cy;
	}

	public void setCx(double cx) {
		this.cx = cx;
	}

	public double getCx() {
		return cx;
	}

	public void setPre(FPoint pre) {
		this.pre = pre;
	}

	public FPoint getPre() {
		return pre;
	}

	public void setPost(FPoint post) {
		this.post = post;
	}

	public FPoint getPost() {
		return post;
	}
	
	@Override
	public String toString() {
		return String.format("frame:%d; (x,y)=(%f,%f)", frame,cx,cy);
		
	}

	public void setSx(int sx) {
		this.sx = sx;
	}

	public int getSx() {
		return sx;
	}

	public void setSy(int sy) {
		this.sy = sy;
	}

	public int getSy() {
		return sy;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public int getFrame() {
		return frame;
	}

	public void setSizex(int size) {
		this.sizex = size;
	}
	
	public void setSizey(int size) {
		this.sizey = size;
	}

	public int getSizex() {
		return sizex;
	}

	public int getSizey() {
		return sizey;
	}
	
	public int[] getSize() {
		int[] ret = new int[2];
		ret[0]=sizex;
		ret[1]=sizey;
		return ret;
	}
	
	public double getRoiInt() {
		return roiInt;
	}

	public Roi retRoi() {
		Roi retRoi = new Roi((int)(Math.round((cx/pixelWidth-(double)sizex/2))),(int)(Math.round((cy/pixelHeight-(double)sizey/2))),
				sizex,sizey);
		retRoi.setStrokeColor(color);
		return retRoi;
	}
	
	public static Vector<Roi> retRoiset(List<FPoint> dplist,Color col) {
		Vector<Roi> roiset = new Vector<Roi>(dplist.size());
		for(FPoint fp:dplist) {
			Roi tmpRoi = fp.retRoi();
			tmpRoi.setStrokeColor(col);
			roiset.add(tmpRoi);
		}
		return roiset;
	}
	
	public Calibration retCal() {
		Calibration retc = new Calibration();
		retc.pixelWidth=pixelWidth;
		retc.pixelHeight=pixelHeight;
		retc.frameInterval=frameInterval;
		retc.setUnit(xyUnit);
		retc.setTimeUnit(timeUnit);
		return retc;		
	}
	
}
