package in.adi.groovy.adidsl.impl.carnatictaal

import groovy.swing.SwingBuilder
import groovy.util.logging.Log4j2
import in.adi.groovy.adidsl.impl.midi.InstrumentName
import in.adi.groovy.adidsl.impl.midi.SoundDSL
import in.adi.groovy.adidsl.impl.midi.SoundDSL.SoundChannel
import in.adi.groovy.adidsl.impl.midi.carnatic.CarnaticNotes

import javax.sound.midi.Instrument
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.border.Border
import java.awt.Color

import in.adi.groovy.adidsl.fw.DSLInit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

@Log4j2
class CarnaticTaal_Init implements DSLInit
{
	@Override
	void compilerConfiguration(CompilerConfiguration compilerConfiguration, ImportCustomizer imports)
	{
		log.debug "Registering taal"
		
		imports.addImport('ctaal', TaalDSL.name)
	}

	@Override
	void init()
	{

	}
}


class TaalDSL
{
	static void spec(Closure closure)
	{
		Taal t = new Taal()
		
		closure.delegate = t
		
		closure()
		t.start()
	}
}

@Log4j2
class Taal
{
	private Color[] COLORS = [
			Color.GREEN, 
			Color.PINK, 
			Color.BLUE, 
			Color.LIGHT_GRAY, 
			Color.DARK_GRAY, 
			Color.CYAN, 
			Color.BLACK, 
			Color.MAGENTA,
			Color.GRAY
	]
	
	private double period = 1000
	private jaati = 4
	private gati = 4
	
	private int[] gatiPanels = [1, 2, 3, 4, 5, 6]

	private List<Box> boxes = []

	private Tick tick = new Tick()

	//private Timer timer = new Timer()
	private Timer swingTimer = new Timer(1000, tick);
	
	private JComponent base
	
	private SoundChannel channel = SoundDSL.musicch()
	
	private ThreadPoolExecutor pool = Executors.newFixedThreadPool(2)
	
	private long gatiPeriod
	
	private def minors = []
	
	void gati(int g)
	{
		this.gati = g
	}
	
	void jaati(int j)
	{
		this.jaati = j
	}
	
	void minor(int... ticks)
	{
		minors.addAll(ticks)
	}
	
	//thattu viral
	void lagu(int c)
	{
		boxes << new Box(true, boxes.size(), Color.orange, 5)

		for (int i = 1; i < c; i++)
			boxes << new Box(boxes.size(), Color.WHITE, 5)
	}
	
	//thatti tirupu
	void drutam()
	{
		boxes << new Box(true, boxes.size(), Color.RED, 5)
		boxes << new Box(boxes.size(), Color.PINK, 5)
	}
	
	void anudrutam()
	{
		boxes << new Box(true, boxes.size(), Color.GRAY, 5)
	}
	
	void period(long time)
	{
		this.period = time
	}
	
	void start()
	{
		gatiPeriod = period / gati

		channel.instrument(InstrumentName.panflute)

		buildUI()
		
		//timer.scheduleAtFixedRate(tick, 1000, (long) gatiPeriod)
		swingTimer.setCoalesce(false)
		swingTimer.setDelay(gatiPeriod.toInteger())
		swingTimer.setRepeats(true)
	
		swingTimer.start()
	}

	private void buildUI()
	{
		TickTimings timings = TickTimings.newInstance()

		timings.add(period, 2)
		timings.add(period, 3)
		timings.add(period, 4)
		timings.add(period, 5)

		new SwingBuilder().edt {

			JFrame frame = frame(title: 'Carnatic Taal', locationRelativeTo: null, show: true)
			//frame.setSize(Toolkit.defaultToolkit.screenSize)
			frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH)
			
			log.debug "Frame size = {}", frame.size
				
			base = new Base()
			
			frame.contentPane = base

			boxes.each {base.blockPanel.add it}
		}
	}
	
	class Tick extends TimerTask implements ActionListener
	{
		int gatiCounter = 0
		long previous
		int curIndex = 0
		
		private Box curBox
		private Box previousBox

		@Override
		void actionPerformed(ActionEvent e)
		{
			pool.execute(this)
		}

		@Override
		void run()
		{
			calc()
			
			long now = System.currentTimeMillis()
			
			log.trace "Interval between ticks = {}", (now - previous)

			previous = now
		}
		
		void calc()
		{
			gatiCounter++

			if (gatiCounter != gati)
			{
				if (minors.contains(gatiCounter))
					onminor()
				else
					ontick()
				return
			}

			//complete aksharam over
			gatiCounter = 0

			curIndex++

			if (curIndex == boxes.size())
				curIndex = 0
			
			previousBox = curBox
			curBox = boxes.get(curIndex)

			if (previousBox != null)
				previousBox.setBorder(Configs.OTHER_BORDER)
			
			curBox.setBorder(Configs.CUR_BORDER)
			//base.repaint()
			
			if (curIndex == 0)
				onavarthamStart()
			else
			{
				if (curBox.groupStart)
					ongroupStart()
				else
					onaksharamStart()	
			}
		}
		
		void ontick()
		{
				
		}
		
		void onminor()
		{
			pool.submit(new Tak(0.1, InstrumentName.panflute))
		}
		
		void onaksharamStart()
		{
			pool.submit(new Tak(0.1, InstrumentName.flute))
		}
		
		void ongroupStart()
		{
			pool.submit(new Tak(0.05, InstrumentName.tom808))
		}
		
		void onavarthamStart()
		{
			pool.submit(new Tak(0.2, InstrumentName.drum))
		}
		
		class Tak implements Runnable
		{
			Instrument ins
			double gatiPeriodDurationFactor
			
			Tak(double gatiPeriodDurationFactor, Instrument ins)
			{
				this.gatiPeriodDurationFactor = gatiPeriodDurationFactor
				this.ins = ins
			}
			
			@Override
			void run()
			{
				long duration = gatiPeriod * gatiPeriodDurationFactor
				channel.instrument(ins)
				channel.play(duration, CarnaticNotes.s)
			}
		}
		
		class GatiPanel
		{
			
		}
	}
	
}

/**
 * This class holds ticks per aksharam
 */
class TickTimings
{
	/**
	 * key   : elapsed millis from aksharam start
	 * value : Gati updates that are to be done during that millis
	 */
	private Map<Long, List<Integer>> map = [:]

	void add(double aksharaDuration, int beatsPerAksharam)
	{
		double oneBeatMillis = aksharaDuration / beatsPerAksharam

		for (int i = 0; i < beatsPerAksharam - 1; i++)
			putInMap(oneBeatMillis * (i + 1), beatsPerAksharam)
	}

	Map<String, List<Integer>> sortedMap()
	{
		def ret = [:]

		map.keySet().toList().sort().each {ret[it] = map[it]}

		return ret
	}

	private void putInMap(double key, Integer val)
	{
		long lk = Math.round(key)
		def e = map[lk]

		if (e == null)
		{
			e = []
			map[lk] = e
		}

		e << val
	}
}

class Base extends JPanel
{
	GatiPanelsBase gatiPanelsBase = new GatiPanelsBase()
	BlockPanel blockPanel = new BlockPanel()
	
	Base()
	{
		setLayout(new GridBagLayout())
		
		add(gatiPanelsBase, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0))
		add(blockPanel, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0))
	}
}

class GatiPanelsBase extends JPanel
{
	GatiPanelsBase()
	{
		setLayout(new GridBagLayout())
		setBorder(BorderFactory.createRaisedBevelBorder())
	}
}

class GatiPanel extends JPanel
{
	GatiPanel()
	{
		setLayout(new GridBagLayout())
	}
}

class BlockPanel extends JPanel
{
	BlockPanel()
	{
		layout = null
	}
}

@Log4j2
class Box extends JPanel
{
	int z
	int index
	int offset = 0
	boolean groupStart = false
	
	Box(boolean groupStart, int index, Color bg, int z)
	{
		this.groupStart = groupStart
		this.index = index
		this.z = z
		setSize Configs.BOX_WIDTH, Configs.BOX_HEIGHT
		background = bg
		position(index)
		setBorder(BorderFactory.createRaisedBevelBorder())
	}
	
	Box(int index, Color bg, int z)
	{
		this(false, index, bg, z)
	}

	void position(int index)
	{
		int x = Configs.BOX_PANEL_X_MARGIN + index * (width + (20 - offset * 2)) - offset
		int y = Configs.BOX_PANEL_Y_MARGIN - offset
		
		setLocation(x, y)

		//log.trace("index = {}, width = {}, offset = {}, Box location = {}", index, width, offset, location)
	}
	
	void position()
	{
		position(index)
	}
}

class Configs
{
	static final int BOX_WIDTH = 40
	static final int BOX_HEIGHT = 50
	
	
	static final int BOX_PANEL_X_MARGIN = 50
	static final int BOX_PANEL_Y_MARGIN = 50
	
	static final Border CUR_BORDER = BorderFactory.createLineBorder(Color.BLACK, 2)
	static final Border OTHER_BORDER = BorderFactory.createRaisedBevelBorder()
	
}