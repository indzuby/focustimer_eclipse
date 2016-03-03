////////////////////////////////////////////////////////////////////////////////////////////////////
/// Timestamp Class
/// Update: 2011-12-12

package com.realnumworks.focustimer.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Timestamp
{
	private static final long K1div32chip	= 49152;	// unit: 1/32 chip
	private static final long K100ns		= 12500;	// unit: 100-nanosecond

	public  static int TIMEZONE_OFFSET = 0;				// TimeZone Offset
	private static final long UTC_1970_1_1_0_0_0;		// 1970-1-1 0:0:0 Millis
	private static final long UTC_1980_1_6_0_0_0;		// 1980-1-6 0:0:0 Millis

	private static SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat SDF_DATETIME_MS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static SimpleDateFormat SDF_DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat SDF_TIME_MS = new SimpleDateFormat("HH:mm:ss.SSS");
	private static SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss");

	public static long mADB_TimeDiff = 0;
	
	static
	{
		// TimeZone RawOffset
		TIMEZONE_OFFSET = TimeZone.getDefault().getRawOffset();
		
		// 1970-1-1 0:0:0 Millis
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		cal.set(1970, 1-1, 1, 0, 0, 0);
		UTC_1970_1_1_0_0_0 = cal.getTimeInMillis();

		// 1980-1-6 0:0:0 Millis
		cal.set(1980, 1-1, 6, 0, 0, 0);
		UTC_1980_1_6_0_0_0 = cal.getTimeInMillis();
	}

	public static long LocaltimeToQualcommtime(long localtime)
	{
		localtime += mADB_TimeDiff;
		
		long dt = UTC_1980_1_6_0_0_0 - UTC_1970_1_1_0_0_0;
		long qualcommtime = (localtime - dt) * 1000;
		qualcommtime *= 10;

		long nano100 = (qualcommtime % K100ns);	// unit: 100-nanosecond
		long chip = (long)((double)nano100 / K100ns * K1div32chip + 0.5);

		qualcommtime /= K100ns;
		qualcommtime = (qualcommtime << 16) + chip;

		return qualcommtime;
	}

    public static long QualcommtimeToLocaltime(long qualcommtime)
    {
        long qualcommtime_ms = qualcommtime;
        qualcommtime_ms = qualcommtime_ms & 0xFFFFFFFFFFFF0000L;
        qualcommtime_ms = qualcommtime_ms >>> 16;

		long qualcomm_chips = qualcommtime & 0x00000000000000FFFFL;
        long seconds = (qualcommtime_ms / 800) + 315964800L;
        long a = (qualcommtime_ms % 800) * 1250;
        long b = (((qualcomm_chips * 1250) / 49152));
        long microseconds = a + b;

        long localtime = UTC_1970_1_1_0_0_0 + (seconds * 1000) + (microseconds / 1000);

        return localtime;
    }

	public static String ToDateString(Calendar time)
	{
		return SDF_DATE.format(time.getTime());
	}

	public static String ToDateString(long time)
	{
		return SDF_DATE.format(time);
	}

	public static String ToDatetimeString(long time, boolean isMS)
	{
		return isMS ? SDF_DATETIME_MS.format(time) : SDF_DATETIME.format(time);
	}

	public static String ToTimeString(long time, boolean isMS)
	{
		return isMS ? SDF_TIME_MS.format(time) : SDF_TIME.format(time);
	}

	public static String ToTZDatetimeString(long time, boolean isMS)
	{
		time += TIMEZONE_OFFSET;
		return isMS ? SDF_DATETIME_MS.format(time) : SDF_DATETIME.format(time);
	}

	public static String ToTZTimeString(long time, boolean isMS)
	{
		time += TIMEZONE_OFFSET;
		return isMS ? SDF_TIME_MS.format(time) : SDF_TIME.format(time);
	}

	public static String QualcommtimeToLocalDatetimeString(long qualcommtime, boolean isMS)
	{
		long localtime = QualcommtimeToLocaltime(qualcommtime);
		return Timestamp.ToDatetimeString(localtime, isMS);
	}

	public static String QualcommtimeToLocaltimeString(long qualcommtime, boolean isMS)
	{
		long localtime = QualcommtimeToLocaltime(qualcommtime);
		return Timestamp.ToTimeString(localtime, isMS);
	}
	
	// Convert struct timeval to milliseconds
	public static long TimevalToLocaltime(long sec, long usec)
	{
		return (sec * 1000) + (usec / 1000);
	}
}
