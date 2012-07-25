package pta.gui;
import ij.WindowManager;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.*;

import pta.data.FPoint;



public class ShowPInfo extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1;
	static int point;
	static List<FPoint> plist;
	static int index;
	static JPanel pane;
	static JLabel pLabel=new JLabel();
	static JLabel framelenLabel=new JLabel();
	static JLabel xyLabel=new JLabel();
	static JLabel xysLabel=new JLabel();
	static JLabel fiLabel=new JLabel();
	static JLabel offsetLabel=new JLabel();
	static JLabel iteLabel=new JLabel();
	static JFrame frame;

	public ShowPInfo(int p,List<FPoint> pl,int index) {
		super(String.format("#%d info", point));
		if (frame==null) frame=this;

		point=p;
		plist=pl;

		setBounds(700,510,300,200);
		pane = (JPanel)getContentPane();
		pane.add(pLabel);
		pane.add(framelenLabel);
		pane.add(xyLabel);
		pane.add(xysLabel);
		pane.add(fiLabel);
		pane.add(offsetLabel);
		pane.add(iteLabel);
		showInfoData(plist.get(index));
		setResizable(false);
		setVisible(true);
		
		WindowManager.addWindow(this);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}
	public void showInfoData(FPoint fp) {
		pane.setLayout(new GridLayout(7,1));
		pLabel.setText(String.format("Point: #%d", point));
		framelenLabel.setText(String.format("Frame: %d/%d", fp.getFrame(),plist.get(plist.size()-1).getFrame()));
		xyLabel.setText(String.format("x:%f, y:%f",fp.getCx(),fp.getCy()));
		xysLabel.setText(String.format("sigma x:%f, sigma y:%f", fp.getParam()[1],fp.getParam()[2]));
		fiLabel.setText(String.format("Fluorescence Intensity:%f", fp.getParam()[0]));
		offsetLabel.setText(String.format("offset:%f", fp.getParam()[5]));
		iteLabel.setText(String.format("Iteration:%d", fp.getInfo()[1]));
	}
	public void setInfoData(int p,List<FPoint> pl,int i) {
		point = p;
		plist = pl;
		index = i;
		showInfoData(plist.get(i));	
	}
	public static void closeWindow(){
		frame.dispose();
	}
}
