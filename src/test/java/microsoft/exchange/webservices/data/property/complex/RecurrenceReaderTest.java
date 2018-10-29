/*
 * The MIT License Copyright (c) 2012 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package microsoft.exchange.webservices.data.property.complex;

import microsoft.exchange.webservices.data.core.EwsServiceXmlReader;
import microsoft.exchange.webservices.data.core.XmlElementNames;
import microsoft.exchange.webservices.data.core.enumeration.misc.XmlNamespace;
import microsoft.exchange.webservices.data.core.enumeration.property.time.DayOfTheWeek;
import microsoft.exchange.webservices.data.core.enumeration.property.time.DayOfTheWeekIndex;
import microsoft.exchange.webservices.data.core.enumeration.property.time.Month;
import microsoft.exchange.webservices.data.property.complex.recurrence.pattern.Recurrence;
import microsoft.exchange.webservices.data.property.complex.recurrence.pattern.Recurrence.MonthlyPattern;
import microsoft.exchange.webservices.data.property.complex.recurrence.pattern.Recurrence.YearlyPattern;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

public class RecurrenceReaderTest {

  @Test
  public void testMonthlyPattern() throws Exception {

    EwsServiceXmlReader reader = Mockito.mock(EwsServiceXmlReader.class);
    doReturn(XmlElementNames.DayOfMonth).when(reader).getLocalName();
    doReturn(1).when(reader).readElementValue(Integer.class);

    MonthlyPattern monthly = new MonthlyPattern();
    monthly.tryReadElementFromXml(reader);

    assertEquals(1, monthly.getDayOfMonth());
  }

  @Test
  public void testMonthlyRegenarationPattern() throws Exception {

    EwsServiceXmlReader reader = Mockito.mock(EwsServiceXmlReader.class);
    doReturn(XmlElementNames.Interval).when(reader).getLocalName();
    doReturn(-1234).when(reader).readElementValue(Integer.class);

    Recurrence.MonthlyRegenerationPattern monthly = new Recurrence.MonthlyRegenerationPattern();
    monthly.tryReadElementFromXml(reader);

    assertEquals(-1234, monthly.getInterval());
  }

  @Test
  public void testYearlyPattern() throws Exception {

    EwsServiceXmlReader reader = Mockito.mock(EwsServiceXmlReader.class);
    doReturn(XmlElementNames.DayOfMonth).when(reader).getLocalName();
    doReturn(2).when(reader).readElementValue(Integer.class);

    YearlyPattern yearly = new YearlyPattern();
    yearly.tryReadElementFromXml(reader);

    assertEquals(2, yearly.getDayOfMonth());
  }

  @Test
  public void testYearlyRegenarationPattern() throws Exception {

    EwsServiceXmlReader reader = Mockito.mock(EwsServiceXmlReader.class);
    doReturn(XmlElementNames.Interval).when(reader).getLocalName();
    doReturn(1234).when(reader).readElementValue(Integer.class);

    Recurrence.YearlyRegenerationPattern yearly = new Recurrence.YearlyRegenerationPattern();
    yearly.tryReadElementFromXml(reader);

    assertEquals(1234, yearly.getInterval());
  }

  @Test
  public void testWeeklyPattern() throws Exception {

    EwsServiceXmlReader reader = Mockito.mock(EwsServiceXmlReader.class);
    doReturn(XmlElementNames.FirstDayOfWeek).when(reader).getLocalName();
    doReturn(DayOfTheWeek.Saturday).when(reader).readElementValue(DayOfTheWeek.class, XmlNamespace.Types, XmlElementNames.FirstDayOfWeek);

    Recurrence.WeeklyPattern weekly = new Recurrence.WeeklyPattern();
    weekly.tryReadElementFromXml(reader);

    assertEquals(DayOfTheWeek.Saturday, weekly.getFirstDayOfWeek());
  }

  @Test
  public void testWeeklyRegenarationPattern() throws Exception {

    EwsServiceXmlReader reader = Mockito.mock(EwsServiceXmlReader.class);
    doReturn(XmlElementNames.Interval).when(reader).getLocalName();
    doReturn(3).when(reader).readElementValue(Integer.class);

    Recurrence.WeeklyRegenerationPattern daily = new Recurrence.WeeklyRegenerationPattern();
    daily.tryReadElementFromXml(reader);

    assertEquals(3, daily.getInterval());
  }

  @Test
  public void testDailyPattern() throws Exception {

    EwsServiceXmlReader reader = Mockito.mock(EwsServiceXmlReader.class);
    doReturn(XmlElementNames.Interval).when(reader).getLocalName();
    doReturn(42).when(reader).readElementValue(Integer.class);

    Recurrence.DailyPattern daily = new Recurrence.DailyPattern();
    daily.tryReadElementFromXml(reader);

    assertEquals(42, daily.getInterval());
  }

  @Test
  public void testDailyRegenarationPattern() throws Exception {

    EwsServiceXmlReader reader = Mockito.mock(EwsServiceXmlReader.class);
    doReturn(XmlElementNames.Interval).when(reader).getLocalName();
    doReturn(17).when(reader).readElementValue(Integer.class);

    Recurrence.DailyRegenerationPattern daily = new Recurrence.DailyRegenerationPattern();
    daily.tryReadElementFromXml(reader);

    assertEquals(17, daily.getInterval());
  }

  @Test
  public void testRelativeMonthlyPattern() throws Exception {

    EwsServiceXmlReader reader = Mockito.mock(EwsServiceXmlReader.class);
    doReturn(XmlElementNames.DaysOfWeek).when(reader).getLocalName();
    doReturn(DayOfTheWeek.Saturday).when(reader).readElementValue(DayOfTheWeek.class);

    Recurrence.RelativeMonthlyPattern rm = new Recurrence.RelativeMonthlyPattern();
    rm.tryReadElementFromXml(reader);

    assertEquals(DayOfTheWeek.Saturday, rm.getDayOfTheWeek());

    doReturn(XmlElementNames.DayOfWeekIndex).when(reader).getLocalName();
    doReturn(DayOfTheWeekIndex.Last).when(reader).readElementValue(DayOfTheWeekIndex.class);
    rm.tryReadElementFromXml(reader);

    assertEquals(DayOfTheWeekIndex.Last, rm.getDayOfTheWeekIndex());
  }

  @Test
  public void testRelativeYearlyPattern() throws Exception {

    EwsServiceXmlReader reader = Mockito.mock(EwsServiceXmlReader.class);
    doReturn(XmlElementNames.DaysOfWeek).when(reader).getLocalName();
    doReturn(DayOfTheWeek.Saturday).when(reader).readElementValue(DayOfTheWeek.class);

    Recurrence.RelativeYearlyPattern ry = new Recurrence.RelativeYearlyPattern();
    ry.tryReadElementFromXml(reader);

    assertEquals(DayOfTheWeek.Saturday, ry.getDayOfTheWeek());

    doReturn(XmlElementNames.DayOfWeekIndex).when(reader).getLocalName();
    doReturn(DayOfTheWeekIndex.Last).when(reader).readElementValue(DayOfTheWeekIndex.class);
    ry.tryReadElementFromXml(reader);

    assertEquals(DayOfTheWeekIndex.Last, ry.getDayOfTheWeekIndex());

    doReturn(XmlElementNames.Month).when(reader).getLocalName();
    doReturn(Month.October).when(reader).readElementValue(Month.class);
    ry.tryReadElementFromXml(reader);

    assertEquals(Month.October, ry.getMonth());
  }
}
