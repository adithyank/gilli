package gilli.extras.panchapakshi

import gilli.extras.geo.Geo
import gilli.http.HttpClient
import gilli.internal.main.Gilli
import gilli.util.dataframe.DataFrame
import groovy.transform.ToString

import java.text.SimpleDateFormat

class Util
{
    static <T> List<T> circle(def list, T current)
    {
        List<T> copylist = list.toList()

        while (copylist[0] != current)
            copylist.add(copylist.remove(0))

        return copylist
    }
}

enum Paksham
{
    VALAR_PIRAI_DAY([Thozil.OON, 1.25,
                     Thozil.NADAI, 1.5,
                     Thozil.ARASU, 2,
                     Thozil.THUYIL, 0.75,
                     Thozil.SAAVU, 0.5],

            [Pakshi.VALLOORU,
             Pakshi.AANDHAI,
             Pakshi.VALLOORU,
             Pakshi.AANDHAI,
             Pakshi.KAAGAM,
             Pakshi.KOZHI,
             Pakshi.MAYIL
            ]),


    VALAR_PIRAI_NIGHT([Thozil.OON, 1.25,
                       Thozil.ARASU, 2,
                       Thozil.SAAVU, 0.5,
                       Thozil.NADAI, 1.5,
                       Thozil.THUYIL, 0.75],

            [Pakshi.KAAGAM,
             Pakshi.KOZHI,
             Pakshi.KAAGAM,
             Pakshi.KOZHI,
             Pakshi.MAYIL,
             Pakshi.VALLOORU,
             Pakshi.AANDHAI
            ]),


    THEI_PIRAI_DAY([Thozil.OON, 2,
                    Thozil.SAAVU, 1.25,
                    Thozil.THUYIL, 0.5,
                    Thozil.ARASU, 0.75,
                    Thozil.NADAI, 1.5],

            [Pakshi.KOZHI,
             Pakshi.MAYIL,
             Pakshi.KOZHI,
             Pakshi.KAAGAM,
             Pakshi.AANDHAI,
             Pakshi.VALLOORU,
             Pakshi.MAYIL
            ]),


    THEI_PIRAI_NIGHT([Thozil.OON, 1.75,
                      Thozil.THUYIL, 0.75,
                      Thozil.NADAI, 1.75,
                      Thozil.SAAVU, 1,
                      Thozil.ARASU, 0.75],

            [Pakshi.VALLOORU,
             Pakshi.KOZHI,
             Pakshi.VALLOORU,
             Pakshi.AANDHAI,
             Pakshi.KAAGAM,
             Pakshi.MAYIL,
             Pakshi.KOZHI
            ]);

    private Map<Thozil, Double> orderedThozilToNaazhigai = [:]
    private Map<Day, Pakshi> adhikaaraPakshi = [:]

    private Paksham(List orderedThozilToNaazhigai, List dayWiseAdhikaraPakshi)
    {
        for (int i = 0; i < orderedThozilToNaazhigai.size(); i += 2)
            this.orderedThozilToNaazhigai[orderedThozilToNaazhigai[i]] = orderedThozilToNaazhigai[i + 1]

        Day.values().eachWithIndex {Day o, int i -> adhikaaraPakshi[o] = dayWiseAdhikaraPakshi[i]}
    }

    Pakshi adhikaaraPakshi(Day day)
    {
        return adhikaaraPakshi[day]
    }

    Map<Thozil, Double> getOrderedThozilToNaazhigai()
    {
        return orderedThozilToNaazhigai
    }

    List<Thozil> orderedThozils()
    {
        return orderedThozilToNaazhigai.keySet().toList()
    }

    void forEachJamam(Closure closure)
    {
        (1..5).each {closure.call(it.intValue())}
    }

    static List<Paksham> list(boolean valarPirai)
    {
        if (valarPirai)
            return [VALAR_PIRAI_DAY, VALAR_PIRAI_NIGHT]
        else
            return [THEI_PIRAI_DAY, THEI_PIRAI_NIGHT]
    }

    double naazhigai(Thozil thozil)
    {
        return orderedThozilToNaazhigai[thozil]
    }

    int minutes(Thozil thozil)
    {
        return naazhigai(thozil) * 24
    }
}

enum Day
{
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY
}

enum Pakshi
{
    VALLOORU(PanchaBootham.NILAM),
    AANDHAI(PanchaBootham.NEER),
    KAAGAM(PanchaBootham.NERUPU),
    KOZHI(PanchaBootham.KAATRU),
    MAYIL(PanchaBootham.AAGAAYAM);

    private equivalentPanchaBootham

    private Pakshi(PanchaBootham panchaBootham)
    {
        this.equivalentPanchaBootham = panchaBootham
    }
}

enum Thozil
{
    OON,
    NADAI,
    ARASU,
    THUYIL,
    SAAVU
}

enum PanchaBootham
{
    NILAM,
    NEER,
    NERUPU,
    KAATRU,
    AAGAAYAM
}

class PanchaPakshiCharter
{
    static SimpleDateFormat HOURMINS   = new SimpleDateFormat("HH:mm")
    static SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy_MM_dd")
    static SimpleDateFormat DAY = new SimpleDateFormat("EEEE")

    static void printChart(String address, String yyyy_mm_dd, boolean valarPirai)
    {
        Gilli.stdout.info("Given Arguments: Address: $address, yyyy_mm_dd: $yyyy_mm_dd, valarpirai: $valarPirai")
        Date date = YYYY_MM_DD.parse(yyyy_mm_dd)

        HourMinuteSec sunrise = SunriseGetter.getSunRiseTime(address, yyyy_mm_dd)

        printChart(sunrise.h, sunrise.m, DAY.format(date), valarPirai)
    }

    static void printChart(int sunRiseHours, int sunRiseMinutes, String tamilDayString, boolean valarPirai)
    {
        Day day = Day.valueOf(tamilDayString.toUpperCase())

        println "Sun Rise = $sunRiseHours hours, $sunRiseMinutes minutes"
        println "Tamil Day String = $tamilDayString"
        println "Paksham = " + ((valarPirai) ? "VALAR PIRAI" : "THEI PIRAI")

        Calendar sunrise = Calendar.getInstance()
        sunrise.clear()
        sunrise.set(Calendar.HOUR_OF_DAY, sunRiseHours)
        sunrise.set(Calendar.MINUTE, sunRiseMinutes)

        DataFrame d = new DataFrame()
        d.header("Paksham", "Jamam", "Pakshi", "Thozil", "Naazhigai", "Minutes", "Start", "End")

        Paksham.list(valarPirai).each {

            Paksham paksham = it
            Pakshi adhikaaraPakshi = paksham.adhikaaraPakshi(day)
            List<Thozil> orderedThozils = paksham.orderedThozils()

            List<Pakshi> finalPakshiOrder = Util.circle(Pakshi.values(), adhikaaraPakshi)

            paksham.forEachJamam { int jamamNo ->

                int mins = (jamamNo - 1) * 144

                //sunrise.add(Calendar.MINUTE, mins)

                Thozil thozilOfAdhikaaraPakshiInThisJamam = orderedThozils[jamamNo - 1]

                List<Thozil> finalThozhilOrder = Util.circle(orderedThozils, thozilOfAdhikaaraPakshiInThisJamam)

                for (int ak = 0; ak < finalPakshiOrder.size(); ak++)
                {
                    String start = HOURMINS.format(sunrise.getTime())
                    Thozil thozil = finalThozhilOrder[ak]
                    int minsoffset = paksham.minutes(thozil)
                    sunrise.add(Calendar.MINUTE, minsoffset)
                    String end = HOURMINS.format(sunrise.getTime())
                    d.row(paksham.name(), jamamNo, finalPakshiOrder[ak], thozil, paksham.naazhigai(thozil), minsoffset, start, end)
                }

            }
        }

        d.printtable()
    }
}

@ToString
class HourMinuteSec
{
    int h
    int m
    int s

    HourMinuteSec(int h, int m)
    {
        this.h = h
        this.m = m
    }
}

class SunriseGetter
{
    static HourMinuteSec getSunRiseTime(String address, String yyyy_mm_dd)
    {
        Map latlng = Geo.latlong(address)
        return getSunRiseTime(latlng.lat, latlng.lng, yyyy_mm_dd);
    }

    static HourMinuteSec getSunRiseTime(String latitude, String longitude, String yyyy_mm_dd)
    {
        yyyy_mm_dd = yyyy_mm_dd.replace('_', '-')

        HourMinuteSec ret = null

        HttpClient.GET {

            host "https://api.sunrise-sunset.org"
            path "json"

            param 'lat', latitude
            param 'lng', longitude
            param 'date', yyyy_mm_dd

            //rawtextresponse {println it}

            response {
                json { root ->

                    String rise = root.results.sunrise

                    println "risee = " + rise

                    int h = rise.takeBefore(':').toInteger()

                    //int m = rise.substring(3, 5).toInteger()
                    //int s = rise.substring(6, 8).toInteger()

                    int m = rise.takeAfter(':').takeBefore(':').toInteger()
                    int s = rise.takeAfter(':').takeAfter(':').takeBefore(' ').toInteger()

                    String ampm = rise.takeRight(2)

                    if (h == 12 && ampm == 'AM')
                        h = 0

                    //println "h = $h, m = $m, s = $s, ampm = $ampm"

                    Calendar cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR, h)
                    cal.set(Calendar.MINUTE, m)
                    cal.set(Calendar.SECOND, s)
                    cal.set(Calendar.AM_PM, ampm == "AM" ? Calendar.AM : Calendar.PM)

                    cal.add(Calendar.HOUR, 5)
                    cal.add(Calendar.MINUTE, 30)

                    ret = new HourMinuteSec(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

                    println "ret = $ret"
                    //println "converted = " + calendar.toInstant().atZone(ZoneId.of("Asia/Kolkata"))
                }
            }
        }
        return ret
    }
}

class Ammavasya
{

}

//PanchaPakshiCharter.printChart("06_30", "Friday", false)

//println SunriseGetter.getSunRiseTime("mettupalayam", "1982_05_02")

//PanchaPakshiCharter.printChart("mettupalayam", "1982_05_01", "saturday", true)
//PanchaPakshiCharter.printChart("mettupalayam", "1980_07_04", "friday", false)
//PanchaPakshiCharter.printChart("chennai", "2022_06_29", "wednesday", true)
println '==========================================='
PanchaPakshiCharter.printChart("chennai", "2022_09_03", true)

//PanchaPakshiCharter.printChart("pattukottai", "1987_06_05", "friday", true)
