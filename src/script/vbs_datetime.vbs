'This file contains some VBS example to handle date and time
Now   = 2/29/2016 1:02:03 PM
Date  = 2/29/2016
Time  = 1:02:03 PM
Timer = 78826.31     ' seconds since midnight

FormatDateTime(Now)                = 2/29/2016 1:02:03 PM
FormatDateTime(Now, vbGeneralDate) = 2/29/2016 1:02:03 PM
FormatDateTime(Now, vbLongDate)    = Monday, February 29, 2016
FormatDateTime(Now, vbShortDate)   = 2/29/2016
FormatDateTime(Now, vbLongTime)    = 1:02:03 PM
FormatDateTime(Now, vbShortTime)   = 13:02

Year(Now)   = 2016
Month(Now)  = 2
Day(Now)    = 29
Hour(Now)   = 13
Minute(Now) = 2
Second(Now) = 3

Year(Date)   = 2016
Month(Date)  = 2
Day(Date)    = 29

Hour(Time)   = 13
Minute(Time) = 2
Second(Time) = 3

Function LPad (str, pad, length)
    LPad = String(length - Len(str), pad) & str
End Function

LPad(Month(Date), "0", 2)    = 02
LPad(Day(Date), "0", 2)      = 29
LPad(Hour(Time), "0", 2)     = 13
LPad(Minute(Time), "0", 2)   = 02
LPad(Second(Time), "0", 2)   = 03

Weekday(Now)                     = 2
WeekdayName(Weekday(Now), True)  = Mon
WeekdayName(Weekday(Now), False) = Monday
WeekdayName(Weekday(Now))        = Monday

MonthName(Month(Now), True)  = Feb
MonthName(Month(Now), False) = February
MonthName(Month(Now))        = February

Set os = GetObject("winmgmts:root\cimv2:Win32_OperatingSystem=@")
os.LocalDateTime = 20131204215346.562000-300
Left(os.LocalDateTime, 4)    = 2013 ' year
Mid(os.LocalDateTime, 5, 2)  = 12   ' month
Mid(os.LocalDateTime, 7, 2)  = 04   ' day
Mid(os.LocalDateTime, 9, 2)  = 21   ' hour
Mid(os.LocalDateTime, 11, 2) = 53   ' minute
Mid(os.LocalDateTime, 13, 2) = 46   ' second

Set timeZones = wmi.ExecQuery("SELECT Bias, Caption FROM Win32_TimeZone")
For Each tz In timeZones
    tz.Bias    = -300
    tz.Caption = (UTC-05:00) Eastern Time (US & Canada)
Next