package gilli.datetime

import gilli.util.DSLHelper
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType


import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class DateTimeUtil
{
	static SimpleDateFormat DAILY_TABLE_NAME = new SimpleDateFormat("d_M_yyyy")
	static SimpleDateFormat DAYSTRING_FORMATTER = new SimpleDateFormat("EEEE")
	static SimpleDateFormat YYYY_MM_DD_FORMATTER = new SimpleDateFormat("yyyy_MM_dd")
	static SimpleDateFormat ISO_8601_24H_FULL_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

	/**
	 * Defined format is <code> dd-MMM-yyyy_HH-mm-ss</code> as defined by Java's {@link SimpleDateFormat}.
	 * 
	 * Example : "30-Jul-2018_23-36-33"
	 */
	static SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss")
	public static SimpleDateFormat ARBITRARY_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS")
	
	static String daystr(String yyyy_MM_dd)
	{
		return DAYSTRING_FORMATTER.format(date(yyyy_MM_dd))
	}
	
	static Date date(String yyyy_MM_dd)
	{
		return YYYY_MM_DD_FORMATTER.parse(yyyy_MM_dd)
	}
	
	static SimpleDateFormat dateformat(String pattern)
	{
		return new SimpleDateFormat(pattern)
	}
	
	static long toMillis(String formattedDateAndTime)
	{
		return SIMPLE_FORMAT.parse(formattedDateAndTime).getTime()
	}
	
	static String todayFormatted()
	{
		return nowFormatted().left(11)
	}

	static String dailyTableName(String prefix)
	{
		return prefix + '_' + dailyDateStr()
	}

	static String dailyDateStr()
	{
		DAILY_TABLE_NAME.format(now())
	}

	static String dailyTableNameYesterDay(String prefix)
	{
		return prefix + '_' + DAILY_TABLE_NAME.format(yesterday())
	}

	static String dailyDateStrYesterDay()
	{
		DAILY_TABLE_NAME.format(yesterday())
	}


	static String nowFormatted()
	{
		return SIMPLE_FORMAT.format(now())
	}

	static String nowArbitraryFormatted()
	{
		return ARBITRARY_FORMAT.format(now())
	}
	
	static long nowmillis()
	{
		System.currentTimeMillis()
	}
	
	static long nownanos()
	{
		System.nanoTime()
	}

	static boolean isEvenDay(int date =0)
	{
		! isOddDay(date)
	}

	static boolean isOddDay(int date = 0)
	{
		((((date ? date : Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) - 1) / 7 ).toInteger() % 2) == 0
	}
	
	static Date now()
	{
		return new Date()
	}

	static void every(Duration duration, Closure closure)
	{
		Timer timer = new Timer()

		TimerTask task = new TimerTask() {
			
			@Override
			void run() 
			{
				closure.call()
			}
		}

		timer.scheduleAtFixedRate(task, 100, duration.toMillis())
	}
	
	static TimeWindow todayTillnow()
	{
		long start = dayStart(now())
		long howlong = System.currentTimeMillis() - start

		return interval(start, howlong)
	}
	
	static TimeWindow today()
	{
		return day(now())
	}
	
	static TimeWindow day (Date anyTimeInThatDay, Long upTo = 1)
	{
		return interval(dayStart(anyTimeInThatDay), upTo.day.millis)
	}

	static long dayStart(Date day)
	{
		return dayStart(day.getTime())
	}
	
	static long dayStart(long millis)
	{
		return new Date(millis).clearTime().getTime()
	}
	
	static TimeWindow day(long startMillis)
	{
		return day(new Date(startMillis))
	}
	
	static TimeWindow hour (Date anyTimeInThatHour, Long upTo=1)
	{
		Calendar cal = Calendar.getInstance()
		cal.setTime(anyTimeInThatHour)
		cal.set(Calendar.MINUTE, 0)
		cal.set(Calendar.SECOND, 0)
		cal.set(Calendar.MILLISECOND, 0)
	
		interval cal.getTimeInMillis(), upTo.hours.getMillis()
	}
	
	static TimeWindow hour(long startMillis)
	{
		return hour(new Date(startMillis))	
	}

	static long todayStartMillis()
	{
		dayStartMillis(now())
	}

	static long dayStartMillis(Date date)
	{
		Calendar cal = Calendar.getInstance(); // locale-specific
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.getTimeInMillis();
	}

	static long todayEndMillis()
	{
		dayEndMillis(now())
	}

	static long dayEndMillis(Date date)
	{
		Calendar cal = Calendar.getInstance(); // locale-specific
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		cal.getTimeInMillis();
	}

	/**
	 * 
	 * Example 1:
	 * 
	 * {@code interval("12-jan-2017_12-22-22", "12-jan-2017_14-44-44")}
	 * 
	 * @param startTimeFormatted According to the format defined by {@link #SIMPLE_FORMAT}
	 * @param endTimeFormatted According to the format defined by {@link #SIMPLE_FORMAT}
	 * @return
	 */
	static TimeWindow interval(String startTimeFormatted, String endTimeFormatted)
	{
		long s = SIMPLE_FORMAT.parse(startTimeFormatted).getTime()
		long e = SIMPLE_FORMAT.parse(endTimeFormatted).getTime()
		
		return interval(s, e - s)
	}

	static TimeWindow interval(String startTimeFormatted, Duration duration)
	{
		interval SIMPLE_FORMAT.parse(startTimeFormatted), duration
	}

	static TimeWindow interval(String startTimeFormatted, Duration duration, String format)
	{
		interval dateformat(format).parse(startTimeFormatted), duration
	}

	static TimeWindow interval (Date startDate, Duration duration)
	{
		interval startDate, duration.toMillis()
	}

	static TimeWindow interval (Date startDate, long howLongMillis)
	{
		interval startDate.time , howLongMillis
	}
	
	static TimeWindow interval(long startMillis, long howLongMillis)
	{
		return new TimeWindow(startMillis, startMillis + howLongMillis)
	}
	
	static TimeWindow yesterday()
	{
		return day(now() - 1)
	}
	
	static TimeWindow previousSliding(Duration d)
	{
		return interval(now().getTime() - d.millis, d.millis)
	}
	
	static TimeWindow previous1Day()
	{
		return previous(1.day)
	}
	
	static TimeWindow previous2Day()
	{
		return previous(2.day)
	}
	
	static TimeWindow previous1Hour()
	{
		return previous(1.hour)
	}
	
	static TimeWindow previous2Hours()
	{
		return previous(2.hours)
	}
	
	static TimeWindow previous3Hours()
	{
		return previous(3.hours)
	}
	
	static TimeWindow previous4Hours()
	{
		return previous(4.hours)
	}
	
	static TimeWindow previous6Hours()
	{
		return previous(6.hours)
	}
	
	static TimeWindow previous8Hours()
	{
		return previous(8.hours)
	}
	
	static TimeWindow previous10Hours()
	{
		return previous(10.hours)
	}
	
	static TimeWindow previous12Hours()
	{
		return previous(12.hours)
	}
	
	static TimeWindow previous(Duration d)
	{
		def start = d.startTime(true)
		return interval(start, d.timeUnit.toMillis(d.value))
	}
	
	/**
	 * Returns the absolute Date object of tomorrow
	 * 
	 * @return
	 */
	static TimeWindow tomorrow()
	{
		return day(now() + 1)
	}

	static String getElapsedstr(Number millis)
	{
		new Duration(millis.longValue(), TimeUnit.MILLISECONDS).str()
	}

	/**
	 * Example1:
	 * <p>
	 * {@code slice(previous1Hour(), 5.minutes) {println START.datestr + " - " + END.datestr}}
	 * 
	 * <p>
	 * Example2:
	 * 
	 * <p>
	 * {@code slice(interval("12-jan-2017_12-22-22", "12-jan-2017_14-44-44"), 10.minutes) {println it}}
	 * 
	 * {@code dlf{}}
	 * 
	 * @param interval
	 * @param into
	 * @param closure
	 * @return
	 */
	static List<TimeWindow> slice(TimeWindow interval, Duration into, Closure closure = {})
	{
		return interval.slice(into, closure)
	}

	static List<TimeWindow> slice(Duration into, Closure closure = {})
	{
		return slice(previous(into), into, closure)
	}
	
	static void schedule(@DelegatesTo(TimeBoundJob) Closure closure)
	{
		def job = DSLHelper.callAndGetDelegate(closure, new TimeBoundJob())
		job.run()
	}

	static void schedule(Duration maxTimeForJob, Duration callInterval, Closure retryJobWorkClosure)
	{
		schedule {
			
			maxTime maxTimeForJob
			interval callInterval
			
			work retryJobWorkClosure
		}
	}

	static void sleep(Duration duration)
	{
		try
		{
			Thread.sleep(duration.millis)
		}
		catch (Exception ex)
		{
		}
	}
}

class TimeBoundJob
{
	long maxPeriod
	long interval
	
	Closure workClosure
	Closure<Boolean> breakWhenClosure
	
	void maxTime(long max)
	{
		this.maxPeriod = max
	}
	
	void maxTime(Duration duration)
	{
		maxTime(duration.millis)
	}
	
	void interval(long intervalMillis)
	{
		this.interval = intervalMillis
	}
	
	void interval(Duration duration)
	{
		interval(duration.millis)
	}
	
	void work(Closure closure)
	{
		this.workClosure = closure
	}
	
	void breakWhen(Closure<Boolean> closure)
	{
		this.breakWhenClosure = closure
	}
	
	void run()
	{
		long whenToBreak = System.currentTimeMillis() + maxPeriod

		int callId = 0
		
		while (true)
		{
			if (breakWhenClosure)
			{
				if (breakWhenClosure(callId++))
					break
			}
			
			if (System.currentTimeMillis() > whenToBreak)
				break
			
			workClosure()
			
			sleep(interval)
		}
	}
}

class TimeWindow
{
	long start
	long end
	Duration duration

	TimeWindow(long start, long end)
	{
		duration = Duration.ofMillis(end - start)
		this.start = start
		this.end = end
	}

	List<TimeWindow> slice(Duration duration, @ClosureParams(value = SimpleType, options = ["TimeWindow"]) Closure closure = {})
	{
		slice(duration.millis, closure)
	}

	List<TimeWindow> slice(long intoMillis, @ClosureParams(value = SimpleType, options = ["TimeWindow"]) Closure closure = {})
	{
		List<TimeWindow> intervals = []

		for (long i = start; i < end; i += intoMillis)
		{
			long to = Math.min(i + intoMillis, end)

			intervals << new TimeWindow(i, to)
		}

		intervals.each { it.work(closure) }

		return intervals
	}

	void work(Closure closure)
	{
		//Binding binding = new Binding()

		//binding.setVariable('START', start)
		//binding.setVariable('END', end)

		//closure.setBinding(binding)
		closure.call(this)
	}

	@Override
	String toString()
	{
		return start.datestr + " -> " + end.datestr
	}
}
