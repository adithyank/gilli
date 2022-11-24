package gilli.extras.midi

import gilli.internal.main.Gilli
import groovy.util.logging.Log4j2

import javax.sound.midi.Instrument
import javax.sound.midi.MidiChannel
import javax.sound.midi.MidiSystem
import javax.sound.midi.Synthesizer

class Midi
{
	static void printInstrumentNames()
	{
		InstrumentName.list().each {
			Gilli.stdoutWithoutTime.info(it.name)
		}
	}
	static void printInstruments()
	{
		InstrumentName.list().each {
			Gilli.stdoutWithoutTime.info(it.name + ":" + it.properties + ":" + it.soundbank)
		}
	}

	static void cplay(long oneNoteDuration, List<Note> notes)
	{
		cplay(InstrumentName.violin2, oneNoteDuration, notes)
	}
	
	static void cplay(long oneNoteDuration, Note... notes)
	{
		cplay(oneNoteDuration, notes.toList())
	}

	static void cplay(Instrument instrument, long oneNoteDuration, List<Note> notes)
	{
		def ch = musicch()
		ch.instrument(instrument)
		ch.play(oneNoteDuration, notes)
	}
	
	static void cplay(Instrument instrument, long oneNoteDuration, Note... notes)
	{
		cplay(instrument, oneNoteDuration, notes.toList())
	}
	
	static void cplay(Note... notes)
	{
		musicch().play(notes)	
	}
	
	static SoundChannel musicch(int channelNo = 0)
	{
		return music().channel(channelNo)
	}
	
	static Music music(@DelegatesTo(Music) Closure closure = {})
	{
		Music music = new Music()

		closure.delegate = music
		closure.resolveStrategy = Closure.DELEGATE_ONLY

		closure()

		return music
	}

	static class Music
	{
		static Synthesizer synthesizer

		static 
		{
			synthesizer = MidiSystem.getSynthesizer()
			synthesizer.open()
		}
		
		SoundChannel channel(int channelNo, @DelegatesTo(SoundChannel) Closure closure = {})
		{
			MidiChannel channel = synthesizer.getChannels()[channelNo]
			SoundChannel ch = new SoundChannel(channel, this)

			closure.delegate = ch
			closure.resolveStrategy = Closure.DELEGATE_ONLY

			closure()
			
			return ch
		}
	}

	@Log4j2
	static class SoundChannel
	{
		private MidiChannel channel

		private Music music

		private long oneNoteDuration = 1000
		
		SoundChannel(MidiChannel channel, Music music)
		{
			this.channel = channel
			this.music = music
		}

		void oneNoteDuration(long oneNoteDuration)
		{
			log.trace("Setting oneNoteDuration = {}", oneNoteDuration)
			this.oneNoteDuration = oneNoteDuration
		}
		
		void instrument(Instrument ins)
		{
			log.trace('Setting instrument : {}', ins)
			channel.programChange(ins.patch.bank, ins.patch.program)
		}

		void play(Instrument ins, long oneNoteDuration, Note... notes)
		{
			instrument(ins)
			play(oneNoteDuration, notes)
		}

		void play(long oneNoteDuration1, List<Note> notes)
		{
			oneNoteDuration(oneNoteDuration1)
			play notes
		}
		
		void play(long oneNoteDuration1, Note... notes)
		{
			play(oneNoteDuration1, notes.toList())
		}

		void play(Note... notes)
		{
			play notes.toList()
		}
		
		void play(List<Note> notes)
		{
			notes.each {play it}
		}
		
		void play(Note note)
		{
			play(note, oneNoteDuration)
		}
		
		void play(Note note, long thisOneNoteDuration)
		{
			log.trace('playing : {}', note)
			channel.noteOn(note.val, 10000)
			sleep(thisOneNoteDuration * note.length)
			channel.noteOff(note.val)
		}
	}
}

class InstrumentName
{
	static final Instrument[] instruments = Midi.Music.synthesizer.getLoadedInstruments()

	static Instrument violin1 = instruments.find {it.name == 'Violin'}
	static Instrument flute = instruments.find {it.name == 'Flute'}
	static Instrument panflute = instruments.find {it.name == 'Pan Flute'}

	static List<Instrument> list()
	{
		return instruments.toList()
	}
	
	static Instrument ins(int number)
	{
		return instruments[number]
	}
}

class Note
{
	int val

	String label
	boolean lengthSet = false
	int length = 1

	Note(int val, String label)
	{
		this.val = val
		this.label = label
	}

	Note getAt(int times)
	{
		if (!lengthSet)
		{
			length = times
			lengthSet = true
		}
		else
		{
			length += times
		}

		return this
	}

	@Override
	String toString()
	{
		return label + 'x' + length
	}
}
