package pta;
import ij.plugin.PlugIn;
import ij.gui.*;


public class AboutPTA implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		String title = "About...";
		String message = "Particle Track and Analysis (PTA) Version 1.1\n" +
				"developed by Yoshiyuki Arai, " +
				"Lab. for Nanosystems Physiology Research Institute for Electronic Science Hokkaido University\n" +
				"I kindly request that you include a acknowledement whenever presenting or publishing results based on PTA.\n" +
				""+
				"PTA Particle Track and Analysis Copyright (2010) Yoshiyuki Arai. All rights reserved.\n"+
				"This product includes software developed by the University of Chicago,\n" +
				"as Operator of Argonne National Laboratory.\n"+
				"Minpack Copyright Notice (1999) University of Chicago.  All rights reserved\n"+
				"This software incorporates JFreeChart, (C)opyright 2000-2012\n" +
				"by Object Renery Limited and Contributors.";
		new MessageDialog(null,title,message);
	}

}
