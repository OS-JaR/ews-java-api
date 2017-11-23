/*
 * The MIT License
 * Copyright (c) 2012 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package microsoft.exchange.webservices.data.utils;

import microsoft.exchange.webservices.data.core.EwsXmlReader;
import microsoft.exchange.webservices.data.security.XmlNodeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Consumer;

public class XmlVersionModifierTest {

  private static final Log LOG = LogFactory.getLog(XmlVersionModifierTest.class);

  private static final String BASE_XML =  "<?xml version=\"%s\" encoding=\"UTF-8\"?>" +
                                "<test>%stestContent</test>";
  private static final String BAD_10 = "&#x5;&#x1A;&#x2B;\\uD800_1_\\uDFFF_2_\\uFFFE";
  private static final String BAD_11 = Character.toString((char)65535);

  private final Map<AbstractMap.SimpleEntry<String, EwsXmlReader.XmlModifier>, AbstractMap.SimpleEntry<Consumer<EwsXmlReader.XmlModifier>, List<Class>>> testCases = new HashMap<AbstractMap.SimpleEntry<String, EwsXmlReader.XmlModifier>, AbstractMap.SimpleEntry<Consumer<EwsXmlReader.XmlModifier>, List<Class>>>()
  {{
      put(new SimpleEntry<>("Test 1, xml 10, friendly, NONE", EwsXmlReader.XmlModifier.NONE),
          new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(true, false), modifier), Arrays.asList()));
    put(new SimpleEntry<>("Test 1, xml 11, friendly, NONE", EwsXmlReader.XmlModifier.NONE),
        new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(false, false), modifier), Arrays.asList()));
    put(new SimpleEntry<>("Test 1, xml 10, bad, NONE", EwsXmlReader.XmlModifier.NONE),
        new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(true, true), modifier), Arrays.asList(XMLStreamException.class)));
    put(new SimpleEntry<>("Test 1, xml 11, bad, NONE", EwsXmlReader.XmlModifier.NONE),
        new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(false, true), modifier), Arrays.asList(XMLStreamException.class)));

    put(new SimpleEntry<>("Test 2, xml 10, friendly, VERSION", EwsXmlReader.XmlModifier.VERSION),
        new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(true, false), modifier), Arrays.asList()));
    put(new SimpleEntry<>("Test 2, xml 11, friendly, VERSION", EwsXmlReader.XmlModifier.VERSION),
        new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(false, false), modifier), Arrays.asList()));
    put(new SimpleEntry<>("Test 2, xml 10, bad, VERSION", EwsXmlReader.XmlModifier.VERSION),
        new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(true, true), modifier), Arrays.asList()));
    put(new SimpleEntry<>("Test 2, xml 11, bad, VERSION", EwsXmlReader.XmlModifier.VERSION),
        new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(false, true), modifier), Arrays.asList(XMLStreamException.class)));

    put(new SimpleEntry<>("Test 3, xml 10, friendly, REPLACER", EwsXmlReader.XmlModifier.REPLACER),
        new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(true, false), modifier), Arrays.asList()));
    put(new SimpleEntry<>("Test 3, xml 11, friendly, REPLACER", EwsXmlReader.XmlModifier.REPLACER),
        new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(false, false), modifier), Arrays.asList()));
    put(new SimpleEntry<>("Test 3, xml 10, bad, REPLACER", EwsXmlReader.XmlModifier.REPLACER),
        new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(true, true), modifier), Arrays.asList(XMLStreamException.class)));
    put(new SimpleEntry<>("Test 3, xml 11, bad, REPLACER", EwsXmlReader.XmlModifier.REPLACER),
        new SimpleEntry<>((modifier) -> generateTestReader(transformXmlBase(false, true), modifier), Arrays.asList()));
  }};

  private static void generateTestReader(byte[] content, EwsXmlReader.XmlModifier modifier)
  {
    try
    {
      currentReader = new EwsXmlReader(new ByteArrayInputStream(content), modifier);
    }
    catch (Exception e)
    {
      currentReader = null;
    }
  }

  private static byte[] transformXmlBase(boolean is10, boolean isBad)
  {
    try
    {
      return String.format(BASE_XML, (is10 ?  "1.0" : "1.1") , (isBad ? (is10 ? BAD_10 : BAD_11) : "")).getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      return String.format(BASE_XML, (is10 ?  "1.0" : "1.1") , (isBad ? (is10 ? BAD_10 : BAD_11) : "")).getBytes();
    }
  }

  private static EwsXmlReader currentReader;
  private static List<Class> currentClasses;
  
  public void assertResult()
  {
    try
    {
      String content = currentReader.readValue();
      LOG.warn("Result : " +  content);
      if(currentClasses.size() == 0)
      {
        boolean success = content.endsWith("testContent");
        Assert.assertTrue(success);
      }
    }
    catch (Exception e)
    {
      Assert.assertTrue(currentClasses.stream().anyMatch(clazz -> clazz.isAssignableFrom(e.getClass()) || e.getClass().isAssignableFrom(clazz)));
    }
  }

  @Test
  public void testCases() throws Exception
  {
    for (Map.Entry<AbstractMap.SimpleEntry<String, EwsXmlReader.XmlModifier>, AbstractMap.SimpleEntry<Consumer<EwsXmlReader.XmlModifier>, List<Class>>> testCase : testCases.entrySet())
    {
      LOG.warn("Start : " +  testCase.getKey().getKey());
      testCase.getValue().getKey().accept(testCase.getKey().getValue());
      currentReader.read(new XmlNodeType(XmlNodeType.START_DOCUMENT));
      currentReader.read(new XmlNodeType(XmlNodeType.START_ELEMENT));
      currentClasses = testCase.getValue().getValue();
      assertResult();
    }
  }
}
