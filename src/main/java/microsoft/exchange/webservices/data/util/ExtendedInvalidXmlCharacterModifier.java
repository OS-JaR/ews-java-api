package microsoft.exchange.webservices.data.util;

import com.github.rwitzel.streamflyer.core.AfterModification;
import com.github.rwitzel.streamflyer.core.Modifier;
import com.github.rwitzel.streamflyer.internal.thirdparty.ZzzValidate;
import com.github.rwitzel.streamflyer.util.ModificationFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by reuter on 22.05.2017.
 */
public class ExtendedInvalidXmlCharacterModifier implements Modifier
{
    public static final String XML_10_VERSION = "1.0";
    public static final String XML_11_VERSION = "1.1";
    protected ModificationFactory factory;
    protected String replacement;
    protected Matcher matcher;
    protected boolean dollarZero;

    public ExtendedInvalidXmlCharacterModifier(String replacement, String xmlVersion) {
        this(8192, replacement, xmlVersion, replacement.contains("$0"));
    }

    public ExtendedInvalidXmlCharacterModifier(int newNumberOfChars, String replacement, String xmlVersion, boolean dollarZero) {
        ZzzValidate.notNull(replacement, "replacement must not be null");
        ZzzValidate.notNull(xmlVersion, "xmlVersion must not be null");
        this.factory = new ModificationFactory(0, newNumberOfChars);
        this.replacement = replacement;
        this.dollarZero = dollarZero;
        Pattern pattern;
        if("1.0".equals(xmlVersion)) {
            pattern = Pattern.compile(this.getInvalidXmlCharacterRegex_Xml10());
        } else {
            if(!"1.1".equals(xmlVersion)) {
                throw new IllegalArgumentException("xmlVersion has the illegal (or unsupported) value " + xmlVersion);
            }

            pattern = Pattern.compile(this.getInvalidXmlCharacterRegex_Xml11());
        }

        this.matcher = pattern.matcher("");
    }

    protected String getInvalidXmlCharacterRegex_Xml10() {
        return "[^\\u0020-\\uD7FF\\u0009\\u000A\\u000D\\uE000-\\uFFFD\\u10000-\\u10FFFF]";
    }

    protected String getInvalidXmlCharacterRegex_Xml11() {
        return "[^\\u0001-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]";
    }

    public AfterModification modify(StringBuilder characterBuffer, int firstModifiableCharacterInBuffer, boolean endOfStreamHit) {
        this.matcher.reset(characterBuffer);
        this.matcher.region(firstModifiableCharacterInBuffer, characterBuffer.length());

        for(int start = firstModifiableCharacterInBuffer; this.matcher.find(start); start = this.onMatch(characterBuffer)) {
            ;
        }

        return this.factory.skipEntireBuffer(characterBuffer, firstModifiableCharacterInBuffer, endOfStreamHit);
    }

    protected int onMatch(StringBuilder characterBuffer) {
        String replacement_ = this.replacement(characterBuffer);
        characterBuffer.replace(this.matcher.start(), this.matcher.end(), replacement_);
        return this.matcher.start() + replacement_.length();
    }

    protected String replacement(StringBuilder characterBuffer) {
        if(!this.dollarZero) {
            return this.replacement;
        } else {
            char ch = characterBuffer.charAt(this.matcher.start());

            String chHex;
            for(chHex = Integer.toString(ch, 16).toUpperCase(); chHex.length() < 4; chHex = "0" + chHex) {
                ;
            }

            chHex = "U+" + chHex;
            return this.replacement.replace("$0", chHex);
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InvalidXmlCharacterModifier [\nreplacement=");
        builder.append(this.replacement);
        builder.append(", \nmatcher=");
        builder.append(this.matcher);
        builder.append(", \ndollarZero=");
        builder.append(this.dollarZero);
        builder.append("]");
        return builder.toString();
    }
}
