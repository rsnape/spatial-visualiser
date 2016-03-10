package Large_scale_plotting;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import repast.simphony.context.Context;
import repast.simphony.ui.Imageable;
import repast.simphony.ui.ImageableJComponentAdapter;
import repast.simphony.ui.RSApplication;
import repast.simphony.util.ContextUtils;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStreamCoder;

public class MovieMaker
{
	IRational FRAME_RATE;
	final String outFile;
	JPanel sourcePanel;
	Imageable source;
	final double SECONDS_PER_TICK = 0.2;
	final IMediaWriter writer;
	int imageCount = 0;
	boolean writeFramesAsPNG = true;
	Context repastContext =  null;
	GeoDBWrapper database = null;
	SimpleDateFormat nameFormatter = new SimpleDateFormat("dd-MMM-yyyy");

	public void setDB(GeoDBWrapper db)
	{
		this.database = db;
	}

	/**
	 * @return the writeFramesAsPNG
	 */
	public boolean isWriteFramesAsPNG()
	{
		return this.writeFramesAsPNG;
	}

	/**
	 * @param writeFramesAsPNG
	 *            the writeFramesAsPNG to set
	 */
	public void setWriteFramesAsPNG(boolean writeFramesAsPNG)
	{
		this.writeFramesAsPNG = writeFramesAsPNG;
	}
	
	public void setRepastContext(Context c)
	{
		this.repastContext = c;
	}

	public MovieMaker(String filename, int frameRate, JPanel i)
	{
		outFile = filename;
		// This is the robot for taking a snapshot of the
		// screen. It's part of Java AWT
		FRAME_RATE = IRational.make(frameRate, 1);
		System.out.println("Making a movie maker with name " + filename);
		// First, let's make a IMediaWriter to write the file.
		writer = ToolFactory.makeWriter(outFile);
		sourcePanel = i;
		source = new ImageableJComponentAdapter(i);

		// We tell it we're going to add one video stream, with id 0,
		// at position 0, and that it will have a fixed frame rate of
		// FRAME_RATE.
		// writer.addVideoStream(0, 0, i.getWidth(), i.getHeight());
		// IStreamCoder vidEncoder = new
		int streamID = writer.addVideoStream(0, 0, FRAME_RATE,3*i.getWidth()/4, i.getHeight());
		//writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_, FRAME_RATE, 2*i.getWidth() / 3, i.getHeight());
		
		System.out.println("Adding video stream returned ID:" + streamID);
		IStreamCoder encoder = writer.getContainer().getStream(0).getStreamCoder();
		encoder.open();
		System.out.println("Endoder is open?" + encoder.isOpen());
		// Trying to get the quality up!
		//encoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, false);
		encoder.setBitRate(400000);
		//encoder.setFrameRate(FRAME_RATE);
		//encoder.setTimeBase(IRational.make(1,25));
		// encoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
		// encoder.setGlobalQuality(4);
		System.out.println("Encoding with bit rate " + encoder.getBitRate() + "; timebase " + encoder.getTimeBase() + "; frame rate " + encoder.getFrameRate());
	}

	public void addImageToVideo()
	{

		//System.out.println("Adding image number " + imageCount);
		// Now, we're going to loop
		double startTime = imageCount * SECONDS_PER_TICK * 1000;
		for (int index = 0; index < SECONDS_PER_TICK * FRAME_RATE.getDouble(); index++)
		{
			// take the screen shot
			BufferedImage screen = source.getImage();
			if (this.writeFramesAsPNG)
			{
				String suffix;
				
				try
				{
					// retrieve image
					if (this.database != null)
					{
						suffix = nameFormatter.format(this.database.getMyCal().getTime());
					}
					else
					{
						suffix = ""+imageCount;
					}
					File outputfile = new File(outFile + "_frame_"+ suffix + ".png");
					ImageIO.write(screen, "png", outputfile);
				} catch (IOException e)
				{
					System.err.println("Can't write image to file!!");
				}
			}

			// convert to the right image type
			screen = screen.getSubimage(0, 0, 3*screen.getWidth() / 4, screen.getHeight());
			BufferedImage bgrScreen = convertToType(screen, BufferedImage.TYPE_3BYTE_BGR);

			// compute the amount to inset the time stamp and
			// translate the image to that position
			// Graphics2D g = bgrScreen.createGraphics();
			// g.drawString(sourcePane, bgrScreen.getWidth() - 300,
			// bgrScreen.getHeight() - 60);

			// encode the image
			//System.out.println("Have a writer with stream" + writer.getContainer().getStream(0).toString());
			
			writer.encodeVideo(0, bgrScreen, (long) (startTime + index * 1000 / FRAME_RATE.getValue()), TimeUnit.MILLISECONDS);

			// sleep for framerate milliseconds
			/*
			 * try { Thread.sleep((long) (1000 / FRAME_RATE.getDouble())); }
			 * catch (InterruptedException e) {
			 * System.err.println("Arrrgh, you've interrupted my sleep!!");
			 * e.printStackTrace(); }
			 */
		}
		imageCount++;
	}
	
	public void endVideo()
	{
		writer.close();
	}

	/**
	 * Convert a {@link BufferedImage} of any type, to {@link BufferedImage} of
	 * a specified type. If the source image is the same type as the target
	 * type, then original image is returned, otherwise new image of the correct
	 * type is created and the content of the source image is copied into the
	 * new image.
	 * 
	 * @param sourceImage
	 *            the image to be converted
	 * @param targetType
	 *            the desired BufferedImage type
	 * 
	 * @return a BufferedImage of the specifed target type.
	 * 
	 * @see BufferedImage
	 */

	public static BufferedImage convertToType(BufferedImage sourceImage,
			int targetType)
	{
		BufferedImage image;

		// if the source image is already the target type, return the source
		// image

		if (sourceImage.getType() == targetType)
			image = sourceImage;

		// otherwise create a new image of the target type and draw the new
		// image

		else
		{
			image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), targetType);
			image.getGraphics().drawImage(sourceImage, 0, 0, null);
		}

		return image;
	}

}
