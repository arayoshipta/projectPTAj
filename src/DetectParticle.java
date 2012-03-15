import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import ij.*;
import ij.gui.*;
import ij.measure.*;
import ij.process.*;

public class DetectParticle extends Thread implements Measurements{
	private PtaParam ptap;
	private ImagePlus imp;
	private ImageStack ims;
	private Calibration cal;
	private Roi scanRoi;
	private double lt,ht;
	private double wx,wy;
	private int startSlice,endSlice;
	private List<List<FPoint>> alldplist = new ArrayList<List<FPoint>>();
	private List<FPoint> dplist;
	private showPdata pData;
	private boolean multiFrames = false;
	private boolean isDetection = false;

	/*
	 * Constructor for multiple frames. this can return alldplist
	 */
	public DetectParticle(PtaParam ptap,ImagePlus imp,Roi scanRoi,
			showPdata pData) 
	{
		this.imp = imp;
		this.dplist = null;
		this.pData = pData;
		this.multiFrames = true;
		this.ims = imp.getImageStack();
		this.cal = imp.getCalibration();
		setPtap(ptap);
		setScanRoi(scanRoi);
		init();
	}
	/*
	 * Constructor for single frames. This can return dplist
	 */
	public DetectParticle(PtaParam ptap,ImagePlus imp,Roi scanRoi,
			List<FPoint> dplist,boolean isDetection,showPdata pData) {
		this.imp = imp;
		this.ims = imp.getImageStack();
		this.cal = imp.getCalibration();
		this.pData = pData;
		this.dplist = dplist;
		this.isDetection = isDetection;
		this.startSlice = imp.getCurrentSlice();
		this.multiFrames = false;

		setPtap(ptap);
		setScanRoi(scanRoi);
		init();
	}

	private void init() {
		ImageProcessor ip = imp.getProcessor();
		lt=Math.round(ip.getMinThreshold());
		if(imp.getBitDepth()!=32)
			ht=Math.round(ip.getMaxThreshold());
		else
			ht=65535;
		if(PTA_.isDebug())
			IJ.log(cal.toString());
	}

	public void setPtap(PtaParam ptap) {
		this.ptap=ptap;
	}

	public void setScanRoi(Roi scanRoi) {
		if(scanRoi == null) scanRoi = new Roi(0,0,imp.getWidth(),imp.getHeight());
		this.scanRoi = scanRoi;
		wx=scanRoi.getBounds().x+scanRoi.getBounds().width-ptap.getRoiSize();
		wy=scanRoi.getBounds().y+scanRoi.getBounds().height-ptap.getRoiSize();
	}

	public void setStackRange(int startSlice,int endSlice) {
		this.startSlice = startSlice>0?startSlice:1; 
		this.endSlice = endSlice>imp.getStackSize()?imp.getStackSize():endSlice;
	}

	public synchronized List<FPoint> dpOneSlice(int slice) {
		Overlay ol = new Overlay();

		byte[] mask = new byte[imp.getWidth()*imp.getHeight()];
		List<FPoint> dplist = new ArrayList<FPoint>(100);
		imp.setOverlay(null);

		imp.setSlice(slice);
		ImageProcessor ip = ims.getProcessor(slice);
		FloatProcessor fip = (FloatProcessor) ip.convertToFloat();

		scan:
			for(int i=scanRoi.getBounds().x;i<wx;i+=ptap.getSearchPointIncrement()) {
				for(int j=scanRoi.getBounds().y;j<wy;j+=ptap.getSearchPointIncrement()) {

					if(this.isInterrupted()) 
						break scan;
					double fval=Float.intBitsToFloat(fip.getPixel(i+ptap.getRoiSize()/2, j+ptap.getRoiSize()/2));
					if (fval>=lt && fval<=ht && mask[i+ptap.getRoiSize()/2+(j+ptap.getRoiSize()/2)*imp.getWidth()]==0) {
						Wand wand = new Wand(fip);
						double wdx = (int)i+(double)ptap.getRoiSize()/2.0D;
						double wdy = (int)j+(double)ptap.getRoiSize()/2.0D;
						wand.autoOutline((int)wdx, (int)wdy, lt, ht);
						Roi wandRoi = new PolygonRoi(wand.xpoints,wand.ypoints,wand.npoints,Roi.POLYGON);

						imp.setRoi(wandRoi);

						double[] param = new double[6];
						ImageStatistics is=imp.getStatistics(AREA+CENTROID);
						if(is.area<ptap.getMinSize()*cal.pixelHeight*cal.pixelWidth) {
							if(PTA_.isDebug())
								IJ.log("particle size:"+is.area+" is less than "+ptap.getMinSize());
							continue;
						}

						double xx = is.xCentroid-cal.pixelWidth*(double)ptap.getRoiSize()/2.0D;
						double yy = is.yCentroid-cal.pixelHeight*(double)ptap.getRoiSize()/2.0D;
						int ixx = (int)(xx/cal.pixelWidth);
						int iyy = (int)(yy/cal.pixelHeight);

						ixx=(int)(ixx<0?0:ixx);ixx=(int) (ixx>wx?wx:ixx);
						iyy=(int)(iyy<0?0:iyy);iyy=(int) (iyy>wy?wy:iyy);
						param[3] = is.xCentroid;
						param[4] = is.yCentroid;						

						double[] fionaData = new double[ptap.getRoiSize()*ptap.getRoiSize()];
						int[] info = new int[2];
						float[] pixVal = (float[])fip.getPixels();
						double roiInt = 0;
						double intInt = 0;

						// extract Roi data and cast to double
						for(int ii=0;ii<ptap.getRoiSize()*ptap.getRoiSize();ii++) {
							int ix=ii%ptap.getRoiSize(),iy=ii/ptap.getRoiSize();
							double tmpval = (double)pixVal[ixx+ix+(iyy+iy)*imp.getWidth()];
							fionaData[ix+iy*ptap.getRoiSize()] = tmpval;
							roiInt += tmpval;
							if(wandRoi.contains((ixx+ix),(iyy+iy))) {
								mask[ixx+ix+(iyy+iy)*imp.getWidth()]=(byte)255;
								intInt += tmpval;
							}
						}
						param[0] = roiInt;
						
						Roi tmpRoi =new Roi((int)(xx/cal.pixelWidth),(int)(yy/cal.pixelHeight),ptap.getRoiSize(),ptap.getRoiSize());
						imp.setRoi(tmpRoi);
						if(PTA_.isDebug()) IJ.log(tmpRoi.toString());
						is=imp.getStatistics(CENTER_OF_MASS+MIN_MAX+AREA);
						// estimate parameter
//						param[0] = is.max; // amplitude
						param[5] = is.min; // offset
						param[1] = (double)ptap.getRoiSize()/10.0d*cal.pixelWidth; // sigma x
						param[2] = (double)ptap.getRoiSize()/10.0d*cal.pixelHeight; // sigma y
						// Disable center of Mass method H22.12.04
//						if(!PTA_.getDetectMethod()) {
//							param[3] = is.xCenterOfMass; // mu x
//							param[4] = is.yCenterOfMass; // mu y
//						}
						if(PTA_.isDebug())
							IJ.log("param:"+Arrays.toString(param));
						// fit 2DGauss
						info[1] = ptap.getIterationNumber(); //iteration limit number
						boolean fit = false;
						double dsize = ptap.getRoiSize()%2==0?ptap.getRoiSize()/2:(ptap.getRoiSize()+1)/2;
						if((param[3]-xx)>0 
								&& (param[3]-xx)<ptap.getRoiSize()
								&& (param[4]-yy)>0
								&& (param[4]-yy)<ptap.getRoiSize()
								&& is.kurtosis>ptap.getKurtosis()){
							double preParam = param[0];
							if (ptap.isDo2dGaussfit()) {
								param[1] /= cal.pixelWidth;
								param[2] /= cal.pixelHeight;
								param[3] = param[4] = (double)ptap.getRoiSize()/2.0D;
								param = PTA_.fit2DGauss(fionaData,param,ptap.getRoiSize(),info); //DO 2d gaussian Fitting!!
								param[3] *= cal.pixelWidth; // to translate pixel data to unit data
								param[4] *= cal.pixelHeight;
								param[3] += (double)ixx*cal.pixelWidth;
								param[4] += (double)iyy*cal.pixelHeight;
								param[1] *= cal.pixelWidth;
								param[2] *= cal.pixelHeight;
							} else {
								param[0] = intInt;
								info[0] = 1;
							}
							fit = true;
							if(PTA_.isDebug()) {
								IJ.log("param:"+Arrays.toString(param));
								Roi debroi = new Roi(ixx,iyy,(int)dsize*2,(int)dsize*2);
								debroi.setStrokeColor(Color.magenta);
								ol.add(debroi);
								imp.setOverlay(ol);
							}
						}
						// Data selection
						if(info[0]==1
								&& fit
								&& param[0]>ptap.getMinIntensity()
								&& param[1]>0 && param[2]>0		
								&& param[1]<=(double)ptap.getRoiSize()*cal.pixelWidth && param[2]<=(double)ptap.getRoiSize()*cal.pixelHeight
								&& (param[3]-xx)<(double)ptap.getRoiSize()*cal.pixelWidth && (param[3]-xx)>0
								&& (param[4]-yy)<(double)ptap.getRoiSize()*cal.pixelHeight && (param[4]-yy)>0
						) 
						{
							FPoint detP = new FPoint(ixx,iyy,param,roiInt,info,ptap.getRoiSize(),Color.cyan,slice,cal);
							dplist.add(detP);
							ol.add(detP.retRoi());
							imp.setOverlay(ol);
						} else {
							StringBuilder msg = new StringBuilder("Out");
							if(info[0]==0)
								msg.append(":info="+info[0]);
							if(!fit)
								msg.append(":fit=false");
							if(param[0]<ptap.getMinIntensity())
								msg.append(":param[0]="+param[0]);
							if(param[1]<0 || param[1]>=ptap.getRoiSize()/2)
								msg.append(":param[1]="+param[1]);
							if(param[2]<0 || param[2]>=ptap.getRoiSize()/2)
								msg.append(":param[2]="+param[2]);
							if((param[3]-ixx)<=0 || (param[3]-ixx)>ptap.getRoiSize())
								msg.append(":param[3]="+param[3]);
							if((param[4]-iyy)<=0 || (param[4]-iyy)>ptap.getRoiSize())
								msg.append(":param[4]="+param[4]);
							if(PTA_.isDebug())
								IJ.log(msg.toString());
						}
					}

				}
			}
		imp.setRoi(scanRoi);
		imp.setOverlay(null);
		ol.clear();
		// only show the dplist removed duplication
		for(Roi r:FPoint.retRoiset(FPoint.findDuplication(dplist, ptap.getNearstRange()*ptap.getRoiSize()*cal.pixelDepth), Color.cyan))
			ol.add(r);
		imp.setOverlay(ol);
		return dplist;
	}

	public void run() {
		int slice = startSlice;
		IJ.resetEscape();
		if(multiFrames) {
			do{
				if(IJ.escapePressed()) {
					IJ.resetEscape();
					int ret = JOptionPane.showConfirmDialog(WindowManager.getFrontWindow(),
							"Abort detection?","Allert",JOptionPane.YES_NO_OPTION);
					if(ret == 0)
						break;
				}
				alldplist.add(slice-1, new ArrayList<FPoint>(dpOneSlice(slice)));
				slice++;
			} while (slice<=endSlice);
			PTA_.setDetectionState(false);
			processPData();
		} else {
			dplist = dpOneSlice(startSlice);
			if(isDetection) {
				alldplist.add(0, new ArrayList<FPoint>(dplist));
				PTA_.setDetectionState(false);
				processPData();
			}
		}
	}

	public void processPData() {
		Overlay pathOverlay = new Overlay();
		new findLinkage(alldplist,ptap,imp).run();
		pData = new showPdata(new makePointList(alldplist,pathOverlay,2).run()
				,imp,ptap);
		imp.setOverlay(pathOverlay);
	}

	public Roi getScanRoi() {
		return scanRoi;
	}
}
