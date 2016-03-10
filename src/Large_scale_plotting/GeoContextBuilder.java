package Large_scale_plotting;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.geotools.filter.FilterTransformer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.StyleAttributeExtractor;
import org.geotools.styling.Rule;
import org.opengis.style.GraphicalSymbol;

import Large_scale_plotting.LegendEntryFilterVisitor.LegendEntryCreationException;
import repast.simphony.context.Context;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.GUIRegistry;
import repast.simphony.engine.environment.GUIRegistryType;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.essentials.RepastEssentials;
//import repast.simphony.gis.display.LegendIconMaker;
import repast.simphony.gis.display.PiccoloMapPanel;
import repast.simphony.space.gis.DefaultGeography;
import repast.simphony.space.gis.ShapefileLoader;
import repast.simphony.space.gis.ShapefileWriter;
import repast.simphony.ui.RSApplication;
import repast.simphony.util.collections.Pair;
import repast.simphony.visualization.gis.DisplayGIS;

public class GeoContextBuilder implements ContextBuilder<GeoAreaAgent>
{

	boolean writeInitialShapefile = false;
	GregorianCalendar simTime = new GregorianCalendar();
	String timestamp = "";
	String output_dir = "";
	String output_filename = "\\cap_times_100_over_HHDens.ogg";

	@SuppressWarnings("unchecked")
	@Override
	public Context build(Context context)
	{
		// DefaultGeography<GeoAreaAgent> ukGeography = new
		// DefaultGeography<GeoAreaAgent>("UK");
		DefaultGeography<GeoAreaAgent> ukGeography = new DefaultGeography<GeoAreaAgent>("UK");
		context.addProjection(ukGeography);
		
		ukGeography.setCRS("EPSG:27700"); // This is a code for the OSGB 1936 projection.  Don't know why!! 27700
		// File file = new
		// File(RepastEssentials.GetParameter("RootDir").toString() +
		// "/dataFiles/LLSOA_areas.shp");
		String filename;// = "C:\\data\\2001_LLSOA_Boundaries\\1_percent\\fivePercentS.shp";//New_generalised\\england_and_wales_low_soa_2001_selectNorth.shp";//"C:\\data\\PCode_dists_2012\\EngWalSco_pcd_2012Polygon.shp";
		filename = (String) RepastEssentials.GetParameter("shapefileName");
		String server = (String) RepastEssentials.GetParameter("server");
		String dbname = (String) RepastEssentials.GetParameter("dbname");
		String user = (String) RepastEssentials.GetParameter("user");
		String pass = (String) RepastEssentials.GetParameter("password");
		String outputBase = (String) RepastEssentials.GetParameter("outputDir");
		String outputFileName = (String) RepastEssentials.GetParameter("outputFile");
		if (outputFileName != null && outputFileName != "")
		{
			this.output_filename = outputFileName;
		}
		this.timestamp = new Long(System.currentTimeMillis()).toString();

		this.output_dir = outputBase + "\\" + this.timestamp;
		
	    new File(output_dir).mkdirs();
		
		
		File file = new File(filename);
		
		ShapefileLoader loader = null;
		try
		{
			loader = new ShapefileLoader(GeoAreaAgent.class, file.toURL(), ukGeography, context);
			loader.load();
			System.out.println("have shapefile initialised with " + file.toURL().toString());

		} catch (MalformedURLException e)
		{
			e.printStackTrace();
		}

		GeoDBWrapper thisDB = new GeoDBWrapper("jdbc:mysql://"+server+"/"+dbname,server,dbname,user,pass);
		
		Iterable areaAgents = context.getAgentLayer(GeoAreaAgent.class);
		
		GeoAreaAgent area = (GeoAreaAgent) areaAgents.iterator().next();
		
		if (area.getLSOA04CD() == null)
		{
			if (area.getPCODEDIST() == null)
			{
				System.err.println("Geo agent with neither Pcode or LLSOA ID");
			}
			thisDB.setID_field(GeoDBWrapper.Pcode_d_FIELD);
			
		} else
		{
			// If it has an LSOA04CD, it must be an LLSOA shape, even if it has
			// a postcode too
			thisDB.setID_field(GeoDBWrapper.LLSOA_ID_FIELD);

		}		
		
		simTime = new GregorianCalendar(2010, 3, 1);		
		thisDB.setMyCal(simTime);
		thisDB.initialise();
		
		//Would initialise the display runner here, but it the displays aren't yet registered!!
		//instead - schedule an action to do it early!
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters params = ScheduleParameters.createOneTime(0, ScheduleParameters.FIRST_PRIORITY);   // createRepeating(0, 1,ScheduleParameters.LAST_PRIORITY);
		schedule.schedule(params, this, "initMovieMaker", new Object[]{context, thisDB}); 

		params =  ScheduleParameters.createRepeating(0, 4,ScheduleParameters.LAST_PRIORITY);   // Add in shapefile writing
		schedule.schedule(params, this, "writeShapefile", new Object[]{ukGeography, thisDB}); 
		
		DateFormat thisFormat = new SimpleDateFormat("d MMM yyyy");
		
		RSApplication.getRSApplicationInstance().getGui().setTickCountFormatter(new TicksToDateFormatter(simTime,thisFormat));
		
		for (Object a : context.getAgentLayer(GeoAreaAgent.class))
		{
			GeoAreaAgent aa = (GeoAreaAgent) a;
			aa.initialise(thisDB, simTime);
		}
		context.add(thisDB);
		RepastEssentials.EndSimulationRunAt(290);
		return context;
	}
	
	public void writeShapefile(DefaultGeography<GeoAreaAgent> ukGeography, GeoDBWrapper database)
	{
		ShapefileWriter testWriter = new ShapefileWriter(ukGeography);
		
		String suffix;
		
		// retrieve image
			if (database != null)
			{
				SimpleDateFormat nameFormatter = new SimpleDateFormat("dd-MMM-yyyy");
				suffix = nameFormatter.format(database.getMyCal().getTime());
			}
			else
			{
				suffix = ""+RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			}
		
		  File outFile = new File(this.output_dir + "\\" + "EWS_PCD_for_tick"+suffix+".shp");
		  try { 
			  testWriter.write(ukGeography.getLayer(GeoAreaAgent.class)
		  .getName(), outFile.toURL()); } 
		  catch (MalformedURLException e) { //
		 // TODO Auto-generated catch block e.printStackTrace(); 
			  } 
	}
	
	public void initMovieMaker(Context context, GeoDBWrapper db)
	{
		RunState runState = RSApplication.getRSApplicationInstance().getController().getCurrentRunState();
		GUIRegistry guiRegis = runState.getGUIRegistry();
		Collection typeAndComp = guiRegis.getTypesAndComponents();
		//Iterator <Pair<GUIRegistryType,Collection<JComponent>>> typeAndCompIter = typeAndComp.iterator();
		Iterator<Pair> typeAndCompIter = typeAndComp.iterator();

		System.out.println("Got type and component iterator of length " + typeAndComp.size());
		
		
		while ( typeAndCompIter.hasNext() ){
			Pair <GUIRegistryType,Collection<JComponent>> typeAndCompPair = typeAndCompIter.next();
			GUIRegistryType guiRegisType = typeAndCompPair.getFirst();
			System.out.println("Got component of type " + guiRegisType.toString());
			if (guiRegisType == GUIRegistryType.DISPLAY) {
				Iterator<JComponent> compIter = typeAndCompPair.getSecond().iterator();
				while (compIter.hasNext())
				{
					PiccoloMapPanel chartComp = (PiccoloMapPanel) compIter.next();
					chartComp.getCanvas().setBackground(Color.LIGHT_GRAY);
					chartComp.setOpaque(true);
					// Create an action with this display to get buffered images and send them to a movie file
					//       BufferedImage img = imageable.getImage();
					
					//MovieMaker gisMovieMaker = new MovieMaker(this.output_dir + "\\cap_times_100_over_HHDens.ogg", 5, chartComp);
					MovieMaker gisMovieMaker = new MovieMaker(this.output_dir + "\\" + this.output_filename, 5, chartComp);
					context.add(gisMovieMaker); //Basically to ensure this is torn down with the context
					gisMovieMaker.setDB(db);
					ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
					ScheduleParameters stop = ScheduleParameters.createAtEnd(ScheduleParameters.LAST_PRIORITY);
					schedule.schedule(stop, gisMovieMaker, "endVideo");
					
//					gisMovieMaker.addDynamicComponent(RSApplication.getRSApplicationInstance().getGui().getDockable(RSGUIConstants.TICK_COUNT_LABEL));
//					
					DisplayGIS thisDisp = (DisplayGIS) guiRegis.getDisplayForComponent(chartComp);

					JFrame legend = new JFrame();
					MapKeyComponent contents = new MapKeyComponent();
					contents.setAlignmentVertical();
					StyleAttributeExtractor vis = new StyleAttributeExtractor();
					FilterTransformer tran = new FilterTransformer();
					LegendEntryFilterVisitor mapper = new  LegendEntryFilterVisitor();
					int cats = 0;
					for (FeatureTypeStyle f : thisDisp.getStyleFor(GeoAreaAgent.class.getName()).getFeatureTypeStyles())
					{
						for (Rule r : f.getRules())
						{
							
							ImageIcon ic = null;
							BufferedImage image = new BufferedImage(20, 20,
							BufferedImage.TYPE_INT_ARGB);
							Graphics2D graphics = image.createGraphics();
							graphics.setBackground(new Color(0, 0, 0, 0));
							graphics.setColor(new Color(0, 0, 0, 0));
							graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

							// set the output area and graphics
	//						renderer.setConcatTransforms(true);
		//					renderer.setOutput(graphics, new java.awt.Rectangle(0, 0, image
			//						.getWidth(), image.getHeight()));
				//			renderer.render(fc, new Envelope(0, iconWidth, 0, iconWidth), s);
					//		icon = new ImageIcon(image);

							//now restore any changes made to the symbolizers
//							for (ChangeTracker ct : changes) {
	//							ct.restoreValue();
		//					}
			//				return icon;

				//		Icon ic = LegendIconMaker.makeLegendIcon(20, r, null); /*Original version - now defunct */
						
							r.accept(vis);
							//r.getFilter().accept(mapper, null);
														
							//Horrible, horrible line of code, but it works because of a priori knowledge that there will be only
							//one symbolizer and it will have a fill color
							Color c = (Color)(((PolygonSymbolizer) r.symbolizers().get(0)).getFill().getColor().evaluate(null,Color.class));
							String mapped="";
							try
							{
								mapped = r.getTitle();
								
							} catch (Exception e1)  //Very naughty - used to only catch LegendEntryCreationException
							{
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							MapKeyCategoryComponent legendEntry = new MapKeyCategoryComponent(c, mapped);
							System.out.println("DEBUG : Category component has " + legendEntry.getComponentCount() + " components");
							contents.addCategory(legendEntry, cats);
							//contents.add(new JLabel(ic));
							//contents.add(new JLabel(mapped));
						
						                  
						}
						cats++;
						chartComp.getCanvas().zoomToAreaOfInterest();

//						legend.setSize(200,100);
//						legend.getContentPane().add(contents);
//						legend.setTitle("Chloropleth legend");
//						legend.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
//						legend.setVisible(true);
						chartComp.getCanvas().add(contents);
						chartComp.revalidate();
						
					}
					

//					
//					
//					MapKeyComponent key = new MapKeyComponent();
//					for (int i = 0; i < chartComp.getCanvas().get)
//					key.addCategory(new MapKeyCategoryComponent(c, cap), );
//					
//					
//					chartComp.getCanvas().getMapLayer().
//					
//					gisMovieMaker.addStaticComponent();
					//ScheduleParameters params = ScheduleParameters.createOneTime(1);
					//if (Consts.DEBUG) System.out.println("chartCompCol: null?: "+getChartCompCollection());
					//if ((chartSnapshotOn) && (getChartCompCollection() != null)){
					ScheduleParameters params = ScheduleParameters.createRepeating(0, 1,ScheduleParameters.LAST_PRIORITY);
					schedule.schedule(params, gisMovieMaker, "addImageToVideo"); 
					
				}
			}
		}
	}
}
