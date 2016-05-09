package pta.capture;
import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.ScreenGrabber;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class CaptureImageStack implements PlugInFilter {
	
	private ImagePlus imp;

	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		// show Dialog
		GenericDialog gd = new GenericDialog("Make caputured stack");
		gd.addNumericField("start Frame", 1, 0);
		gd.addNumericField("end Frame", imp.getStackSize(), 0);
		gd.showDialog();
		if(gd.wasCanceled()) return;
		ImagePlus newimp = new ImagePlus("Grab of "+imp.getShortTitle(),
				captImageStack(imp,(int)gd.getNextNumber(),(int)gd.getNextNumber()));
		newimp.show();
	}

	public int setup(String arg, ImagePlus imp) {
		// TODO Auto-generated method stub
		this.imp = imp;
		return DOES_ALL;
	}
	
	public static ImageStack captImageStack(ImagePlus imp,int startStack, int endStack) {
		
		//Get reference to the imp
		WindowManager.setTempCurrentImage(imp);
		//Check the range of startStack and endStack
		startStack = startStack<1?1:startStack;
		startStack = startStack>imp.getStackSize()?imp.getStackSize():startStack;
		endStack = endStack<2?1:endStack;
		endStack = endStack>imp.getStackSize()?imp.getStackSize():endStack;
		
		int frameLength = endStack-startStack;
		ScreenGrabber sg = new ScreenGrabber();
		ImagePlus newimp = sg.captureImage();
		ImageStack is = newimp.createEmptyStack();
		//take one frame, capture one frame, and make imageStack
		for (int frame = 0;frame<=frameLength;frame++) {
			imp.setSlice(frame+startStack);
			ImagePlus tmpimp = sg.captureImage();
			is.addSlice(String.valueOf(frame), tmpimp.getProcessor());
			IJ.showProgress((double)frame/frameLength);
		}
		IJ.showProgress(1.0D);
		return is;		
	}

}
