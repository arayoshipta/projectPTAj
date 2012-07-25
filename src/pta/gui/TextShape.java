package pta.gui;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.AffineTransform;

import ij.gui.*;

public class TextShape {
		
	private TextShape(){};
	
	public static ShapeRoi makeDigitShapeRoi(int x,int y,String text,ImageCanvas ic) {
	       Graphics2D g2;
	        g2 = (Graphics2D) ic.getGraphics();

	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                            RenderingHints.VALUE_ANTIALIAS_ON);

	        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
	                            RenderingHints.VALUE_RENDER_QUALITY);

	        FontRenderContext frc = g2.getFontRenderContext();
	        Font f = new Font("SansSerif",Font.PLAIN,12);
	        TextLayout tl = new TextLayout(text, f, frc);
	        AffineTransform transform = new AffineTransform();
	        transform.setToTranslation(x,y);
	        Shape shape = tl.getOutline(transform);
	        		
		return new ShapeRoi(shape);
		
	}

}
