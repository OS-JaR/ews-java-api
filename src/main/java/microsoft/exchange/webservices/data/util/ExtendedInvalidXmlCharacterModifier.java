package microsoft.exchange.webservices.data.util;

import com.github.rwitzel.streamflyer.core.AfterModification;
import com.github.rwitzel.streamflyer.core.Modifier;
import com.github.rwitzel.streamflyer.internal.thirdparty.ZzzValidate;
import com.github.rwitzel.streamflyer.util.ModificationFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by reuter on 22.05.2017.
 */
public class ExtendedInvalidXmlCharacterModifier implements Modifier
{
    private static final Log LOG = LogFactory.getLog(ExtendedInvalidXmlCharacterModifier.class);

    private enum XmlVersionModifierState
    {
        /**
         * The initial state. No input read yet.
         */
        INITIAL,

        /**
         * The modifier has requested to read the XML prolog.
         */
        PROLOG_REQUEST,

        /**
         * The modifier has read the XML prolog, modified it if necessary.
         * Nothing more to do for the modifier.
         */
        PROLOG_MODIFIED,
        PROLOG_UNMODIFIED,
    }

    protected ModificationFactory factory;

    /**
     * The replacement for each invalid XML character.
     */
    protected String replacement;

    protected int numberOfChars;

    /**
     * This matcherInvalidChar matches invalid XML characters.
     */
    protected Matcher matcherInvalidChar;

    private XmlVersionModifierState state = XmlVersionModifierState.INITIAL;

    /**
     * Like {@link ExtendedInvalidXmlCharacterModifier#ExtendedInvalidXmlCharacterModifier(int, String)} but uses 8192
     * as default for <code>newNumberOfChars</code>
     */
    public ExtendedInvalidXmlCharacterModifier(String replacementInvalidChar) {
        this(8192, replacementInvalidChar);
    }

    /**
     * @param newNumberOfChars
     * @param replacement
     *            the string that shall replace invalid XML characters. This string may contain "$0" which refers to the
     *            replaced character, see {@link Matcher#replaceAll(String)}.
     */
    public ExtendedInvalidXmlCharacterModifier(int newNumberOfChars, String replacement) {

        ZzzValidate.notNull(replacement, "replacement must not be null");

        this.factory = new ModificationFactory(0, newNumberOfChars);
        this.replacement = replacement;
        this.numberOfChars = newNumberOfChars;

        this.matcherInvalidChar = Pattern.compile(getInvalidXmlCharacterRegex_Xml11()).matcher("");
    }

    /**
     * <pre>
     * [2]     Char       ::=      [#x1-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     * // any Unicode character, excluding the surrogate blocks, FFFE, and FFFF.
     * </pre>
     *
     * [Source: http://www.w3.org/TR/xml11/#charsets ]
     *
     * @return Returns a regular expression that matches invalid XML 1.1 characters.
     */
    protected String getInvalidXmlCharacterRegex_Xml11() {
        return "(&#x0;)|[^\\u0001-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]";
    }
    /**
     * @see com.github.rwitzel.streamflyer.core.Modifier#modify(java.lang.StringBuilder, int, boolean)
     */
    @Override
    public AfterModification modify(StringBuilder characterBuffer, int firstModifiableCharacterInBuffer, boolean endOfStreamHit)
    {
        try
        {
            switch (state)
            {
                case PROLOG_MODIFIED:
                case PROLOG_UNMODIFIED:
                {
                    matcherInvalidChar.reset(characterBuffer);
                    matcherInvalidChar.region(firstModifiableCharacterInBuffer, characterBuffer.length());

                    int start = firstModifiableCharacterInBuffer;
                    while (matcherInvalidChar.find(start))
                    {
                        start = onMatch(characterBuffer);
                    }

                    return factory.skipEntireBuffer(characterBuffer, firstModifiableCharacterInBuffer, endOfStreamHit);
                }
                case INITIAL:
                {
                    state = XmlVersionModifierState.PROLOG_REQUEST;
                    return factory.modifyAgainImmediately(numberOfChars, firstModifiableCharacterInBuffer);
                }
                case PROLOG_REQUEST:
                {
                    Matcher matcher = Pattern.compile("<\\?xml\\s+version\\s*=\\s*['\"](1.0|1.1)['\"].*").matcher(characterBuffer);
                    if (matcher.find())
                    {
                        characterBuffer.replace(matcher.start(1), matcher.end(1), "1.1");
                        state = XmlVersionModifierState.PROLOG_MODIFIED;
                        return factory.skip(matcher.end(1), characterBuffer, firstModifiableCharacterInBuffer, endOfStreamHit);
                    }
                    state = XmlVersionModifierState.PROLOG_UNMODIFIED;
                    return factory.modifyAgainImmediately(numberOfChars, firstModifiableCharacterInBuffer);
                }
                default:
                    return factory.skipEntireBuffer(characterBuffer, firstModifiableCharacterInBuffer, endOfStreamHit);
            }
        }
        catch (Exception e)
        {
            return factory.skipEntireBuffer(characterBuffer, firstModifiableCharacterInBuffer, endOfStreamHit);
        }
    }

    /**
     * Replaces the found invalid XML character with the given replacementInvalidChar.
     * <p>
     * You may override this method to insert some information about invalid character in to the character buffer.
     *
     * @param characterBuffer
     */
    protected int onMatch(StringBuilder characterBuffer)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug(String.format("Start replacing %s (%d-%d)", characterBuffer.substring(matcherInvalidChar.start(), matcherInvalidChar.end()), matcherInvalidChar.start(), matcherInvalidChar.end()));
        }
        characterBuffer.replace(matcherInvalidChar.start(), matcherInvalidChar.end(), replacement);
        return matcherInvalidChar.start() + replacement.length();
    }
}
