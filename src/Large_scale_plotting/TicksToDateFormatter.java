/**
 * 
 */
package Large_scale_plotting;

import java.text.DateFormat;
import java.util.GregorianCalendar;
import repast.simphony.ui.plugin.TickCountFormatter;

/**
 * @author jsnape
 *
 */
public class TicksToDateFormatter implements TickCountFormatter {

	private GregorianCalendar time;
	private DateFormat tickFormatter;
	
	/* (non-Javadoc)
	 * @see repast.simphony.ui.plugin.TickCountFormatter#format(double)
	 */
	@Override
	public String format(double tick) {
		StringBuilder returnBuilder = new StringBuilder();
		returnBuilder.append(tickFormatter.format(time.getTime()));
		returnBuilder.append(" (Raw Tick: ");
		returnBuilder.append(tick);
		returnBuilder.append(")");
		return returnBuilder.toString();
	}

	/* (non-Javadoc)
	 * @see repast.simphony.ui.plugin.TickCountFormatter#getInitialValue()
	 */
	@Override
	public String getInitialValue() {
		// TODO Auto-generated method stub
		return format(0);
	}
	
	public TicksToDateFormatter(GregorianCalendar time, DateFormat thisFormat)
	{
		this.time = time;
		this.tickFormatter = thisFormat;
	}

}
