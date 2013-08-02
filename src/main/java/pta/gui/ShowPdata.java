package pta.gui;
import ij.*;
import ij.gui.*;
import ij.io.SaveDialog;
import ij.measure.*;
import ij.process.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.DefaultXYDataset;

import pta.PTA;
import pta.calc.CalcBGData;
import pta.calc.CalcMSD;
import pta.calc.CalcVelocity;
import pta.calc.FitMSD;
import pta.data.FPoint;
import pta.data.MSDdata;
import pta.data.PtaParam;
import pta.measure.FPointStatics;
import pta.measure.PlRelation;



public class ShowPdata extends JFrame{

	private static final long serialVersionUID = 1L;
	final static String[] columnNames = {"#", "From", "To","FrameLength","FIAverage","RunLength","Check","Color"};
	private Object[][] data;
	private List<List<FPoint>> pointlist;
	private ImagePlus imp;
	private Calibration cal;
	private JTable jt;
	static chartFrame cFrame;
	static JFrame msdFrame;
	static JFrame histoFrame;
	private int[] selectedList;
	static ChartPanel xypanel,fipanel,xpanel,ypanel;
	static XYPlot xyXyplot,xyFiplot,xyXplot,xyYplot;
	static ChartPanel msdpanel;
	static ChartPanel velpanel;
	static Color[] comboColors = {Color.cyan,Color.blue,Color.red,Color.yellow,Color.green,Color.magenta,Color.orange,Color.white};
	static String[] cString = {"Cyan","Blue","Red","Yellow","Green","Magenta","Orange","White"};
	private double[] x,y,fi,frame,framexy;
	private int framelen;
	public saveDataAction sda;
	private PtaParam ptap;
	private double[] FIList,durationList,runLengthList,aveXList,sdXList;
	private double[] aveYList,sdYList,sdFIList,aveOstList,sdOstList;
	static ChartPanel FIHistoPanel,durationHistoPanel,sdFIHistoPanel,aveOstHistoPanel,sdOstHistoPanel,
	runLengthHistoPanel,aveXHistoPanel,sdXHistoPanel,aveYHistoPanel,sdYHistoPanel;
	private ShowPdata pData;
	public int size;

	/*
	 * constructor
	 */	

	public ShowPdata(List<List<FPoint>> pointlist, ImagePlus imp,PtaParam ptap, boolean nogui) {
		this.pointlist = pointlist;
		this.imp = imp;
		this.cal = imp.getCalibration();
		this.selectedList = new int[pointlist.size()];
		this.ptap = ptap;

		for(int i=0;i<pointlist.size();i++) selectedList[i]=i;
		setTableObjectData(pointlist);
		sda = new saveDataAction();

		if (!nogui) 
			new makeTableFrame(this);
		PTA.setSelectedList(pointlist, selectedList);
		PTA.setAnalyzedImp(imp);
		//		IJ.log("pData constructed");
	}

	public ShowPdata(List<List<FPoint>> pointlist, Object[][] objData, ImagePlus imp,PtaParam ptap) {
		this.pointlist = pointlist;
		this.imp = imp;
		this.cal = imp.getCalibration();
		this.selectedList = new int[pointlist.size()];
		this.ptap = ptap;

		for(int i=0;i<pointlist.size();i++) selectedList[i]=i;
		data = new Object[pointlist.size()][8];
		for(int i=0;i<pointlist.size();i++) {
			int pnum = (Integer)objData[i][0];
			for(int j=0;j<8;j++)
				data[pnum][j]=objData[i][j];
		}
		sda = new saveDataAction();

		new makeTableFrame(this);
		PTA.setSelectedList(pointlist, selectedList);
		PTA.setAnalyzedImp(imp);
	}

	private void setTableObjectData(List<List<FPoint>> pl) {
		int pointnum=0;
		data = new Object[pl.size()][8];
		for(List<FPoint> list:pl) {
			int ffrom = list.get(0).getFrame();
			int fto = list.get(list.size()-1).getFrame();
			double avebg=0;
			if(PTA.isBg()) {
				CalcBGData cbg = new CalcBGData(list,cal,imp);
				avebg=cbg.aveBg();
			}
			FPointStatics fps = new FPointStatics(list,avebg);
			data[pointnum][0]=new Integer(pointnum);
			data[pointnum][1]=new Integer(ffrom);
			data[pointnum][2]=new Integer(fto);
			data[pointnum][3]=new Integer(fto-ffrom+1);
			data[pointnum][4]=fps.getAveFI();
			data[pointnum][5]=fps.getRunlength();
			data[pointnum][6]=new Boolean(false);
			data[pointnum][7]=Color.cyan; // DefaultColor is Cyan
			pointnum++;
		}				
	}

	public void updatejt() {
		drawTrajectory(pointlist.get(jt.convertRowIndexToModel(jt.getSelectedRows()[0])));
	}

	public void updateTableFI() {
		double avebg=0;
		for(int i=0;i<pointlist.size();i++) {
			List<FPoint> plist = pointlist.get(jt.convertRowIndexToModel(i));
			if(PTA.isBg() && PTA.isBgSub()) {
				CalcBGData cbg = new CalcBGData(plist,cal,imp);
				avebg=cbg.aveBg();
			}
			if(!PTA.isBgSub()) 
				avebg = 0;
			FPointStatics fps = new FPointStatics(plist,avebg);
			jt.setValueAt(fps.getAveFI(), i, 4);

		}
	}

	public void drawTrajectory(List<FPoint> plist) {
		double[] bgdata=null;
		double avebg=0;
		if(PTA.isBg()) {
			CalcBGData cbg = new CalcBGData(plist,cal,imp);
			bgdata = cbg.getBGdata();
			avebg=cbg.aveBg();
		}
		if(!PTA.isBgSub()) 
			avebg = 0;
		FPointStatics fps = new FPointStatics(plist,avebg);
		jt.setValueAt(fps.getAveFI(), jt.convertRowIndexToView(pointlist.indexOf(plist)), 4);

		int i=0;
		int bglen=0;
		if(bgdata != null) bglen=bgdata.length;
		framelen = plist.size();
		framexy = new double[framelen];
		frame = new double[framelen+bglen];
		fi = new double[framelen+bglen];
		x = new double[framelen];
		y = new double[framelen];
		for(FPoint p:plist) {
			frame[i] = framexy[i] = (double)p.getFrame()*cal.frameInterval;
			if(PTA.isRoiInt())
				fi[i] = p.getRoiInt();
			else
				fi[i] = p.getParam()[0];
			x[i] = p.getCx();
			y[i] = p.getCy();
			i++;
		}
		for(int b =0;b<bglen;b++) {
			frame[framelen+b] = frame[framelen-1] + cal.frameInterval*b;
			fi[framelen+b] = bgdata[b];
		}
		if(PTA.isBgSub()) {
			for(int j=0;j<frame.length;j++){
				fi[j]=fi[j]-avebg;
			}					
		}
		DefaultXYDataset dxy = new DefaultXYDataset();
		DefaultXYDataset fiframe = new DefaultXYDataset();
		DefaultXYDataset framex = new DefaultXYDataset();
		DefaultXYDataset framey = new DefaultXYDataset();
		fiframe.addSeries("FI", new double[][]{frame,fi});
		dxy.addSeries("xy", new double[][]{x,y});
		framex.addSeries("framex", new double[][]{framexy,x});
		framey.addSeries("framey", new double[][]{framexy,y});
		JFreeChart xychart = 
			ChartFactory.createXYLineChart("x-y coordinates", "x ["+cal.getUnit()+"]", "y ["+cal.getUnit()+"]", dxy, PlotOrientation.VERTICAL,false, true, false);
		JFreeChart fichart = 
			ChartFactory.createXYLineChart("FI trajectory", cal.getTimeUnit(), "FI [a.u.]", fiframe, PlotOrientation.VERTICAL,false,true,false);
		JFreeChart xframechart = 
			ChartFactory.createXYLineChart("x trajectory", cal.getTimeUnit(), "x ["+cal.getUnit()+"]", framex, PlotOrientation.VERTICAL,false,true,false);
		JFreeChart yframechart = 
			ChartFactory.createXYLineChart("y trajectory", cal.getTimeUnit(), "y ["+cal.getUnit()+"]", framey, PlotOrientation.VERTICAL,false,true,false);

		if(xypanel == null || fipanel==null || xpanel == null || ypanel == null) {
			xypanel = new ChartPanel(xychart);
			fipanel = new ChartPanel(fichart);
			xpanel = new ChartPanel(xframechart);
			ypanel = new ChartPanel(yframechart);
			cFrame.getContentPane().add(xypanel);
			cFrame.getContentPane().add(fipanel);
			cFrame.getContentPane().add(xpanel);
			cFrame.getContentPane().add(ypanel);
		} else {	
			xypanel.setChart(xychart);
			fipanel.setChart(fichart);
			xpanel.setChart(xframechart);
			ypanel.setChart(yframechart);
		}

		xyXyplot=configXYchart(xychart);
		xyXyplot.getRangeAxis().setInverted(true);
		xyFiplot=configXYchart(fichart);
		xyXplot=configXYchart(xframechart);
		xyYplot=configXYchart(yframechart);
		try {
			//make msd graph panel
			DefaultXYDataset msdxy = new DefaultXYDataset();
			double[][] msddata;
			if(plist.size()>=3) {
				CalcMSD cm = new CalcMSD(plist,plist.size()-1,cal);
				msddata = new double[][]{cm.getDFrame(),cm.getMsdList()};
			} else {
				msddata = new double[][]{new double[]{0},new double[]{0}};
			}
			msdxy.addSeries("msd", msddata);
			JFreeChart msdchart = 
				ChartFactory.createXYLineChart("MSD", "delta "+cal.getTimeUnit(), "msd ["+cal.getUnit()+"^2/"+cal.getTimeUnit()+"]", msdxy, PlotOrientation.VERTICAL,false, true, false);
			if(msdpanel == null) {
				msdpanel = new ChartPanel(msdchart);
				cFrame.getContentPane().add(msdpanel);
			} else {
				msdpanel.setChart(msdchart);
			}
			//make velocity graph panel
			DefaultXYDataset veldataset = new DefaultXYDataset();
			double[][] veldata;
			if(plist.size()>=2) {
				CalcVelocity cv = new CalcVelocity(plist,cal);
				veldata = new double[][]{cv.getFrame(),cv.getVelocity()};
			} else {
				veldata = new double[][]{new double[]{0},new double[]{0}};
			}
			veldataset.addSeries("velocity", veldata);
			JFreeChart velchart = 
				ChartFactory.createXYLineChart("Velocity", cal.getTimeUnit(), "velocity ["+cal.getUnit()+"/"+cal.getTimeUnit()+"]", veldataset, PlotOrientation.VERTICAL, false, true, false);
			if(velpanel == null) {
				velpanel = new ChartPanel(velchart);
				cFrame.getContentPane().add(velpanel);
			} else {
				velpanel.setChart(velchart);
			}

			imp.setSlice(plist.get(0).getFrame());
			cFrame.validate();

			if(!cFrame.isVisible()) {
				cFrame.setVisible(true);
				WindowManager.addWindow((Frame)cFrame);
			}
		} catch (NullPointerException e) {
			IJ.log(e.toString());
		}
	}
	public void updateTrajectory() {
		drawTrajectory(pointlist.get(jt.convertRowIndexToModel(jt.getSelectedRow())));
	}
	class makeTableFrame implements ListSelectionListener {

		public makeTableFrame(final JFrame frame) {

			frame.setTitle(imp.getShortTitle()+", Total Points = "+String.valueOf(pointlist.size()));
			frame.setBounds(10,10,300,200);
			JPanel pane = (JPanel)frame.getContentPane();

			TableModel dm = new DefaultTableModel(data,columnNames){
				private static final long serialVersionUID = 1L;

				@Override public Class<?> getColumnClass(int column) {
					return getValueAt(0,column).getClass();	// to return proper value}
				}
			};
			jt = new JTable(dm) {
				private static final long serialVersionUID = 1L;

				@Override public boolean isCellEditable(int row,int column) {
					if(column<6)
						return false;
					else
						return true; // only column over 6 is editable
				}
			};
			JComboBox combobox = new JComboBox(comboColors);
			combobox.setRenderer(new MyCellRenderer());
			TableCellEditor editor = new DefaultCellEditor(combobox) ; 
			jt.getColumnModel().getColumn(7).setCellEditor(editor);
			jt.setDefaultRenderer(Object.class, new ColorTableRenderer());
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(dm);
			sorter.setMaxSortKeys(2);
			jt.setRowSorter(sorter);
			jt.getSelectionModel().addListSelectionListener(this);
			JScrollPane tablePane = new JScrollPane(jt);
			pane.add(tablePane,BorderLayout.CENTER);			

			// menu item settings

			JMenuBar menubar = new JMenuBar();
			frame.setJMenuBar(menubar);
			JMenu file = new JMenu("File");
			menubar.add(file);
			JMenuItem saveAll = new JMenuItem("Save all");
			saveAll.addActionListener(sda);
			JMenuItem saveSelected = new JMenuItem("Save selected");
			saveSelected.addActionListener(sda);
			JMenuItem saveChecked = new JMenuItem("Save checked");
			saveChecked.addActionListener(sda);
			JMenuItem saveTableAsText = new JMenuItem("Save Table as Text Data");
			saveTableAsText.addActionListener(sda);
			JMenuItem saveTable = new JMenuItem("Save Table");
			saveTable.addActionListener(sda);
			file.add(saveAll);
			file.add(saveSelected);
			file.add(saveChecked);
			file.add(saveTableAsText);
			file.add(saveTable);

			JMenu anaMenu = new JMenu("Analysis");
			menubar.add(anaMenu);
			JMenuItem calcMSD = new JMenuItem("calc MSD");
			JMenuItem calcVel = new JMenuItem("calc Velocity");
			JMenuItem reFitBy2DGauss = new JMenuItem("Re-fit by 2DGauss");
			JMenuItem extractPalongLineRoi = new JMenuItem("Extract point along LineRoi");
			JMenuItem statSD = new JMenuItem("show Statics");
			calcMSD.addActionListener(new anaMenuAction());
			calcVel.addActionListener(new anaMenuAction());
			reFitBy2DGauss.addActionListener(new anaMenuAction());
			extractPalongLineRoi.addActionListener(new anaMenuAction());
			anaMenu.add(calcMSD);
			anaMenu.add(calcVel);
			anaMenu.add(reFitBy2DGauss);
			anaMenu.add(extractPalongLineRoi);

			JMenu checkMenu = new JMenu("Check");
			menubar.add(checkMenu);
			JMenuItem uncheckSelected = new JMenuItem("Uncheck");
			JMenuItem checkSelected = new JMenuItem("Check");
			JMenuItem checkReverse = new JMenuItem("Reverse");
			uncheckSelected.addActionListener(new checkMenuAction());
			checkSelected.addActionListener(new checkMenuAction());
			checkReverse.addActionListener(new checkMenuAction());
			checkMenu.add(uncheckSelected);
			checkMenu.add(checkSelected);
			checkMenu.add(checkReverse);

			JMenu statMenu = new JMenu("Statistics");
			menubar.add(statMenu);
			JMenuItem statAll = new JMenuItem("All points of statistics");
			JMenuItem statSelected = new JMenuItem("Checked points of statistics");
			statAll.addActionListener(new statMenuAction());
			statSelected.addActionListener(new statMenuAction());
			statSD.addActionListener(new statMenuAction());

			statMenu.add(statAll);
			statMenu.add(statSelected);
			statMenu.add(statSD);

			
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			WindowManager.addWindow((Frame)frame);
			pData = (ShowPdata)frame;

			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e)
				{
					GenericDialog yesnoDialog = new GenericDialog("Close?");
					yesnoDialog.addMessage("Close this Table?");
					yesnoDialog.showDialog();
					if(yesnoDialog.wasOKed()) {
						if(cFrame!=null)
							cFrame.dispose();
						if(histoFrame!=null)
							histoFrame.dispose();
						WindowManager.removeWindow(cFrame);
						WindowManager.removeWindow(histoFrame);
						WindowManager.removeWindow((Frame)frame);
						e.getWindow().setVisible(false);
						ShowPInfo.closeWindow();
						imp.setOverlay(null);
					} 
				}
				public void windowActivated(WindowEvent e) {
					PTA.setPdata(imp,pData);
					PTA.setSelectedList(pointlist, selectedList);
					PTA.setAnalyzedImp(imp);
				}
			});			
		}

		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()) return; // to avoid overlapping procedure
			if (cFrame==null) cFrame = new chartFrame();
			cFrame.setLayout(new GridLayout(3,2));

			int index = jt.convertRowIndexToModel(jt.getSelectedRow());
			selectedList = jt.getSelectedRows();
			for(int ind=0;ind<selectedList.length;ind++)
				selectedList[ind] = jt.convertRowIndexToModel(selectedList[ind]);
			PTA.setSelectedList(pointlist, selectedList);

			if (selectedList.length == 1) 
				drawTrajectory(pointlist.get(index));			
		}

	}

	class saveDataAction extends AbstractAction{
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			AbstractButton b = (AbstractButton)e.getSource();
			boolean subBg = false;
			if(PTA.isBg()) {
				GenericDialog gd = new GenericDialog("Subtract Backgroud");
				gd.addMessage("Do you want to apply calculateing the background?\n" +
				"(This process may take time if you have many points)");
				gd.addCheckbox("Calculate Background", false);
				gd.showDialog();
				subBg=gd.getNextBoolean();
			}
			if(b.getText() == "Save all") 
				saveAll(subBg);
			else if(b.getText() == "Save selected") 
				saveSelected(subBg);			
			else if(b.getText() == "Save checked") 
				saveChecked(subBg);
			else if(b.getText() == "Save Table as Text Data") 
				saveTableText();
			else if(b.getText() == "Save Table") 
				saveTable();
		}
		public void saveAll(boolean doSubBg) {
			SaveDialog sd = new SaveDialog("Save all",imp.getShortTitle()+"FIAllPoints",".txt");
			File file = new File(sd.getDirectory(),sd.getFileName());
			if(sd.getFileName()!=null) {
				try {
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
					List<FPoint> tmpPlist;
					for(int index=0;index<pointlist.size();index++) {
						tmpPlist=pointlist.get(jt.convertRowIndexToModel(index));
						writePointData(pointlist,tmpPlist,pw,doSubBg);
					}
					pw.close();
				} catch (IOException e1) {
					IJ.log(e1.toString());
				}
			}			
		}
		public void saveSelected(boolean doSubBg) {
			SaveDialog sd = new SaveDialog("Save selected points",imp.getShortTitle()+"FISelectedPoints",".txt");
			File file = new File(sd.getDirectory(),sd.getFileName());
			if(sd.getFileName()!=null) {
				try {
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
					List<FPoint> tmpPlist;
					for(int index=0;index<selectedList.length;index++) {
						tmpPlist=pointlist.get(selectedList[index]);
						writePointData(pointlist, tmpPlist,pw,doSubBg);
					}
					pw.close();
				} catch (IOException e1) {
					IJ.log(e1.toString());
				}
			}			
		}
		public void saveChecked(boolean doSubBg) {
			SaveDialog sd = new SaveDialog("Save checked points",imp.getShortTitle()+"FICheckedPoints",".txt");
			File file = new File(sd.getDirectory(),sd.getFileName());
			if(sd.getFileName()!=null) {
				try {
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
					List<FPoint> tmpPlist;
					for(int index=0;index<pointlist.size();index++) {
						if((Boolean) jt.getValueAt(index, 6) == Boolean.TRUE) {
							IJ.log("checked data index is = "+jt.convertRowIndexToModel(index));
							tmpPlist=pointlist.get(jt.convertRowIndexToModel(index));
							writePointData(pointlist, tmpPlist,pw,doSubBg);
						}
					}
					pw.close();
				} catch (IOException e1) {
					IJ.log(e1.toString());
				}
			}			
		}
		public void saveTableText() {
			SaveDialog sd = new SaveDialog("Save Table Data",imp.getShortTitle()+"TableData",".txt");
			File file = new File(sd.getDirectory(),sd.getFileName());
			if(sd.getFileName()!=null) {
				try {
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
					pw.println("#No From To FrameLength FIAverage RunLength Check Color");
					for(int row=0;row<jt.getRowCount();row++) {
						for(int column=0;column<jt.getColumnCount();column++) {
							if(column !=7)
								pw.print(jt.getValueAt(row, column));
							else {
								int i=0;
								for(Color cs:comboColors) {
									String s = cs.toString();
									if(s.equals(jt.getValueAt(row, column).toString()))
										break;
									i++;
								}
								pw.print(cString[i]);
							}
							pw.print(" ");
						}
						pw.println();
					}
					pw.close();
				} catch (IOException e1) {
					IJ.log(e1.toString());
				}
			}			
		}
		public void saveTable() {
			SaveDialog sd = new SaveDialog("Save Table Data",imp.getShortTitle()+"TableData",".dat");
			File file = new File(sd.getDirectory(),sd.getFileName());
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
				oos.writeObject(imp.getTitle());
				oos.writeObject(pointlist);
				resetDataFromJT(); // re-set data from current JT
				oos.writeObject(data);
				oos.close();
			} catch (Exception e1) {
				IJ.log(e1.toString());
			}			
		}
	}

	class anaMenuAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			AbstractButton b = (AbstractButton)e.getSource();
			if(b.getText() == "calc MSD") {
				GenericDialog gd = new GenericDialog("Calculate MSD");
				gd.addNumericField("delta "+cal.getTimeUnit()+" length for MSD calc", 5, 0);
				gd.addCheckbox("Linear(true) or Poly2(false)", true);
				gd.showDialog();
				if(gd.wasCanceled()) return;
				double leastlenTime = (double)gd.getNextNumber();
				boolean isLinear =gd.getNextBoolean();
				FitMSD fitmsd = new FitMSD(pointlist, cal);
				ArrayList<MSDdata> reslist = fitmsd.doMSDanalysis(selectedList, leastlenTime, isLinear);
				String lp;
				if (isLinear)
					lp = "Linear";
				else 
					lp = "Poly2";
				SaveDialog sd = new SaveDialog("Save MSD Data",imp.getShortTitle()+"MSDData"+lp,".txt");
				File file = new File(sd.getDirectory(),sd.getFileName());
				try {
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
					if(isLinear) {
						IJ.log("y=a+bx");
						IJ.log("a b R^2");
					} else {
						IJ.log("y=a+bx+cx^2");
						IJ.log("a b c R^2");
					}
					for(MSDdata res:reslist) {
						StringBuilder sb = new StringBuilder();
						sb.append("Point");
						sb.append(res.getID());
						sb.append(": ");
						for(double msd:res.getFullMSD()) {
							sb.append(msd);
							sb.append(" ");
						}
						pw.println(sb.toString());
						if(PTA.isDebug())
							IJ.log(sb.toString());
						if(isLinear) {
							IJ.log(res.getA()+" "+res.getB()+" "+res.getR());
						} else {
							IJ.log(res.getA()+" "+res.getB()+" "+res.getC()+" "+res.getR());						
						}
					}
					pw.close();
				} catch (IOException e1) {
					IJ.log(e1.toString());
				}
			} else if(b.getText() == "calc Velocity") {

				SaveDialog sd = new SaveDialog("Save Velocity Data",imp.getShortTitle()+"VelocityData",".txt");
				File file = new File(sd.getDirectory(),sd.getFileName());
				try {
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
					for(int index:selectedList) {
						if(pointlist.get(index).size()<2) continue; // if the length of pointlist is less than leastlen, skip it
						CalcVelocity cv = new CalcVelocity(pointlist.get(index),cal);
						StringBuilder sb = new StringBuilder();
						sb.append("Point");
						sb.append(index);
						sb.append(": ");
						for(double cvdata:cv.getVelocity()) {
							sb.append(cvdata);
							sb.append(" ");
						}
						pw.println(sb.toString());
						if(PTA.isDebug())
							IJ.log(sb.toString());
					}
					pw.close();
				} catch (IOException e1) {
					IJ.log(e1.toString());
				}
			} else if(b.getText() == "Re-fit by 2DGauss") {
				GenericDialog gd = new GenericDialog("Re-fit by 2DGauss");
				gd.addMessage("Are you really want to re-fit the data by 2DGaussian function?");
				gd.showDialog();
				if(gd.wasCanceled()) return;

				IJ.showStatus("Refitting by 2DGaussian");
				Roi preRoi = imp.getRoi();
				for(int index:selectedList) {
					List<FPoint> pl = pointlist.get(index);
					final int sizex = pl.get(0).getSizex();
					final int sizey = pl.get(0).getSizey();
					for(FPoint p:pl) {
						int currentFrame = p.getFrame();
						int x=p.getSx(),y=p.getSy();
						double dx=(double)x*cal.pixelWidth,dy=(double)y*cal.pixelHeight;
						imp.setSlice(currentFrame);
						ImageProcessor ip = imp.getProcessor();
						FloatProcessor fip = (FloatProcessor) ip.convertToFloat();
						double[] param = p.getParam();
						double[] fionaData = new double[sizex*sizey];
						double[] newParam = new double[6];
						float[] pixVal = (float[])fip.getPixels();
						int[] info = new int[2];
						info[1] = ptap.getIterationNumber();
						for(int ii=0;ii<sizex*sizey;ii++) {
							int ix=ii%sizex,iy=ii/sizex;
							fionaData[ix+iy*sizex] = (double)pixVal[x+ix+(y+iy)*imp.getWidth()];				
						}
						imp.setRoi(new Roi(x,y,sizex,sizey));

						param[1] /= cal.pixelWidth;
						param[2] /= cal.pixelHeight;
						param[3] = sizex/2.0D;
						param[4] = sizey/2.0D;
						newParam = PTA.fit2DGauss(fionaData,param,sizex,sizey,info); //DO 2d Gaussian Fitting!
						newParam[3] = (newParam[3]+x)*cal.pixelWidth;newParam[4]=(newParam[4]+y)*cal.pixelHeight;
						newParam[1] *=cal.pixelWidth;
						newParam[2] *=cal.pixelHeight;
						if(info[0]==1
								&& newParam[0]>ptap.getMinIntensity()
								&& newParam[1]>0 && newParam[2]>0		
								&& newParam[1]<=(double)sizex*cal.pixelWidth && newParam[2]<=(double)sizey*cal.pixelHeight
								&& (newParam[3]-dx)<(double)sizex*cal.pixelWidth && (newParam[3]-dx)>0
								&& (newParam[4]-dy)<(double)sizey*cal.pixelHeight && (newParam[4]-dy)>0) 
						{
							p.setParam(newParam);
							p.setInfo(info);
							if(PTA.isDebug())
								IJ.log("info:"+Arrays.toString(info)+", param:"+Arrays.toString(newParam));
						} else {
							if(PTA.isDebug()) {
								IJ.log("*info:"+Arrays.toString(info)+", param:"+Arrays.toString(newParam));
							}
							IJ.log("fitting error occured: "+index+" frame: "+currentFrame);
//							param[3]=(param[3]+x)*cal.pixelWidth;
//							param[4]=(param[4]+y)*cal.pixelHeight;
//							param[1] *= cal.pixelWidth;
//							param[2] *= cal.pixelHeight;
//							p.setParam(param);
						}
					}
					double avebg=0;
					if(PTA.isBg()) {
						CalcBGData cbg = new CalcBGData(pl,cal,imp);
						avebg=cbg.aveBg();
					}
					FPointStatics fps = new FPointStatics(pl,avebg);
					jt.setValueAt(fps.getAveFI(),jt.convertRowIndexToView(index),4);
					jt.setValueAt(fps.getRunlength(), jt.convertRowIndexToView(index),5);
				}
				imp.setRoi(preRoi);
				IJ.showStatus("Re-Fitting has done");
				drawTrajectory(pointlist.get(jt.convertRowIndexToModel(jt.getSelectedRows()[0])));
			} else if (b.getText() == "Extract point along LineRoi") {
				Roi roi = imp.getRoi();
				if (roi==null) {
					IJ.error("No Roi");
					return;
				}
				GenericDialog gd = new GenericDialog("Extract point along the Line");
				gd.addNumericField("Distance Range (+-Pixels)", 1, 1);
				gd.addNumericField("Around distance (+-Pixels)", 5, 1);
				gd.showDialog();
				if(gd.wasCanceled()) return;
				double Distance = gd.getNextNumber();
				double aroundDistance = gd.getNextNumber();
				Rectangle r = roi.getBounds();
				int[] xc;
				int[] yc;
				int num;
				if(roi.getType()==Roi.POLYLINE) {
					PolygonRoi proi = (PolygonRoi)roi;
					xc = proi.getXCoordinates();
					yc = proi.getYCoordinates();
					num = proi.getNCoordinates();
				} else if (roi.getType()==Roi.LINE){
					Line lroi = (Line)roi;
					xc = new int[2];
					yc = new int[2];
					xc[0]=lroi.x1;xc[1]=lroi.x2;yc[0]=lroi.y1;yc[1]=lroi.y2;num=2;
					r.x=r.y=0;
				} else {
					IJ.error("Roi must be Line or segemented Line");
					return;
				}
				for(int index=0;index<pointlist.size();index++) {
					List<FPoint> pl = pointlist.get(index);
					double len=1000;
					for(int i=0;i<num-1;i++) {
						PlRelation plr = new PlRelation(
								(double)xc[i]+r.x,(double)yc[i]+r.y,
								(double)xc[i+1]+r.x,(double)yc[i+1]+r.y,
								pl.get(0).getCx()/cal.pixelWidth,pl.get(0).getCy()/cal.pixelHeight);
						double tmplen = plr.retDistanceLineAndPoint();
						if(tmplen<len) len = tmplen;
					}
					if(PTA.isDebug())
						IJ.log("len="+len+", limitdistance ="+Distance+", farDistance="+aroundDistance);
					if(len<Distance) {
						jt.setValueAt(new Boolean(true), jt.convertRowIndexToView(index),6 );
						jt.setValueAt(Color.green, jt.convertRowIndexToView(index),7);
					} else if(len<(Distance+aroundDistance)) {
						jt.setValueAt(Color.magenta,jt.convertRowIndexToView(index),7);
					} else {
						jt.setValueAt(Color.cyan,jt.convertRowIndexToView(index),7);					
					}
				}
			} 
		}
	}

	class checkMenuAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			AbstractButton b = (AbstractButton)e.getSource();
			GenericDialog gd = new GenericDialog(b.getText()+" selected");
			gd.addMessage(b.getText()+" selected?");
			gd.showDialog();
			if(gd.wasCanceled()) return;

			for(int index:selectedList) {
				if(b.getText() == "Uncheck") {
					jt.setValueAt(new Boolean(false),jt.convertRowIndexToView(index),6);
				} else if (b.getText() == "Check") {
					jt.setValueAt(new Boolean(true),jt.convertRowIndexToView(index),6);
				} else if (b.getText() == "Reverse") {
					Boolean tmpb=(Boolean)jt.getValueAt(jt.convertRowIndexToView(index), 6);
					if (PTA.isDebug())
						IJ.log("index:"+index+", "+tmpb.toString());
					if (tmpb.equals(Boolean.TRUE))
						jt.setValueAt(new Boolean(false), jt.convertRowIndexToView(index), 6);
					else if (tmpb.equals(Boolean.FALSE))
						jt.setValueAt(new Boolean(true), jt.convertRowIndexToView(index), 6);
				}
			}
		}
	}

	class statMenuAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			AbstractButton b = (AbstractButton)e.getSource();
			int bins = 1,pointscnt=0;
			boolean subBg = false;
			if(PTA.isBgSub()) {
				GenericDialog gd = new GenericDialog("Subtract Backgroud");
				gd.addMessage("Do you want to apply calculateing the background?\n" +
				"(This process may take time if you have many points)");
				gd.addCheckbox("Calculate Background", false);
				gd.showDialog();
				subBg=gd.getNextBoolean();
			}
			if(b.getText() == "All points of statistics") {
				// make histoData
				FIList = new double[pointlist.size()];
				durationList = new double[pointlist.size()];
				runLengthList = new double[pointlist.size()];
				aveXList = new double[pointlist.size()];
				sdXList = new double[pointlist.size()];
				aveYList = new double[pointlist.size()];
				sdYList = new double[pointlist.size()];
				sdFIList = new double[pointlist.size()];
				aveOstList = new double[pointlist.size()];
				sdOstList = new double[pointlist.size()];
				FPointStatics fps;

				for(int i=0;i<pointlist.size();i++) {
					double avebg=0;
					if(PTA.isBgSub() && subBg) {
						CalcBGData cbg = new CalcBGData(pointlist.get(i),cal,imp);
						avebg=cbg.aveBg();
					}
					fps = new FPointStatics(pointlist.get(i),avebg);
					FIList[i] = fps.getAveFI();
					sdFIList[i] = fps.getSdFI();
					durationList[i] = fps.getTotalFrame()*cal.frameInterval;
					runLengthList[i] = fps.getRunlength();
					aveXList[i] = fps.getAveX();
					sdXList[i] = fps.getSdX();
					aveYList[i] = fps.getAveY();
					sdYList[i] = fps.getSdY();
					aveOstList[i] = fps.getAveOffset();
					sdOstList[i] = fps.getSdOffset();
				}
				pointscnt = pointlist.size();
			} else if (b.getText() == "Checked points of statistics") {
				int lenChecked=0;
				for(int i=0;i<pointlist.size();i++)
					if((Boolean)jt.getValueAt(i, 6)) lenChecked++;

				FIList = new double[lenChecked];
				durationList = new double[lenChecked];
				runLengthList =new double[lenChecked];
				aveXList = new double[lenChecked];
				sdXList = new double[lenChecked];
				aveYList = new double[lenChecked];
				sdYList = new double[lenChecked];
				sdFIList = new double[lenChecked];
				aveOstList = new double[lenChecked];
				sdOstList = new double[lenChecked];
				FPointStatics fps;
				for(int i=0,j=0;i<pointlist.size();i++) {
					if((Boolean)jt.getValueAt(i, 6)) {
						double avebg=0;
						if(PTA.isBg() && subBg) {
							CalcBGData cbg = new CalcBGData(pointlist.get(i),cal,imp);
							avebg=cbg.aveBg();
						}
						fps = new FPointStatics(pointlist.get(i),avebg);
						FIList[j] = fps.getAveFI();
						sdFIList[j] = fps.getSdFI();
						durationList[j] = (double)fps.getTotalFrame()*cal.frameInterval;
						runLengthList[j] = fps.getRunlength();
						aveXList[j] = fps.getAveX();
						sdXList[j] = fps.getSdX();
						aveYList[j] = fps.getAveY();
						sdYList[j] = fps.getSdY();
						aveOstList[j] = fps.getAveOffset();
						sdOstList[j] = fps.getSdOffset();
						j++;
					}
				}
				pointscnt = lenChecked;
			} else if (b.getText() == "show Statics") {
				IJ.log("PointIndex totalFrame runLength averageX s.d.X averageY s.d.Y averageFI sdFI averageOffset sdOffset");
				for(int index:selectedList) {
					List<FPoint> pl = pointlist.get(index);
					double avebg=0;
					if(PTA.isBg() && subBg) {
						CalcBGData cbg = new CalcBGData(pl,cal,imp);
						avebg=cbg.aveBg();
					}
					IJ.log(index+" "+new FPointStatics(pl,avebg).toString());
				}
			} else 
				return;
			bins = (int) (1+Math.log10(pointscnt)/Math.log10(2)); //bin width determined by Starjes law
			HistogramDataset FIHistoData = new HistogramDataset();
			HistogramDataset sdFIHistoData = new HistogramDataset();
			HistogramDataset durationHistoData = new HistogramDataset();
			HistogramDataset runLengthHistoData = new HistogramDataset();
			HistogramDataset aveXHistoData = new HistogramDataset();
			HistogramDataset sdXHistoData = new HistogramDataset();
			HistogramDataset aveYHistoData = new HistogramDataset();
			HistogramDataset sdYHistoData = new HistogramDataset();
			HistogramDataset aveOstHistoData = new HistogramDataset();
			HistogramDataset sdOstHistoData = new HistogramDataset();

			FIHistoData.addSeries("FI", FIList, bins);
			sdFIHistoData.addSeries("s.d. FI", sdFIList, bins);
			durationHistoData.addSeries("Duration", durationList, bins);
			runLengthHistoData.addSeries("Run Length", runLengthList, bins);
			aveXHistoData.addSeries("Average X", aveXList, bins);
			sdXHistoData.addSeries("s.d. X", sdXList, bins);
			aveYHistoData.addSeries("Average Y", aveYList, bins);
			sdYHistoData.addSeries("s.d. Y", sdYList, bins);
			aveOstHistoData.addSeries("Average Offset", aveOstList, bins);
			sdOstHistoData.addSeries("s.d. Offset", sdOstList, bins);

			JFreeChart FIHistoChart = ChartFactory.createHistogram(null, "Fluorescence Intensity [a.u.]", "Number of Points", FIHistoData
					, PlotOrientation.VERTICAL, false, false, false);
			JFreeChart sdFIHistoChart = ChartFactory.createHistogram(null, "s.d. of Fluorescence Intensity [a.u.]", null, sdFIHistoData
					, PlotOrientation.VERTICAL, false, false, false);
			JFreeChart aveOstHistoChart = ChartFactory.createHistogram(null, "Average Offset Intensity [a.u.]", null, aveOstHistoData
					, PlotOrientation.VERTICAL, false, false, false);
			JFreeChart sdOstHistoChart = ChartFactory.createHistogram(null, "s.d. Offset Intensity [a.u.]", null, sdOstHistoData
					, PlotOrientation.VERTICAL, false, false, false);
			JFreeChart durationHistoChart = ChartFactory.createHistogram(null, "Duration ["+cal.getTimeUnit()+"]", null, durationHistoData
					, PlotOrientation.VERTICAL, false, false, false);
			JFreeChart runLengthHistoChart = ChartFactory.createHistogram(null, "Run Length ["+cal.getUnit()+"]", "Number of Points", runLengthHistoData
					, PlotOrientation.VERTICAL, false, false, false);
			JFreeChart aveXHistoChart = ChartFactory.createHistogram(null, "Average X ["+cal.getUnit()+"]", null, aveXHistoData
					, PlotOrientation.VERTICAL, false, false, false);
			JFreeChart sdXHistoChart = ChartFactory.createHistogram(null, "s.d. X ["+cal.getUnit()+"]", null, sdXHistoData
					, PlotOrientation.VERTICAL, false, false, false);
			JFreeChart aveYHistoChart = ChartFactory.createHistogram(null, "Average Y ["+cal.getUnit()+"]", null, aveYHistoData
					, PlotOrientation.VERTICAL, false, false, false);
			JFreeChart sdYHistoChart = ChartFactory.createHistogram(null, "s.d. Y ["+cal.getUnit()+"]", null, sdYHistoData
					, PlotOrientation.VERTICAL, false, false, false);
			if (histoFrame == null) {
				histoFrame = new JFrame("Histogram [Number of Points:"+pointscnt+"]");
				histoFrame.setBounds(0,500,1200,300);
				histoFrame.setLayout(new GridLayout(2,5));
				//				histoFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			} 
			if(FIHistoPanel == null) {
				FIHistoPanel = new ChartPanel(FIHistoChart);
				sdFIHistoPanel = new ChartPanel(sdFIHistoChart);
				aveOstHistoPanel = new ChartPanel(aveOstHistoChart);
				sdOstHistoPanel = new ChartPanel(sdOstHistoChart);
				durationHistoPanel = new ChartPanel(durationHistoChart);
				runLengthHistoPanel = new ChartPanel(runLengthHistoChart);
				aveXHistoPanel = new ChartPanel(aveXHistoChart);
				sdXHistoPanel = new ChartPanel(sdXHistoChart);
				aveYHistoPanel = new ChartPanel(aveYHistoChart);
				sdYHistoPanel = new ChartPanel(sdYHistoChart);

				histoFrame.getContentPane().add(FIHistoPanel);
				histoFrame.getContentPane().add(sdFIHistoPanel);
				histoFrame.getContentPane().add(aveOstHistoPanel);
				histoFrame.getContentPane().add(sdOstHistoPanel);
				histoFrame.getContentPane().add(durationHistoPanel);
				histoFrame.getContentPane().add(runLengthHistoPanel);
				histoFrame.getContentPane().add(aveXHistoPanel);
				histoFrame.getContentPane().add(sdXHistoPanel);
				histoFrame.getContentPane().add(aveYHistoPanel);
				histoFrame.getContentPane().add(sdYHistoPanel);
			} else {
				WindowManager.removeWindow(histoFrame);
				histoFrame.setTitle("Histogram [Number of Points:"+pointscnt+"]");
				FIHistoPanel.setChart(FIHistoChart);
				sdFIHistoPanel.setChart(sdFIHistoChart);
				aveOstHistoPanel.setChart(aveOstHistoChart);
				sdOstHistoPanel.setChart(sdOstHistoChart);
				durationHistoPanel.setChart(durationHistoChart);
				runLengthHistoPanel.setChart(runLengthHistoChart);
				aveXHistoPanel.setChart(aveXHistoChart);
				sdXHistoPanel.setChart(sdXHistoChart);
				aveYHistoPanel.setChart(aveYHistoChart);
				sdYHistoPanel.setChart(sdYHistoChart);
				WindowManager.addWindow(histoFrame);
			}
			if(!histoFrame.isVisible()) {
				histoFrame.setVisible(true);
				WindowManager.addWindow(histoFrame);
			}
		}
	}
	/*
	 * write Point data
	 */
	private void writePointData(List<List<FPoint>> plist, List<FPoint> tmpPlist,PrintWriter pw,boolean doSubBg) throws IOException {
		pw.println(String.format("#Point:%d -- -- -- -- -- -- -- --",plist.indexOf(tmpPlist)));
		pw.println("#Frame x y sx sy F.I.(I) F.I.(II) offset Iteration");
		int frame=0;
		double avebg=0,firoi=0;
		for(FPoint tmpP:tmpPlist) {
			if(PTA.isBg() && doSubBg) {
				CalcBGData cbg = new CalcBGData(tmpPlist,cal,imp);
				avebg=cbg.aveBg();
			}
			if(PTA.isBgSub() && doSubBg)
				firoi=tmpP.getRoiInt()-avebg;
			else
				firoi=tmpP.getRoiInt();
			pw.println(String.format("%d %f %f %f %f %f %f %f %d", 
					tmpP.getFrame(),tmpP.getCx(),tmpP.getCy(),
					tmpP.getParam()[1],tmpP.getParam()[2],firoi,tmpP.getParam()[0],
					tmpP.getParam()[5],tmpP.getInfo()[1]));
			frame = tmpP.getFrame();
		}
		if(PTA.isBg() && doSubBg) {
			CalcBGData cbg = new CalcBGData(tmpPlist,cal,imp);
			for(int i=1;i<=cbg.getBGlen();i++) {
				if(PTA.isBgSub())
					pw.println(String.format("%d -- -- -- -- %f -- -- --",frame+i,cbg.getBGdata()[i-1]-avebg));
				else
					pw.println(String.format("%d -- -- -- -- %f -- -- --",frame+i,cbg.getBGdata()[i-1]));
			}
		}
	}
	public Color getDataofColor(int index) {
		Object col = jt.getValueAt(jt.convertRowIndexToView(index), 7);
		if(col instanceof String)
			return Color.cyan;
		else
			return (Color)col;
	}

	class chartFrame extends JFrame {
		private static final long serialVersionUID = 1L;

		chartFrame() {
			setLayout(new GridLayout(2,3));
			setBounds(500, 10, 500, 500);
			setTitle("Trajectories");
		}
	}

	public void setFrame(int f) {
		if(frame==null) return;
		int index;
		/*
		 * domain and range crosshair is changed depending on the frame
		 */
		index=Arrays.binarySearch(frame, f*cal.frameInterval);
		if(index>=0) {
			xyXyplot.setDomainCrosshairValue(x[index]);
			xyXyplot.setRangeCrosshairValue(y[index]);
			xyFiplot.setDomainCrosshairValue(frame[index]);
			xyFiplot.setRangeCrosshairValue(fi[index]);
			xyXplot.setDomainCrosshairValue(frame[index]);
			xyXplot.setRangeCrosshairValue(x[index]);
			xyYplot.setDomainCrosshairValue(frame[index]);
			xyYplot.setRangeCrosshairValue(y[index]);
		}
	}
	private XYPlot configXYchart(JFreeChart xyLineChart) {
		XYPlot xyplot = xyLineChart.getXYPlot();
		xyplot.setDomainCrosshairVisible(true);
		xyplot.setRangeCrosshairVisible(true);
		NumberAxis xAxis = (NumberAxis)xyplot.getDomainAxis();
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = (NumberAxis)xyplot.getRangeAxis();
		yAxis.setAutoRangeIncludesZero(false);

		return xyplot;
	}

	public void setPointlist(List<List<FPoint>> pointlist) {
		this.pointlist=pointlist;
	}
	public List<List<FPoint>> getPointlist(){
		return this.pointlist;
	}
	// update "data"
	private void resetDataFromJT() {
		for(int row=0;row<jt.getRowCount();row++)
			for(int column=0;column<jt.getColumnCount();column++)				
				data[row][column]=jt.getValueAt(row,column);
	}

	public String toString() {
		return new String("fpDataName is:"+imp.getTitle());
	}

	public double retDistanceLineAndPoint(double l1x,double l1y,
			double l2x,double l2y,double px,double py) {
		double dx = l2x-l1x;
		double dy = l2y-l1y;
		double a = dx*dx+dy*dy;
		if(a==0)
			return Math.sqrt((px-l1x)*(px-l1x)+(py-l1y)*(py-l1y));
		double b = dx*(l1x-px)+dy*(l1y-py);
		double t = -(b/a);
		if (t<0.0D) t=0.0D;
		if (t>1.0D) t=1.0D;
		double x=t*dx+l1x;
		double y=t*dy+l1y;
		return Math.sqrt((x-px)*(x-px)+(y-py)*(y-py));
	}
//	public ArrayList<MSDdata> doMSDanalysis(
//			List<List<FPoint>> pointlist, 
//			int[] selectedList, 
//			double leastlenTime,
//			boolean isLinear
//			){
//		int leastlen = (int)(leastlenTime/cal.frameInterval);
//		leastlen = leastlen<=3?3:leastlen;		
//		
//		ArrayList<MSDdata> msdresults = new ArrayList<MSDdata>();
//		for(int index:selectedList) {
//			if(pointlist.get(index).size()<leastlen) continue; // if the length of pointlist is less than leastlen, skip it.
//
//			CalcMSD cm = new CalcMSD(pointlist.get(index),leastlen,cal);
//
//			double[] fullDF = cm.getDFrame();
//			double[] fullMSD = cm.getMsdList();
//			double[] x = Arrays.copyOfRange(fullDF, 0, leastlen);
//			double[] y = Arrays.copyOfRange(fullMSD,0,leastlen);
//			CurveFitter cv = new CurveFitter(x,y);
//			if(isLinear) {
//				cv.doFit(CurveFitter.STRAIGHT_LINE);
//				//IJ.log(cv.getParams()[0]+" "+cv.getParams()[1]+" "+cv.getRSquared());
//				msdresults.add(new MSDdata(index, fullDF, fullMSD, cv.getParams()[0], cv.getParams()[1], cv.getRSquared()));
//				
//			} else {
//				cv.doFit(CurveFitter.POLY2);
//			//	IJ.log(cv.getParams()[0]+" "+cv.getParams()[1]+" "+cv.getParams()[2]+" "+cv.getRSquared());						
//				msdresults.add(new MSDdata(index, fullDF, fullMSD, cv.getParams()[0], cv.getParams()[1], cv.getParams()[2], cv.getRSquared()));
//			}
//		}
//		return msdresults;		
//	}
	
}
class ColorTableRenderer extends DefaultTableCellRenderer{
	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable tb,
			Object val,boolean isSelected,
			boolean hasFocus,int r,int c){

		Component returnMe = super.getTableCellRendererComponent(tb, val, isSelected, hasFocus, r,c);

		if (val instanceof Color) {
			Color color = (Color)val;
			returnMe.setBackground(color);
			if (returnMe instanceof JLabel) {
				JLabel jl = (JLabel)returnMe;
				jl.setOpaque(true);
				jl.setText(" ");
			}
		}
		return returnMe;
	}
}
class MyCellRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;

	MyCellRenderer(){
		setOpaque(true);
	}

	public Component getListCellRendererComponent(
			JList list,
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus){

		setText(" ");

		if (isSelected){
			setForeground(Color.black);
			setBackground((Color)value);
		}else{
			setForeground(Color.white);
			setBackground((Color)value);
		}
		return this;
	}
	
}

