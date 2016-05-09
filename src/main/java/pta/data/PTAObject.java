package pta.data;

import java.awt.Color;

public abstract class PTAObject {
	public double x,y;
	public int frame;
	public double intencity;
	public Color color;
	public PTAObject pre;
	public PTAObject post;
	
	public void setPre(PTAObject pre) {
		this.pre = pre;
	}
	
	public PTAObject getPre() {
		return this.pre;
	}
	
	public void setPost(PTAObject post) {
		this.post = post;
	}
	
	public PTAObject getPost() {
		return this.post;
	}
	
	public static double getDistance(PTAObject fo,PTAObject so) {
		double subx = fo.x-so.y;
		double suby = fo.x-so.y;
		return Math.sqrt(subx*subx+suby*suby);	
	}
}
