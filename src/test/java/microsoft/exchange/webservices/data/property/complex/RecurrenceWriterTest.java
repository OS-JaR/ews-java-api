
package microsoft.exchange.webservices.data.property.complex;

import microsoft.exchange.webservices.base.BaseTest;
import microsoft.exchange.webservices.data.core.EwsServiceXmlWriter;
import microsoft.exchange.webservices.data.core.enumeration.property.time.DayOfTheWeek;
import microsoft.exchange.webservices.data.property.complex.recurrence.pattern.Recurrence.WeeklyPattern;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class RecurrenceWriterTest extends BaseTest
{
    @Test
    public void testWeeklyPattern() throws Exception
    {
        OutputStream output = new ByteArrayOutputStream();
        EwsServiceXmlWriter writer = new EwsServiceXmlWriter(exchangeServiceMock, output);

        WeeklyPattern weekly = new WeeklyPattern();
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(1);
        weekly.setFirstDayOfWeek(DayOfTheWeek.Saturday);
        weekly.setStartDate(new Date());
        weekly.writeElementsToXml(writer);

        assertEquals(DayOfTheWeek.Saturday, weekly.getFirstDayOfWeek());
    }
}
