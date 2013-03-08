package pta;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.io.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import pta.data.*;
import pta.gui.*;
import pta.track.DetectParticle;


		
import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.measure.Calibration;
import ij.plugin.frame.*;
import ij.process.ImageProcessor;

/*
 * Particle Track and Analysis (PTA)
 * developed by Yoshiyuki Arai
 * PTA Particle Track and Analysis Copyright (2010) Yoshiyuki Arai. All rights reserved.
 */

public class PTA extends PlugInFrame{

	private static final long serialVersionUID = 1L;
	public native static double[] fit2DGauss(double[] y, double[] x, int sizex, int sizey, int[] info);
	// Load fit2DGauss Library
	static {
		System.loadLibrary("fit2DGauss");
		isDetectionPerformed = false;
		pInfo = null;
	}

	// Vector list for fionaPoints of all of frames.
	private static List<List<FPoint>> pointlist;
	private static int[] selectedList;

//	private PtaParam ptap = new PtaParam.Builder(12,12, false,false).build();
	private PtaParam ptap = new PtaParam.Builder(12,12, false).build();

	private Roi scanAreaRoi;

	private static ImagePlus imp;
	private static ImagePlus analyzedImp; // imp for analyzed table
	private static ImageCanvas ic;
	private static Calibration cal;
	private DetectParticle dp;

	private static ShowPInfo pInfo;
	private static ShowPdata pData;
	private static Frame frame;
	private ImageListener listener;
	private ParamChangeListener pcl; 
	private double maxThreshold, minThreshold;
	private boolean isParamChanged;
	private static boolean isDetectionPerformed;
	// Variables declaration - do not modify
	private JPanel jAppearPanel;
	private JPanel jIntensityPanel;
	private JPanel jTrackPanel;
	private JPanel jTrackSubPanel1;
	private JPanel jTrackSubPanel2;
	private JPanel jOthersPanel;
	private JPanel jParamPanel;
	private JPanel jParamPanelBasic;
	private JPanel jParamPanelAdvanced;
	// for variable x and y RoiSize
	private JPanel jParamPanelRoiSizex;
	private JPanel jParamPanelRoiSizey;
	//
	private JPanel jPanel1;
	private JCheckBox j2DGaussCheckbox;
	private JCheckBox jAllFramesCheckbox;
	private JCheckBox jRoiCheckBox;
	private JCheckBox jNumberCheckBox;
	private JCheckBox jAllCheckBox;
	// test for LMA with JAMA
//	private JCheckBox jLmaCheckbox;
	//
	private JCheckBox jAutomaticTrackCheckBox;
	private static JCheckBox jDebugCheckbox;
	private static JComboBox jDetectMethodCombobox;
	private static JCheckBox jBgCheckBox;
	private static JCheckBox jSubBgCheckBox;
	private JButton jTrackButton;
	private JButton jPreviewButton;
	private JButton jUpdateTableButton;
	private JLabel jRoiSizexLabel;
	private JLabel jRoiSizeyLabel;
	private JLabel jLabel10;
	private JLabel jLabel11;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JLabel jLabel5;
	private JLabel jLabel6;
	private JLabel jLabel7;
	private JLabel jLabel9;
	//variable x and y roiSize
	private JSpinner jRoiSizexSpinner;
	private JSpinner jRoiSizeySpinner;
	//
	private JSpinner jMinIntSpinner;
	private JSpinner jMinSizeSpinner;
	private JSpinner jNearestRangeSpinner;
	private JSpinner jMaxLinkFraSpinner;
	private JSpinner jKurtosisSpinner;
	private JSpinner jIteSpinner;
	private JSpinner jSearchIncSpinner;
	private static JSpinner jBgFrameSpinner;
	private ButtonGroup buttonGroup0;
	private static JRadioButton jRadioROIButton;
	private JRadioButton jRadioGIButton; 
	private ButtonGroup buttonGroup1;
	private JRadioButton jRadioAlwaysButton;
	private JRadioButton jRadioGrowingButton;
	private JRadioButton jRadioNoneButton;
	private JButton jLoadButton;
	private JButton jLoadParamButton;
	private JButton jSaveParamButton;
	private JPanel jShowPathPanel;
	private JPanel jShowRoiPanel;
	// End of variables declaration
	public PTA() {
		super("PTA");
		// VersionCheck
		if (IJ.versionLessThan("1.43g"))
			return;
		if (frame != null){
			IJ.error("PTA is already implemented");
			return;
		}
		frame = this;

		// panel coordinate

		imp = WindowManager.getCurrentImage();
		// open the Threshold Adjuster
		ThresholdAdjuster ta = new ThresholdAdjuster();
		ta.setEnabled(true);
		// if the image is already opened
		if (imp != null) {
			ImageProcessor ip = imp.getProcessor();
			//to avoid automatic point detection
			ip.setThreshold(-808080.0D, ip.getMax(), ImageProcessor.RED_LUT);
			ic=imp.getCanvas();
			cal = imp.getCalibration();
			ic.addMouseListener(new icMouseAdapter());
			ic.addKeyListener(new icKeyAdapter());
		}
		initComponents();
		GUI.center(this);
		setVisible(true);
		WindowManager.addWindow(this);
		ImagePlus.addImageListener(listener);
	}
	private void initComponents() {

		pcl = new ParamChangeListener();
		buttonGroup1 = new ButtonGroup();
		buttonGroup0 = new ButtonGroup();
		jPanel1 = new JPanel();
		jTrackPanel = new JPanel();
		jTrackSubPanel1 = new JPanel();
		jTrackSubPanel2 = new JPanel();
		jTrackButton = new JButton();
		jPreviewButton = new JButton();
		j2DGaussCheckbox = new JCheckBox();
		jAutomaticTrackCheckBox = new JCheckBox();
		jAllFramesCheckbox = new JCheckBox();
		//test for LMA with JAMA
//		jLmaCheckbox = new JCheckBox();
		//
		jAppearPanel = new JPanel();
		jShowPathPanel = new JPanel();
		jIntensityPanel = new JPanel();
		jLabel11 = new JLabel();
		jRadioAlwaysButton = new JRadioButton();
		jRadioGrowingButton = new JRadioButton();
		jRadioNoneButton = new JRadioButton();
		jRadioROIButton = new JRadioButton();
		jRadioGIButton = new JRadioButton();
		jUpdateTableButton = new JButton();
		jShowRoiPanel = new JPanel();
		jRoiCheckBox = new JCheckBox();
		jNumberCheckBox = new JCheckBox();
		jAllCheckBox = new JCheckBox();
		jOthersPanel = new JPanel();
		jLoadButton = new JButton();
		jLoadParamButton = new JButton();
		jSaveParamButton = new JButton();
		jDebugCheckbox = new JCheckBox();
		jBgCheckBox = new JCheckBox();
		jSubBgCheckBox = new JCheckBox();
		String[] detectList = {"Centroid","CenterOfMass"};
		jDetectMethodCombobox = new JComboBox(detectList);
		jParamPanel = new JPanel();
		jParamPanelBasic = new JPanel();
		jParamPanelAdvanced = new JPanel();
		// variable x and y size of Roi
		jParamPanelRoiSizex = new JPanel();
		jParamPanelRoiSizey = new JPanel();

		jRoiSizexLabel = new JLabel();
		jRoiSizexSpinner = new JSpinner(new SpinnerNumberModel(12,5,50,1));
		jRoiSizeyLabel = new JLabel();
		jRoiSizeySpinner = new JSpinner(new SpinnerNumberModel(12,5,50,1));
		//
		jLabel2 = new JLabel();
		jMinIntSpinner = new JSpinner(new SpinnerNumberModel(ptap.getMinIntensity(),0,65535,1));
		jLabel3 = new JLabel();
		jMinSizeSpinner = new JSpinner(new SpinnerNumberModel(ptap.getMinSize(),3,100,1));
		jLabel5 = new JLabel();
		jNearestRangeSpinner = new JSpinner(new SpinnerNumberModel(ptap.getNearstRange(),0.1,10,0.1));
		jLabel6 = new JLabel();
		jMaxLinkFraSpinner = new JSpinner(new SpinnerNumberModel(ptap.getLinkageFrame(),0,10,1));
		jLabel7 = new JLabel();
		jKurtosisSpinner = new JSpinner(new SpinnerNumberModel(ptap.getKurtosis(),-10,10,0.01));
		jLabel9 = new JLabel();
		jIteSpinner = new JSpinner(new SpinnerNumberModel(ptap.getIterationNumber(),1,1000,1));
		jLabel10 = new JLabel();
		jSearchIncSpinner = new JSpinner(new SpinnerNumberModel(ptap.getSearchPointIncrement(),1,100,1));

		jBgFrameSpinner = new JSpinner(new SpinnerNumberModel(10,1,100,1));
		
		listener = new ImageListener() {

			public synchronized void imageClosed(ImagePlus arg0) {
			}

			public synchronized void imageOpened(ImagePlus arg0) {
				maxThreshold = minThreshold = 0;
				// add MouseListner and KeyListener for ImageCanvas of newly opened image
				ImageCanvas nic=arg0.getCanvas();
				nic.addMouseListener(new icMouseAdapter());
				nic.addKeyListener(new icKeyAdapter());
				imp = arg0;
				ImageProcessor ip = imp.getProcessor();
				ip.setThreshold(-808080.0D, ip.getMax(), ImageProcessor.RED_LUT);
				cal = imp.getCalibration();
			}

			public synchronized void imageUpdated(ImagePlus arg0) {
				imp=arg0;
				cal = imp.getCalibration();
				if(isDetectionPerformed) return; //ignore when detection has been performed
				ImageProcessor ip = arg0.getProcessor();
				// procedure when parameters were changed
				if(isParamChanged && jAutomaticTrackCheckBox.isSelected()) {
					scanOneFrame(arg0);
					isParamChanged = false;
					if(isDebug())
						IJ.log("param changed");
					return;
				}
				// procedure when threshold was changed
				if(jAutomaticTrackCheckBox.isSelected() && ip.getMinThreshold() != -808080.0D && 
						(ip.getMaxThreshold() != maxThreshold 
								|| ip.getMinThreshold() != minThreshold)) {
					maxThreshold = ip.getMaxThreshold();minThreshold = ip.getMinThreshold();
					scanOneFrame(arg0);					
					if(isDebug()) {
						IJ.log("Threshold changed");
					}
					return;
				}

				//	ignore when detection has not done yet
				if(pData == null) {
					if(isDebug())
						IJ.log("pData is null");
					return;
				}
				if(selectedList == null) {
					if(isDebug())
						IJ.log("selectedList = null");
					return;
				}
				if(imp != analyzedImp) {
					if(isDebug())
						IJ.log("This image is not same the table image");
					return;
				}
				
				Overlay ol = new Overlay();
				if(selectedList.length<1 && !jAllCheckBox.isSelected())
					return;
				int len = jAllCheckBox.isSelected()?pointlist.size():selectedList.length;
				for(int slist=0;slist<len;slist++) {
					List<FPoint> focusedlist;
					if(jAllCheckBox.isSelected()) 
						focusedlist = pointlist.get(slist);
					else 
						focusedlist = pointlist.get(selectedList[slist]);
					int index=0;
					for(FPoint p:focusedlist) {
						if(p.getFrame()==arg0.getCurrentSlice()) {
							if(jRadioAlwaysButton.isSelected()) {
								GeneralPath gp = new GeneralPath();
								for(int i=0;i<focusedlist.size()-1;i++) {
									FPoint fp=focusedlist.get(i);
									FPoint sp=focusedlist.get(i+1);
									gp.moveTo(fp.getCx()/cal.pixelWidth,fp.getCy()/cal.pixelHeight);
									gp.lineTo(sp.getCx()/cal.pixelWidth,sp.getCy()/cal.pixelHeight);
								}
								ShapeRoi sr = new ShapeRoi(gp);
								sr.setStrokeColor(pData.getDataofColor(pointlist.indexOf(focusedlist)));
								ol.add(sr);
							} else if(jRadioGrowingButton.isSelected()) {
								GeneralPath gp = new GeneralPath();
								for(int i=0;i<focusedlist.indexOf(p);i++) {
									gp.moveTo(focusedlist.get(i).getCx()/cal.pixelWidth,focusedlist.get(i).getCy()/cal.pixelHeight);
									gp.lineTo(focusedlist.get(i+1).getCx()/cal.pixelWidth,focusedlist.get(i+1).getCy()/cal.pixelHeight);
								}
								ShapeRoi sr = new ShapeRoi(gp);
								sr.setStrokeColor(pData.getDataofColor(pointlist.indexOf(focusedlist)));
								ol.add(sr);
							} 
							if(jNumberCheckBox.isSelected()){ // show the number beside ROI
								int size = p.getSizex();
								Roi numRoi;
								numRoi = new TextRoi((int)(p.getCx()/cal.pixelWidth)+size/2+2, (int)(p.getCy()/cal.pixelHeight)-size/2,
										String.valueOf(pointlist.indexOf(focusedlist)),
										new Font("SansSerif",Font.PLAIN,10));
								numRoi.setStrokeColor(pData.getDataofColor(pointlist.indexOf(focusedlist)));
								ol.add(numRoi);
							}
							p.setColor(pData.getDataofColor(pointlist.indexOf(focusedlist)));
							if(jRoiCheckBox.isSelected()) {
								ol.add(p.retRoi());
							}

							if(pInfo == null || !pInfo.isVisible()) {
								pInfo = new ShowPInfo(jAllCheckBox.isSelected()?0:selectedList[0],focusedlist,index);
							}
							else {
								pInfo.setInfoData(jAllCheckBox.isSelected()?0:selectedList[0], focusedlist,index);
							}
							if(slist == 0 
									&& arg0.getCurrentSlice()>=focusedlist.get(0).getFrame() 
									&& arg0.getCurrentSlice()<=focusedlist.get(focusedlist.size()-1).getFrame())
								pData.setFrame(arg0.getCurrentSlice());
						}
						index++;
					}
				}
				imp.setOverlay(ol);
			}			
		};

		setResizable(false);
		setLayout(new GridLayout(1, 2));
		setSize(600,400);
		jPanel1.setLayout(new GridLayout(4, 1));

// set TrackPanel
		jTrackPanel.setBorder(BorderFactory.createTitledBorder("Particle Tracking"));
		jTrackPanel.setLayout(new GridLayout(2, 1));
		jTrackSubPanel1.setLayout(new GridLayout(1,3));
		jPreviewButton.setText("Preview");
		jPreviewButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if(imp != null && imp.getProcessor().getMinThreshold() != -808080.0D) {
					scanOneFrame(imp);
				}
			}
		});
		jTrackSubPanel1.add(jPreviewButton);
		jTrackButton.setText("Track");
		jTrackButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if(isDetectionPerformed) {
					IJ.error("Tracking is runnning!!");
					return;
				}
				if(cal.frameInterval == 0.0) {
					cal.frameInterval = 1;
					cal.setTimeUnit("frame");
				}
				imp = WindowManager.getCurrentImage();
				if(imp==null) return;
				setDetectionState(true);
				scanAreaRoi = new Roi(0,0,imp.getWidth(),imp.getHeight());
				Roi tmpAreaRoi = imp.getRoi();
				if(tmpAreaRoi != null && tmpAreaRoi.getType() == Roi.RECTANGLE)
					scanAreaRoi =imp.getRoi();
				dp = new DetectParticle(ptap,imp,scanAreaRoi,pData);
				ptap.setDo2dGaussfit(j2DGaussCheckbox.isSelected());
				// test by LMA with JAMA
//				ptap.setDo2dGaussfitbyLMA(jLmaCheckbox.isSelected());
				dp.setPtap(ptap);
				dp.setScanRoi(scanAreaRoi);
				if(jAllFramesCheckbox.isSelected())
					dp.setStackRange(1, imp.getStackSize());
				else {
					List<FPoint> dplist = new ArrayList<FPoint>();
					dp = new DetectParticle(ptap,imp,scanAreaRoi, dplist,true,pData);
				}
				dp.start();
			}			
		});
		jTrackSubPanel1.add(jTrackButton);
		// Disable detection method selection H22.12.05
//		jTrackSubPanel1.add(jDetectMethodCombobox);
//		jDetectMethodCombobox.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				if(jAutomaticTrackCheckBox.isSelected())
//					scanOneFrame(imp);
//			}
//		});
		jTrackPanel.add(jTrackSubPanel1);
		jTrackSubPanel2.setLayout(new GridLayout(2,2));
		j2DGaussCheckbox.setText("2-DGaussian Fit");
		jTrackSubPanel2.add(j2DGaussCheckbox);
		j2DGaussCheckbox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				ptap.setDo2dGaussfit(j2DGaussCheckbox.isSelected());
				if(jAutomaticTrackCheckBox.isSelected())
					scanOneFrame(imp);
			}
		});
		jAutomaticTrackCheckBox.setText("Auto-update");
		jTrackSubPanel2.add(jAutomaticTrackCheckBox);
		jTrackPanel.add(jTrackSubPanel2);
		jAllFramesCheckbox.setText("All Frames");
		jTrackSubPanel2.add(jAllFramesCheckbox);
//		jLmaCheckbox.setText("Fit by LMA with JAMA");
//		jTrackSubPanel2.add(jLmaCheckbox);
//		// ---from--- test for LMA
//		jLmaCheckbox.addActionListener(new ActionListener() {
//			
//			public void actionPerformed(ActionEvent e) {
//				ptap.setDo2dGaussfitbyLMA(jLmaCheckbox.isSelected());
//				if(jAutomaticTrackCheckBox.isSelected())
//					scanOneFrame(imp);
//			}
//		});
//		// ---to---
		jPanel1.add(jTrackPanel);

// set Appearance Panel
		jAppearPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Appearance"));
		jLabel11.setText("Path");
		jShowPathPanel.add(jLabel11);
		buttonGroup1.add(jRadioAlwaysButton);
		jRadioAlwaysButton.setText("Always");
		jRadioAlwaysButton.setSelected(true);
		jRadioAlwaysButton.addActionListener(new RadioAction());
		jShowPathPanel.add(jRadioAlwaysButton);
		buttonGroup1.add(jRadioGrowingButton);
		jRadioGrowingButton.setText("Growing");
		jRadioGrowingButton.addActionListener(new RadioAction());
		jShowPathPanel.add(jRadioGrowingButton);
		buttonGroup1.add(jRadioNoneButton);
		jRadioNoneButton.setText("None");
		jRadioNoneButton.addActionListener(new RadioAction());
		jShowPathPanel.add(jRadioNoneButton);
		jAppearPanel.add(jShowPathPanel);
		jShowRoiPanel.setLayout(new GridLayout(1, 3));
		jRoiCheckBox.setText("Roi");
		jShowRoiPanel.add(jRoiCheckBox);
		jRoiCheckBox.setSelected(true);
		jRoiCheckBox.addActionListener(new checkBoxAction());
		jNumberCheckBox.setText("Number");
		jShowRoiPanel.add(jNumberCheckBox);
		jNumberCheckBox.addActionListener(new checkBoxAction());
		jAllCheckBox.setText("All");
		jShowRoiPanel.add(jAllCheckBox);
		jAllCheckBox.addActionListener(new checkBoxAction());
		jAppearPanel.add(jShowRoiPanel);
		jPanel1.add(jAppearPanel);
		
// set background panel
		jIntensityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Intensity"));
		jIntensityPanel.setLayout(new GridLayout(3,2));
		buttonGroup0.add(jRadioROIButton);
		jRadioROIButton.setText("TypeI (ROI)");
		jRadioROIButton.setSelected(true);
		jRadioROIButton.addActionListener(new roiIntRadioAction());
		buttonGroup0.add(jRadioGIButton);
		jRadioGIButton.setText("TypeII");
		jRadioGIButton.addActionListener(new roiIntRadioAction());
		jIntensityPanel.add(jRadioROIButton);
		jIntensityPanel.add(jRadioGIButton);
		
		jBgCheckBox.setText("BG Show");
		jIntensityPanel.add(jBgCheckBox);
		jBgCheckBox.addActionListener(new bgChangeListener());
		jIntensityPanel.add(jBgFrameSpinner);
		jBgFrameSpinner.addChangeListener(new bgLenChangeListener());
		jSubBgCheckBox.setText("Sub. BG");
		jSubBgCheckBox.setEnabled(false);
		jSubBgCheckBox.addActionListener(new bgChangeListener());
		jIntensityPanel.add(jSubBgCheckBox);
		jUpdateTableButton.setText("Update Table");
		jUpdateTableButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				if(pData != null)
					pData.updateTableFI();
			}		
		});
		jIntensityPanel.add(jUpdateTableButton);

		jPanel1.add(jIntensityPanel);
		
		jOthersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Others"));
		jOthersPanel.setLayout(new FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
		jLoadParamButton.setText("Load Param");
		jOthersPanel.add(jLoadParamButton);
		jLoadParamButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				OpenDialog od = new OpenDialog("Load Parameters",null);
				File inFile = new File(od.getDirectory(),od.getFileName());
				ObjectInputStream ois;
				try {
					ois = new ObjectInputStream(new FileInputStream(inFile));
					ptap = (PtaParam)ois.readObject();
					ois.close();
					spinParamSet();
				} catch (Exception e1) {
					IJ.log(e1.toString());
				}
			}
		});
		jSaveParamButton.setText("Save Param");
		jOthersPanel.add(jSaveParamButton);
		jSaveParamButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				SaveDialog sd = new SaveDialog("Save Parameters","Parameters",".dat");
				File file = new File(sd.getDirectory(),sd.getFileName());
				ObjectOutputStream oos;
				if(sd.getFileName()!=null) {
					try {
						oos = new ObjectOutputStream(new FileOutputStream(file));
						oos.writeObject(ptap);
						oos.close();
					} catch (IOException e1) {
						IJ.log(e1.toString());
					}
				}	
			}
		});
		jLoadButton.setText("Load Data");
		jOthersPanel.add(jLoadButton);
		jLoadButton.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				OpenDialog od = new OpenDialog("Open Table data",null);
				File inFile = new File(od.getDirectory(),od.getFileName());
				ObjectInputStream ois;
				try {
					ois = new ObjectInputStream(new FileInputStream(inFile));
					String impName = (String)ois.readObject();					
					List<List<FPoint>> tmplist = (List<List<FPoint>>)ois.readObject();
					Object[][] objData = (Object[][])ois.readObject();
					if ((imp=WindowManager.getImage(impName)) == null) {
						IJ.error("Please open (or rename?) the "+impName);
						ois.close();
						return;
					}
					else {
						pData = new ShowPdata(tmplist,objData,imp,ptap);
					}
					pData.setVisible(true);
					ois.close();
				} catch (Exception e1) {
					IJ.log(e1.toString());
				}
			}
		});
		jDebugCheckbox.setText("Debug");
		jOthersPanel.add(jDebugCheckbox);
		jPanel1.add(jOthersPanel);

		add(jPanel1);

		jParamPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));
		jParamPanel.setLayout(new GridLayout(2, 1));
		jParamPanelBasic.setBorder(javax.swing.BorderFactory.createTitledBorder("Basic"));
		jParamPanelBasic.setLayout(new GridLayout(4,2));
		jParamPanelAdvanced.setBorder(javax.swing.BorderFactory.createTitledBorder("Advanced"));
		jParamPanelAdvanced.setLayout(new GridLayout(4,2));
		
		// varialbe x and y RoiSize
		jRoiSizexLabel.setText("RoiSize x");
		jParamPanelRoiSizex.add(jRoiSizexLabel);
		jParamPanelRoiSizex.add(jRoiSizexSpinner);
		jRoiSizeyLabel.setText("RoiSize y");
		jParamPanelRoiSizey.add(jRoiSizeyLabel);
		jParamPanelRoiSizey.add(jRoiSizeySpinner);
		jRoiSizexSpinner.addChangeListener(pcl);
		jRoiSizeySpinner.addChangeListener(pcl);
		jParamPanelBasic.add(jParamPanelRoiSizex);
		jParamPanelBasic.add(jParamPanelRoiSizey);
		//
		
		jLabel2.setText("Min Intensity");
		jParamPanelBasic.add(jLabel2);
		jParamPanelBasic.add(jMinIntSpinner);
		jMinIntSpinner.addChangeListener(pcl);

		jLabel3.setText("Min Size (pixel)");
		jParamPanelBasic.add(jLabel3);
		jParamPanelBasic.add(jMinSizeSpinner);
		jMinSizeSpinner.addChangeListener(pcl);

		jLabel5.setText("Nearest Particle range");
		jParamPanelBasic.add(jLabel5);
		jParamPanelBasic.add(jNearestRangeSpinner);
		jNearestRangeSpinner.addChangeListener(pcl);

		jLabel6.setText("Maxinum miss frame");
		jParamPanelAdvanced.add(jLabel6);
		jParamPanelAdvanced.add(jMaxLinkFraSpinner);
		jMaxLinkFraSpinner.addChangeListener(pcl);

		jLabel7.setText("Kurtosis");
		jParamPanelAdvanced.add(jLabel7);
		jParamPanelAdvanced.add(jKurtosisSpinner);
		jKurtosisSpinner.addChangeListener(pcl);

		jLabel9.setText("Iteration");
		jParamPanelAdvanced.add(jLabel9);
		jParamPanelAdvanced.add(jIteSpinner);

		jLabel10.setText("Search step (pixel)");
		jParamPanelAdvanced.add(jLabel10);
		jParamPanelAdvanced.add(jSearchIncSpinner);
		jSearchIncSpinner.addChangeListener(pcl);

		add(jParamPanel);
		jParamPanel.add(jParamPanelBasic);
		jParamPanel.add(jParamPanelAdvanced);

	}// </editor-fold>

	@Override
	public void windowClosed(WindowEvent e)
	{
		if(listener!=null)
			ImagePlus.removeImageListener(listener);
		if(pData!=null)
			pData.dispose();
		if(pInfo!=null)
			pInfo.dispose();
		frame=null;
		pData=null;
		pInfo=null;
		imp.setOverlay(null);
		imp=null;
	}

	protected void spinParamSet() {
		jRoiSizexSpinner.setValue(ptap.getRoiSizex());
		jRoiSizeySpinner.setValue(ptap.getRoiSizey());

		jMinIntSpinner.setValue(ptap.getMinIntensity());
		jMinSizeSpinner.setValue(ptap.getMinSize());
		jNearestRangeSpinner.setValue(ptap.getNearstRange());
		jMaxLinkFraSpinner.setValue(ptap.getLinkageFrame());
		jKurtosisSpinner.setValue(ptap.getKurtosis());
		jIteSpinner.setValue(ptap.getIterationNumber());
		jSearchIncSpinner.setValue(ptap.getSearchPointIncrement());		
	}

	public void scanOneFrame(ImagePlus scanImp) {
		if(scanImp == null) return;
		scanAreaRoi = new Roi(0,0,scanImp.getWidth(),scanImp.getHeight());
		Roi tmpAreaRoi = scanImp.getRoi();
		if(tmpAreaRoi != null && tmpAreaRoi.getType() == Roi.RECTANGLE)
			scanAreaRoi =scanImp.getRoi();
		try {
			if(dp != null && dp.isAlive() && !isDetectionPerformed) {
				dp.interrupt();
				scanAreaRoi = dp.getScanRoi();
				try {
					dp.join(); // to finish the Thread safely
				} catch (InterruptedException e) {
					IJ.log(e.toString());
				}
				dp = null;
			}
			dp = new DetectParticle(ptap,scanImp,scanAreaRoi,new ArrayList<FPoint>(),false,null);
			dp.setStackRange(scanImp.getCurrentSlice(), scanImp.getCurrentSlice());
			dp.start();
		} catch (Exception e) {
			IJ.log(e.toString());
		}
	}

	class RadioAction extends AbstractAction{

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if(imp == null) return;
			imp.updateAndDraw();
		}
	}
	class roiIntRadioAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		
		public void actionPerformed(ActionEvent arg0) {
			if(pData == null) return;
			pData.updatejt();
			pData.updateTableFI();
		}
		
	}
	class checkBoxAction implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			if(imp == null) return;
			imp.updateAndDraw();
		}
	}

	class icMouseAdapter extends MouseAdapter {
		@Override
		public void mouseReleased(MouseEvent e) {
			ImageProcessor ip = imp.getProcessor();
			if(ip.getMinThreshold() != -808080.0D && jAutomaticTrackCheckBox.isSelected()) {
				Roi r = imp.getRoi();
				if(scanAreaRoi.equals(r)) return;
				if(r.getType() == Roi.RECTANGLE) {
					// scan one frame when current rectangle roi was changed
					scanAreaRoi = r;
					scanOneFrame(imp);
				}
			}
		}	
	}

	class icKeyAdapter extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			ImageProcessor ip = imp.getProcessor();
			if(ip.getMinThreshold() != -808080.0D && jAutomaticTrackCheckBox.isSelected()) {
				int key = e.getKeyCode();
				int mod = e.getModifiersEx();		
				if ((mod & (InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK | 
						InputEvent.CTRL_DOWN_MASK)) != 0) {
					if (key == KeyEvent.VK_A) {
						// scan one frame when alt+a (or meta+a, ctrl+a) was pressed
						scanAreaRoi = new Roi(0,0,imp.getWidth(),imp.getHeight());
						imp.setRoi(scanAreaRoi);
						scanOneFrame(imp);
					}

				}	
			}
		}		
	}

	class ParamChangeListener implements ChangeListener {

		
		public void stateChanged(ChangeEvent e) {
			ptap.setIterationNumber((Integer)jIteSpinner.getValue());
			ptap.setKurtosis((Double)jKurtosisSpinner.getValue());
			ptap.setLinkageFrame((Integer)jMaxLinkFraSpinner.getValue());
			ptap.setNearstRange((Double)jNearestRangeSpinner.getValue());
			ptap.setMinIntensity((Double)jMinIntSpinner.getValue());
			ptap.setMinSize((Integer)jMinSizeSpinner.getValue());
			// varible x and y roisize
			ptap.setRoiSizex((Integer)jRoiSizexSpinner.getValue());
			ptap.setRoiSizey((Integer)jRoiSizeySpinner.getValue());			
			//
			ptap.setSearchPointIncrement((Integer)jSearchIncSpinner.getValue());
			ptap.setDo2dGaussfit(j2DGaussCheckbox.isSelected());
			isParamChanged = true;
			imp.updateAndDraw();
		}
	}
	
	class bgChangeListener implements ActionListener {

		
		public void actionPerformed(ActionEvent e) {
			if(jBgCheckBox.isSelected())
				jSubBgCheckBox.setEnabled(true);
			else
				jSubBgCheckBox.setEnabled(false);
			if (pData !=null) {
				pData.updatejt();
			}
		}
	}
	
	class bgLenChangeListener implements ChangeListener {

		public void stateChanged(ChangeEvent e) {
			if (pData !=null) {
				pData.updatejt();
			}			// TODO Auto-generated method stub			
		}
		
	}
	
	public static void setAnalyzedImp(ImagePlus imp)
	{
		analyzedImp = imp;
	}

	public static void setSelectedList(List<List<FPoint>> plist, int[] slist) {
		pointlist = plist;
		selectedList = slist;
	}

	public static boolean isDebug() {
		return jDebugCheckbox.isSelected();
	}

	public static void setPdata(ImagePlus img,ShowPdata data) {
		imp=img;
		ic=imp.getCanvas();
		pData = data;
	}

	public static void setDetectionState(boolean flag) {
		isDetectionPerformed = flag;
	}

	public static boolean getDetectMethod() {
		return jDetectMethodCombobox.getSelectedItem()=="Centroid"?true:false;
	}

	public static boolean isBg() {
		return jBgCheckBox.isSelected();
	}
	
	public static boolean isBgSub() {
		return jSubBgCheckBox.isSelected();
	}
	
	public static int getSubBgLen() {
		return (Integer)jBgFrameSpinner.getValue();
	}
	
	public static boolean isRoiInt() {
		return jRadioROIButton.isSelected();
	}
}
