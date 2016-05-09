package pta;
import ij.plugin.PlugIn;
import ij.gui.*;


public class AboutPTA implements PlugIn {

	public void run(String arg0) {
		String title = "About...";
		String message = "Particle Track and Analysis (PTA) Version 1.2\n" +
				"developed by Yoshiyuki Arai, " +
				"ISIR, Osaka University\n" +
				"I kindly request that you include a acknowledgement whenever presenting or publishing results based on PTA.\n" +
				""+
				"PTA Particle Track and Analysis Copyright (2010-2016) Yoshiyuki Arai. All rights reserved.\n"+
				"This product includes software developed by the University of Chicago,\n" +
				"as Operator of Argonne National Laboratory.\n"+
				"Minpack Copyright Notice (1999) University of Chicago.  All rights reserved\n"+
				"This software incorporates JFreeChart, (C)opyright 2000-2012\n" +
				"by Object Renery Limited and Contributors.";
		new MessageDialog(null,title,message);
	}

}
