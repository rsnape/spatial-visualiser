package Large_scale_plotting;

import java.awt.Color;
import java.awt.LayoutManager;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.Component;

public class MapKeyComponent extends JPanel
{
	ArrayList<MapKeyCategoryComponent> categories = new ArrayList<MapKeyCategoryComponent>();

	public void setAlignmentHorizontal()
	{
		this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
	}
	
	public void setAlignmentVertical()
	{
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	}
	
	public void addCategory(MapKeyCategoryComponent c, int index)
	{
		categories.add(index, c);
		this.removeAll();
		for (MapKeyCategoryComponent cat : categories)
		{
			int newWidth = this.getWidth();
			if (cat.getWidth() > newWidth)
			{
				newWidth = cat.getWidth() + 15;
			}
			this.setSize(newWidth,this.getHeight()+cat.getHeight());			
			this.add(cat);
		}
		
		this.validate();
	}
	
	public MapKeyComponent()
	{
		super();
		TitledBorder title;
		title = BorderFactory.createTitledBorder("Key");
		this.setBorder(title);
		//this.setBackground(Color.DARK_GRAY);
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
	}	
}
