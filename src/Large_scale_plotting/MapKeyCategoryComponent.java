package Large_scale_plotting;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.vividsolutions.jts.geom.Envelope;

public class MapKeyCategoryComponent extends JPanel
{
	Icon col;
	String caption;
	
	public MapKeyCategoryComponent(int r, int g, int b, String cap)
	{
		this(new Color(r,g,b),cap);
	}

	public MapKeyCategoryComponent(int rgb, String cap)
	{
		this(new Color(rgb), cap);
	}
	
	public MapKeyCategoryComponent(Color c, String cap)
	{
		this(iconFromColour(c),cap);

	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public MapKeyCategoryComponent(Icon col, String cap)
	{
		this.col = col;
		this.caption = cap;
		initThisRow();
	}
	
	private void initThisRow()
	{
		this.setSize(300,30);
		JLabel colbox = new JLabel(this.col);
		colbox.setOpaque(true);
		colbox.setVisible(true);
		JLabel text = new JLabel(" "+this.caption);
		text.setVisible(true);
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setVisible(true);
		this.add(colbox);
		this.add(text);
		
		//Test only
//		JFrame foo = new JFrame();
//		foo.getContentPane().add(this);
//		foo.setVisible(true);
		
		//this.validate();
		
	}
	
	private static Icon iconFromColour(Color c)
	{
		BufferedImage image = new BufferedImage(25, 25,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setBackground(c);
		graphics.setColor(c);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

		// set the output area and graphics
//		renderer.setConcatTransforms(true);
//		renderer.setOutput(graphics, new java.awt.Rectangle(0, 0, image
//				.getWidth(), image.getHeight()));
//		renderer.render(fc, new Envelope(0, iconWidth, 0, iconWidth), s);
		return new ImageIcon(image);
	}
}
