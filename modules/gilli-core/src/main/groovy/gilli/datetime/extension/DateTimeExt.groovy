package gilli.datetime.extension

import gilli.datetime.DateTimeUtil

import java.time.Duration

//import gilli.datetime.Duration

class DateTimeExt
{
	static Duration getDay(Number self)
	{
	 	return Duration.ofDays(self.longValue())
	}

	static long getMillis(Duration self)
	{
		return self.toMillis()
	}

	static String str(Duration self)
	{
		int d = self.millis / 1.day.millis

		long rem = self.millis - (d * 1.day.millis)

		int h = rem / 1.hour.millis

		rem = rem - (h * 1.hour.millis)

		int m = rem / 1.minute.millis

		rem = rem - (m * 1.minute.millis)

		int s = rem / 1.second.millis

		rem = rem - (s * 1.second.millis)

		int ms = rem
		//return "$d d, $h hr, $m mins, $s secs"

		def res = []

		if (d > 0)
			res << d + "d"

		if (h > 0)
			res << h + "hr"

		if (m > 0)
			res << m + "m"

		if (s > 0)
			res << s + "s"

		if (ms > 0)
			res << ms + "ms"

		res ? res.join(' ') : '0ms'
	}


	static Duration getDays(Number self)
	{
	 	return Duration.ofDays(self.longValue())
	}
	
	static Duration getHour(Number self)
	{
	 	return Duration.ofHours(self.longValue())
	}
	
	static Duration getHours(Number self)
	{
	 	return Duration.ofHours(self.longValue())
	}
	
	static long getHourMillis(Number self)
	{
	 	return Duration.ofHours(self.longValue()).millis
	}

	static Duration getMinute(Number self)
	{
	 	return Duration.ofMinutes(self.longValue())
	}
	
	static Duration getMinutes(Number self)
	{
	 	return Duration.ofMinutes(self.longValue())
	}
	
	static long getMinuteMillis(Number self)
	{
	 	return Duration.ofMinutes(self.longValue()).millis
	}
	
	static Duration getSecond(Number self)
	{
	 	return Duration.ofSeconds(self.longValue())
	}
	
	static Duration getSeconds(Number self)
	{
	 	return Duration.ofSeconds(self.longValue())
	}
	
	static long getSecondMillis(Number self)
	{
	 	return Duration.ofSeconds(self.longValue()).millis
	}
	
	static Duration getMillis(Number self)
	{
		Duration.ofMillis(self.longValue())
	}

	static String getDailyTableName(Number self)
	{
		return DateTimeUtil.DAILY_TABLE_NAME.format(new Date(self.longValue()))
	}

	static Date getDate(Number self) {return new Date(self.longValue())}

	static String getDatestr (Number self, String format)
	{
		DateTimeUtil.dateformat(format).format(new Date(self.longValue()))
	}

	static long getDateNumber(Number self)
	{
		getDatestr(self, 'yyyyMMdd') as long
	}
	
	static String getDatestr(Number self) {new Date(self.longValue()).toString()}

	static String getElapsedstr(Number self) {DateTimeUtil.getElapsedstr(self)}

	/**
	 * Format defined by {@link DateTimeUtil#SIMPLE_FORMAT}
	 * @param formattedDateAndTime
	 * @return
	 */
	static long toMillis(String formattedDateAndTime)
	{
		return DateTimeUtil.toMillis(formattedDateAndTime)
	}
}