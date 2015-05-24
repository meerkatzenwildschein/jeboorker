package org.rr.commons.utils;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTMLEntityConverter implements Serializable {

	private static final long serialVersionUID = 5138995692645522618L;
	
	private String text;
    private int encodeCondition;
    private boolean useNamedEntities = false;
    private boolean reencodeEntities = false;

    /**
     * This constructor provides the minimum of parameters needed.
     * @param text The text to be encoded / decoded.
     * @param encodeCondition The encode conditions <code>ENCODE_SEVEN_BIT_ASCII, ENCODE_EIGHT_BIT_ASCII, ENCODE_SEVEN_BIT_XML</code>.
     */
    public HTMLEntityConverter(String text, int encodeCondition) {
        this.text = text;
        this.encodeCondition = encodeCondition;
    }

    /**
     * The target/encoded string contains only seven bit ascii characters. All other characters gets entity encoded.
     */
    public static final int ENCODE_SEVEN_BIT_ASCII = 0;
    
    /**
     * The target/encoded string contains only eight bit ascii characters. All other characters gets entity encoded.
     */
    public static final int ENCODE_EIGHT_BIT_ASCII = 1;
    
    /**
     * The target/encoded string contains only seven bit ascii characters. Some special characters, needed for the xml structure will also be encoded.
     */
    public static final int ENCODE_SEVEN_BIT_XML = 2;
    
    /**
     * Contains all characters in the ascii seven area which should not appear in a xml file
     */
    private static final LinkedList<Character> invalidAscii7XMLCharacters = new LinkedList<Character>() {

		private static final long serialVersionUID = 8718029412734859537L;

		{
            add(Character.valueOf('='));
            add(Character.valueOf('<'));
            add(Character.valueOf('>'));
            add(Character.valueOf('\"'));
            add(Character.valueOf('\'')); // #39
            add(Character.valueOf('`')); // #96
            add(Character.valueOf('^')); // #94
            add(Character.valueOf('&')); // #38
        }
    };
    /**
     * Contains most common HTML4 entities as key with the referring unicode character
     */
    private static final HashMap<String, Character> htmlEntities = new HashMap<String, Character>() {

        private static final long serialVersionUID = -3493475860475518475L;

        @Override
        public Character put(String key, Character value) {
            if (!key.startsWith("&") || !key.endsWith(";")) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Bad Key in init htmlEntities '" + (key) + "'");
            }
            return super.put(key, value);
        }

        {
            // HTML Symbols
            put("&quot;", Character.valueOf('\"')); // Anf&uuml;hrungszeichen oben
            put("&amp;", Character.valueOf('\u0026')); // Ampersand-Zeichen, kaufm&auml;nnisches Und
            put("&lt;", Character.valueOf('\u003c')); // &ouml;ffnende spitze Klammer
            put("&gt;", Character.valueOf('\u003e')); // schliessende spitze Klammer

            // Diakretic Smybols
            put("&circ;", Character.valueOf('\u005e')); // Zirkumflex
            put("&tilde;", Character.valueOf('\u007e')); // Tilde

            // ISO 8859-1 Symbols
            put("&apos;", Character.valueOf('\'')); // ''' xml problematic character
            put("&nbsp;", Character.valueOf('\u00a0')); // Erzwungenes Leerzeichen
            put("&iexcl;", Character.valueOf('\u00a1')); // umgekehrtes Ausrufezeichen
            put("&cent;", Character.valueOf('\u00a2')); // Cent-Zeichen
            put("&pound;", Character.valueOf('\u00a3')); // Pfund-Zeichen
            put("&curren;", Character.valueOf('\u00a4')); // W&auml;hrungszeichen
            put("&yen;", Character.valueOf('\u00a5')); // Yen-Zeichen
            put("&brvbar;", Character.valueOf('\u00a6')); // durchbrochener Strich
            put("&sect;", Character.valueOf('\u00a7')); // Paragraph-Zeichen
            put("&uml;", Character.valueOf('\u00a8')); // P&uuml;nktchen oben
            put("&copy;", Character.valueOf('\u00a9')); // Copyright-Zeichen
            put("&ordf;", Character.valueOf('\u00aa')); // Ordinal-Zeichen weiblich
            put("&laquo;", Character.valueOf('\u00ab')); // angewinkelte Anf&uuml;hrungszeichen links
            put("&not;", Character.valueOf('\u00ac')); // Verneinungs-Zeichen
            put("&shy;", Character.valueOf('\u00ad')); // bedingter Trennstrich
            put("&reg;", Character.valueOf('\u00ae')); // Registriermarke-Zeichen
            put("&macr;", Character.valueOf('\u00af')); // &Uuml;berstrich
            put("&deg;", Character.valueOf('\u00b0')); // Grad-Zeichen
            put("&plusmn;", Character.valueOf('\u00b1')); // Plusminus-Zeichen
            put("&sup2;", Character.valueOf('\u00b2')); // Hoch-2-Zeichen
            put("&sup3;", Character.valueOf('\u00b3')); // Hoch-3-Zeichen
            put("&acute;", Character.valueOf('\u00b4')); // Akut-Zeichen
            put("&micro;", Character.valueOf('\u00b5')); // Mikro-Zeichen
            put("&para;", Character.valueOf('\u00b6')); // Absatz-Zeichen
            put("&middot;", Character.valueOf('\u00b7')); // Mittelpunkt
            put("&cedil;", Character.valueOf('\u00b8')); // H&auml;kchen unten
            put("&sup1;", Character.valueOf('\u00b9')); // Hoch-1-Zeichen
            put("&ordm;", Character.valueOf('\u00ba')); // Ordinal-Zeichen m&auml;nnlich
            put("&raquo;", Character.valueOf('\u00bb')); // angewinkelte Anf&uuml;hrungszeichen rechts
            put("&frac14;", Character.valueOf('\u00bc')); // ein Viertel
            put("&frac12;", Character.valueOf('\u00bd')); // ein Halb
            put("&frac34;", Character.valueOf('\u00be')); // drei Viertel
            put("&iquest;", Character.valueOf('\u00bf')); // umgekehrtes Fragezeichen
            put("&Agrave;", Character.valueOf('\u00c0')); // A mit accent grave (Gravis)
            put("&Aacute;", Character.valueOf('\u00c1')); // A mit accent aigu (Akut)
            put("&Acirc;", Character.valueOf('\u00c2')); // A mit Zirkumflex
            put("&Atilde;", Character.valueOf('\u00c3')); // A mit Tilde
            put("&Auml;", Character.valueOf('\u00c4')); // A Umlaut
            put("&Aring;", Character.valueOf('\u00c5')); // A mit Ring
            put("&AElig;", Character.valueOf('\u00c6')); // A mit legiertem E
            put("&Ccedil;", Character.valueOf('\u00c7')); // C mit H&auml;kchen
            put("&Egrave;", Character.valueOf('\u00c8')); // E mit accent grave (Gravis)
            put("&Eacute;", Character.valueOf('\u00c9')); // E mit accent aigu (Akut)
            put("&Ecirc;", Character.valueOf('\u00ca')); // E mit Zirkumflex
            put("&Euml;", Character.valueOf('\u00cb')); // E Umlaut
            put("&Igrave;", Character.valueOf('\u00cc')); // I mit accent grave (Gravis)
            put("&Iacute;", Character.valueOf('\u00cd')); // I mit accent aigu (Akut)
            put("&Icirc;", Character.valueOf('\u00ce')); // I mit Zirkumflex
            put("&Iuml;", Character.valueOf('\u00cf')); // I Umlaut
            put("&ETH;", Character.valueOf('\u00d0')); // grosses Eth (isl&auml;ndisch)
            put("&Ntilde;", Character.valueOf('\u00d1')); // N mit Tilde
            put("&Ograve;", Character.valueOf('\u00d2')); // O mit accent grave (Gravis)
            put("&Oacute;", Character.valueOf('\u00d3')); // O mit accent aigu (Akut)
            put("&Ocirc;", Character.valueOf('\u00d4')); // O mit Zirkumflex
            put("&Otilde;", Character.valueOf('\u00d5')); // O mit Tilde
            put("&Ouml;", Character.valueOf('\u00d6')); // O Umlaut
            put("&times;", Character.valueOf('\u00d7')); // Mal-Zeichen
            put("&Oslash;", Character.valueOf('\u00d8')); // O mit Schr&auml;gstrich
            put("&Ugrave;", Character.valueOf('\u00d9')); // U mit accent grave (Gravis)
            put("&Uacute;", Character.valueOf('\u00da')); // U mit accent aigu (Akut)
            put("&Ucirc;", Character.valueOf('\u00db')); // U mit Zirkumflex
            put("&Uuml;", Character.valueOf('\u00dc')); // U Umlaut
            put("&THORN;", Character.valueOf('\u00de')); // grosses Thorn (isl&auml;ndisch)
            put("&szlig;", Character.valueOf('\u00df')); // scharfes S
            put("&agrave;", Character.valueOf('\u00e0')); // a mit accent grave (Gravis)
            put("&aacute;", Character.valueOf('\u00e1')); // a mit accent aigu (Akut)
            put("&acirc;", Character.valueOf('\u00e2')); // a mit Zirkumflex
            put("&atilde;", Character.valueOf('\u00e3')); // a mit Tilde
            put("&auml;", Character.valueOf('\u00e4')); // a Umlaut
            put("&aring;", Character.valueOf('\u00e5')); // a mit Ring
            put("&aelig;", Character.valueOf('\u00e6')); // a mit legiertem e
            put("&ccedil;", Character.valueOf('\u00e7')); // c mit H&auml;kchen
            put("&egrave;", Character.valueOf('\u00e8')); // e mit accent grave (Gravis)
            put("&eacute;", Character.valueOf('\u00e9')); // e mit accent aigu (Akut)
            put("&ecirc;", Character.valueOf('\u00ea')); // e mit Zirkumflex
            put("&euml;", Character.valueOf('\u00eb')); // e Umlaut
            put("&igrave;", Character.valueOf('\u00ec')); // i mit accent grave (Gravis)
            put("&iacute;", Character.valueOf('\u00ed')); // i mit accent aigu (Akut)
            put("&icirc;", Character.valueOf('\u00ee')); // i mit Zirkumflex
            put("&iuml;", Character.valueOf('\u00ef')); // i Umlaut
            put("&eth;", Character.valueOf('\u00f0')); // kleines Eth (isl&auml;ndisch)
            put("&ntilde;", Character.valueOf('\u00f1')); // n mit Tilde
            put("&ograve;", Character.valueOf('\u00f2')); // o mit accent grave (Gravis)
            put("&oacute;", Character.valueOf('\u00f3')); // o mit accent aigu (Akut)
            put("&ocirc;", Character.valueOf('\u00f4')); // o mit Zirkumflex
            put("&otilde;", Character.valueOf('\u00f5')); // o mit Tilde
            put("&ouml;", Character.valueOf('\u00f6')); // o Umlaut
            put("&divide;", Character.valueOf('\u00f7')); // Divisions-Zeichen
            put("&oslash;", Character.valueOf('\u00f8')); // o mit Schr&auml;gstrich
            put("&ugrave;", Character.valueOf('\u00f9')); // u mit accent grave (Gravis)
            put("&uacute;", Character.valueOf('\u00fa')); // u mit accent aigu (Akut)
            put("&ucirc;", Character.valueOf('\u00fb')); // u mit Zirkumflex
            put("&uuml;", Character.valueOf('\u00fc')); // u Umlaut
            put("&yacute;", Character.valueOf('\u00fd')); // y mit accent aigu (Akut)
            put("&Yacute;", Character.valueOf('\u00dd')); // Y mit accent aigu (Akut)
            put("&thorn;", Character.valueOf('\u00fe')); // kleines Thorn (isl&auml;ndisch)
            put("&yuml;", Character.valueOf('\u00ff')); // y Umlaut
            put("&Yuml;", Character.valueOf('\u0178')); // Y Umlaut

            // Greek Symbols
            put("&Alpha;", Character.valueOf('\u0391')); // Alpha gross
            put("&alpha;", Character.valueOf('\u03B1')); // alpha klein
            put("&Beta;", Character.valueOf('\u0392')); // Beta gross
            put("&beta;", Character.valueOf('\u03B2')); // Beta klein
            put("&Gamma;", Character.valueOf('\u0393')); // Gamma gross
            put("&gamma;", Character.valueOf('\u03B3')); // Gamme klein
            put("&Delta;", Character.valueOf('\u0394')); // Delta gross
            put("&delta;", Character.valueOf('\u03B4')); // Delta klein
            put("&Epsilon;", Character.valueOf('\u0395')); // Epsilon gross
            put("&epsilon;", Character.valueOf('\u03B5')); // Epsilon klein
            put("&Zeta;", Character.valueOf('\u0396')); // Epsilon gross
            put("&zeta;", Character.valueOf('\u03B6')); // Epsilon klein
            put("&Eta;", Character.valueOf('\u0397')); // Eta gross
            put("&eta;", Character.valueOf('\u03B7')); // Eta klein
            put("&Theta;", Character.valueOf('\u0398')); // Theta gross
            put("&theta;", Character.valueOf('\u03B8')); // Theta klein
            put("&Iota;", Character.valueOf('\u0399')); // Iota gross
            put("&iota;", Character.valueOf('\u03B9')); // Iota klein
            put("&Kappa;", Character.valueOf('\u039A')); // Kappa gross
            put("&kappa;", Character.valueOf('\u03BA')); // Kappa klein
            put("&Lambda;", Character.valueOf('\u039B')); // Lambda gross
            put("&lambda;", Character.valueOf('\u03BB')); // Lambda klein
            put("&Mu;", Character.valueOf('\u039C')); // Mu gross
            put("&mu;", Character.valueOf('\u03BC')); // Mu klein
            put("&Nu;", Character.valueOf('\u039D')); // Nu gross
            put("&nu;", Character.valueOf('\u03BD')); // Nu klein
            put("&Xi;", Character.valueOf('\u039E')); // Xi gross
            put("&xi;", Character.valueOf('\u03BE')); // Xi klein
            put("&Omicron;", Character.valueOf('\u039F')); // Omicron gross
            put("&omicron;", Character.valueOf('\u03BF')); // Omicron klein
            put("&Pi;", Character.valueOf('\u03A0')); // Pi gross
            put("&pi;", Character.valueOf('\u03C0')); // Pi klein
            put("&Rho;", Character.valueOf('\u03A1')); // Rho gross
            put("&rho;", Character.valueOf('\u03C1')); // Rho klein
            put("&sigmaf;", Character.valueOf('\u03C2')); // Schluss-Sigma
            put("&Sigma;", Character.valueOf('\u03A3')); // Sigma gross
            put("&sigma;", Character.valueOf('\u03C3')); // Sigma klein
            put("&Tau;", Character.valueOf('\u03A4')); // Tau gross
            put("&tau;", Character.valueOf('\u03C4')); // Tau klein
            put("&Upsilon;", Character.valueOf('\u03A5')); // Upsilon gross
            put("&upsilon;", Character.valueOf('\u03C5')); // Upsilon klein
            put("&Phi;", Character.valueOf('\u03A6')); // Phi gross
            put("&phi;", Character.valueOf('\u03C6')); // Phi klein
            put("&Chi;", Character.valueOf('\u03A7')); // Chi gross
            put("&chi;", Character.valueOf('\u03C7')); // Chi klein
            put("&Psi;", Character.valueOf('\u03A8')); // Psi gross
            put("&psi;", Character.valueOf('\u03C8')); // Psi klein
            put("&Omega;", Character.valueOf('\u03A9')); // Omega gross
            put("&omega;", Character.valueOf('\u03C9')); // Omega klein
            put("&thetasym;", Character.valueOf('\u03D1'));// theta Symbol
            put("&upsih;", Character.valueOf('\u03D2')); // ypsilon mit Haken
            put("&piv;", Character.valueOf('\u03D6')); // greek pi symbol

            // Mathematical Symbols
            put("&forall;", Character.valueOf('\u2200')); // for all
            put("&part;", Character.valueOf('\u2202')); // partial differential
            put("&exist;", Character.valueOf('\u2203')); // there exists
            put("&empty;", Character.valueOf('\u2205')); // empty set = null set = diameter
            put("&nabla;", Character.valueOf('\u2207')); // nabla = backward difference
            put("&isin;", Character.valueOf('\u2208')); // element of
            put("&notin;", Character.valueOf('\u2209')); // not an element of
            put("&ni;", Character.valueOf('\u220B')); // contains as member
            put("&prod;", Character.valueOf('\u220F')); // n-ary product = product sign
            put("&sum;", Character.valueOf('\u2211')); // n-ary sumation
            put("&minus;", Character.valueOf('\u2212')); // minus sign
            put("&lowast;", Character.valueOf('\u2217')); // asterisk operator
            put("&radic;", Character.valueOf('\u221A')); // square root = radical sign
            put("&prop;", Character.valueOf('\u221D')); // proportional to
            put("&infin;", Character.valueOf('\u221E')); // infinity
            put("&ang;", Character.valueOf('\u2220')); // angle
            put("&and;", Character.valueOf('\u2227')); // logical and
            put("&or;", Character.valueOf('\u2228')); // logical or
            put("&cap;", Character.valueOf('\u2229')); // intersection
            put("&cup;", Character.valueOf('\u222A')); // union
            put("&int;", Character.valueOf('\u222B')); // integral
            put("&there4;", Character.valueOf('\u2234')); // therefore
            put("&sim;", Character.valueOf('\u223C')); // tilde operator = varies with = similar to
            put("&cong;", Character.valueOf('\u2245')); // approximately equal to
            put("&asymp;", Character.valueOf('\u2248')); // almost equal to
            put("&ne;", Character.valueOf('\u2260')); // not equal to
            put("&equiv;", Character.valueOf('\u2261')); // identical to
            put("&le;", Character.valueOf('\u2264')); // less-than or equal to
            put("&ge;", Character.valueOf('\u2265')); // greater-than or equal to
            put("&sub;", Character.valueOf('\u2282')); // subset of
            put("&sup;", Character.valueOf('\u2283')); // superset of
            put("&nsub;", Character.valueOf('\u2284')); // not a subset of
            put("&sube;", Character.valueOf('\u2286')); // subset of or equal to
            put("&supe;", Character.valueOf('\u2287')); // superset of or equal to
            put("&oplus;", Character.valueOf('\u2295')); // circled plus = direct sum
            put("&otimes;", Character.valueOf('\u2297')); // circled times = vector product
            put("&perp;", Character.valueOf('\u22A5')); // up tack = orthogonal to = perpendicular
            put("&sdot;", Character.valueOf('\u22C5')); // dot operator
            put("&loz;", Character.valueOf('\u25CA')); // lozenge
            put("&fnof;", Character.valueOf('\u0192')); // latin small f with hook = function = florin

            // General Punctuation
            put("&bull;", Character.valueOf('\u2022')); // bullet = black small circle
            put("&hellip;", Character.valueOf('\u2026')); // horizontal ellipsis = three dot leader
            put("&prime;", Character.valueOf('\u2032')); // prime = minutes = feet
            put("&Prime;", Character.valueOf('\u2033')); // double prime = seconds = inches
            put("&oline;", Character.valueOf('\u203E')); // overline = spacing overscore
            put("&frasl;", Character.valueOf('\u2044')); // fraction slash

            // Letterlike Symbols
            put("&weierp;", Character.valueOf('\u2118')); // script capital P = power set = Weierstrass p
            put("&image;", Character.valueOf('\u2111')); // blackletter capital I = imaginary part
            put("&real;", Character.valueOf('\u211C')); // blackletter capital R = real part symbol
            put("&trade;", Character.valueOf('\u2122')); // trade mark sign
            put("&alefsym;", Character.valueOf('\u2135')); // alef symbol = first transfinite cardinal
            put("&euro;", Character.valueOf('\u20ac')); // euro currency

            // Arrow Symbols
            put("&larr;", Character.valueOf('\u2190'));
            put("&uarr;", Character.valueOf('\u2191'));
            put("&rarr;", Character.valueOf('\u2192'));
            put("&darr;", Character.valueOf('\u2193'));
            put("&harr;", Character.valueOf('\u2194'));
            put("&crarr;", Character.valueOf('\u21b5'));
            put("&lArr;", Character.valueOf('\u21d0'));
            put("&uArr;", Character.valueOf('\u21d1'));
            put("&rArr;", Character.valueOf('\u21d2'));
            put("&dArr;", Character.valueOf('\u21d3'));
            put("&hArr;", Character.valueOf('\u21d4'));

            // Miscellaneous Symbols
            put("&spades;", Character.valueOf('\u2660')); // black spade suit
            put("&clubs;", Character.valueOf('\u2663')); // black club suit = shamrock
            put("&hearts;", Character.valueOf('\u2665')); // black heart suit = valentine
            put("&diams;", Character.valueOf('\u2666')); // black diamond suit

            // Miscellaneous Technical
            put("&lceil;", Character.valueOf('\u2308')); // left ceiling = apl upstile
            put("&rceil;", Character.valueOf('\u2309')); // right ceiling
            put("&lfloor;", Character.valueOf('\u230A')); // left floor = apl downstile
            put("&rfloor;", Character.valueOf('\u230B')); // right floor
            put("&lang;", Character.valueOf('\u2329')); // left-pointing angle bracket = bra
            put("&rang;", Character.valueOf('\u232A')); // right-pointing angle bracket = ket

            // Benannte Zeichen lateinisch erweitert
            put("&Oelig;", Character.valueOf('\u0152')); // OE-Ligatur
            put("&oelig;", Character.valueOf('\u0153')); // OE-Ligatur klein
            put("&Scaron;", Character.valueOf('\u0160')); // S mit Hatschek (Caron)
            put("&scaron;", Character.valueOf('\u0161')); // s mit Hatschek (Caron)

            // Benannte Zeichen f&uuml;r Interpunktion
            put("&lsquo;", Character.valueOf('\u8216'));
            put("&rsquo;", Character.valueOf('\u8217'));
            put("&ldquo;", Character.valueOf('\u8220'));
            put("&rdquo;", Character.valueOf('\u8221'));
            put("&ensp;", Character.valueOf('\u2002'));
            put("&emsp;", Character.valueOf('\u2003'));
            put("&thinsp;", Character.valueOf('\u2009'));
            put("&zwnj;", Character.valueOf('\u200C'));
            put("&zwj;", Character.valueOf('\u200D'));
            put("&lrm;", Character.valueOf('\u200E'));
            put("&rlm;", Character.valueOf('\u200F'));
            put("&ndash;", Character.valueOf('\u2013'));
            put("&mdash;", Character.valueOf('\u2014'));
            put("&sbquo;", Character.valueOf('\u201A'));
            put("&bdquo;", Character.valueOf('\u201E'));
            put("&dagger;", Character.valueOf('\u2020'));
            put("&Dagger;", Character.valueOf('\u2021'));
            put("&permil;", Character.valueOf('\u2030'));
            put("&lsaquo;", Character.valueOf('\u2039'));
            put("&rsaquo;", Character.valueOf('\u203A'));

        }
    };
    /**
     * Contains all characters from the {@link #htmlEntities} Map but the HTML Entity is the value and the unicode char is the key.
     */
    private static final HashMap<Character, String> reverseHtmlEntities = new HashMap<Character, String>() {

        private static final long serialVersionUID = 94497908219310662L;

        {
            for (Map.Entry<String, Character> entry : htmlEntities.entrySet()) {
                String key = entry.getKey();
                Character value = entry.getValue(); // the unicode char

                put(value, key);
            }
        }
    };

    /**
     * Determines the character for a given HTML entity. If the HTML entity, specified with the <code>entity</code> parameter, is not found, the given
     * {@link String} is returned. <br>
     * <br>
     * The HTML entity {@link String} can be somtehing like "&nbsp;" but it's not wickedly if the '&' or ';' character is missing. These characters can be
     * omitted.
     *
     * @param entity
     *            The entity to be converted into the associated character.
     * @return The associated character or, if no associated character is found, <code>null</code> is returned.
     */
    private static Character getHTMLEntityCharacter(final String entity) {
        if (entity == null || entity.length() == 0) {
            return null;
        }
        try {
            // start some normalization
            String processEntity = entity.trim();
            if (!processEntity.startsWith("&")) {
                // Append the & char if not presend
                processEntity = "&" + processEntity;
            }

            if (!processEntity.endsWith(";")) {
                // Append the ; char at the end if not present
                processEntity = processEntity + ";";
            }

            final Character fetchedEntity = htmlEntities.get(processEntity);

            return fetchedEntity;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decodes a numeric entity like <code>"&#64;"</code> or <code>"&#x40;"</code> using the <code>decodeUnicode(String)</code> method.
     * <code>decodeDecimalEntity(String)</code> only reformats the given String into an unicode ascii sequence and returns the reuslt from the
     * <code>decodeUnicode(String)</code> method.
     *
     * @param str
     *            The entity to decode.
     * @return The character matching to the given entity.
     * @throws IllegalArgumentException
     *             if the entity could not be decoded.
     */
    private static String decodeNumericEntity(final String str) {
        String hexValue = str;
        try {
            if (hexValue.charAt(2) == 'x') {
                // &#x20ac, its already hex (only cut the &#x and the ; away)
                hexValue = str.substring(3, str.length() - 1);
            } else {
                // &#8264, convert it to a hex value and cut the &# and the ; away.
                String substring = str.substring(2, str.length() - 1);
                hexValue = hex(Integer.valueOf(substring).doubleValue());
            }

            // use the unicode decode method to decode the value.
            return String.valueOf(decodeNumericUnicodeSequence(hexValue));
        } catch (Exception e) {
            throw new RuntimeException("IllegalUnicodeSequence " + str, e);
        }
    }

    /**
     * Creates a string that contains a repeating character of a specified length.
     *
     * @param size
     *            Number of recurrences.
     * @param repeat
     *            The character that should be repeated. The first character of the <code>String</code> will be used.
     * @return A String with the specified number of repeating characters.
     */
    private static final String string(final int size, final char repeat) {
        if (size <= 0) {
            return EMPTY;
        }
        final StringBuilder returnValue = new StringBuilder(size);

        for (int i = 0; i < size; i++) {
            returnValue.append(repeat);
        }
        return returnValue.toString();
    }

    /**
     * Decodes an hexadecimal, numeric value into an UTF-8 String.
     *
     * @param s
     *            The numeric String to be decoded.
     * @return The decoded unicode character.
     * @throws IllegalArgumentException
     */
    private static char decodeNumericUnicodeSequence(final String s) {
        // normalize the given numeric string to a two byte numeric string value (40 -> 0040).
        String toDecode = string(4 - s.length(), '0') + s;

        int value = 0;
        for (int i = 0; i < 4; i++) {
            char aChar = toDecode.charAt(i);
            switch (aChar) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    value = (value << 4) + aChar - '0';
                    break;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    value = (value << 4) + 10 + aChar - 'a';
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    value = (value << 4) + 10 + aChar - 'A';
                    break;
                default:
                    throw new RuntimeException("IllegalUnicodeSequence " + s);
            }
        }
        return (char) value;
    }

    /**
     * Creates a string that represents the hexadecimal value of a specified number.
     *
     * If number is not a whole number, it is rounded to the nearest whole number before being evaluated.
     *
     * @param value
     *            The number to be used for calculation.
     * @return The hex <code>String</code> from the given number.
     */
    private static final String hex(double value) {
        value = round(value, 0);
        return Integer.toHexString((int) value).toUpperCase();
    }

    /**
     * Round a double value to the next closest number considing the specified number of decimal places.
     *
     * @param value
     *            The number to be rounded.
     * @param places
     *            Specifies how many places to the right of the decimal are included in the rounding.
     * @return The rounded value.
     */
    private static final double round(double value, final int places) {
        long factor = (long) Math.pow(10, places);
        // Shift the decimal the correct number of places to the right.
        value = value * factor;
        return (double) Math.round(value) / factor;
    }

    /**
     * Decodes all HTML entities like: &nbsp; &#x20ac or &#62 in the given text and replaces them with the correct String. Did not throws any kind of Exception.
     *
     * @param text
     *            The text to be proecessed.
     * @return The processed <code>String</code>. If the <code>text</code> is <code>null</code>, <code>null</code> will be returned.
     */
    public String decodeEntities() {
        if (text == null) {
            return null;
        }

        try {
            final StringBuilder resultBuf = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '&') {
                    // this can be possibly a HTML entity!
                    String tmp = text.substring(i);
                    final int semikolonIndex = tmp.indexOf(';');
                    if (semikolonIndex != -1) {
                        tmp = tmp.substring(0, semikolonIndex + 1);
                        Character entity = getHTMLEntityCharacter(tmp);

                        // is there an entity found, than it's only one character!
                        if (entity != null) {
                            resultBuf.append(entity);
                            i += tmp.length() - 1; // skip the rest from processing
                            continue;
                        } else {
                            // entity not in list, test for a numeric entity. A numeric entity could not be longer than 8 chars.
                            if (tmp.charAt(1) == '#' && tmp.length() <= 8) {
                                resultBuf.append(decodeNumericEntity(tmp));
                                i += tmp.length() - 1; // skip the rest from processing
                                continue;
                            }
                        }
                    }
                    // append the & character if the previous process did not match.
                    resultBuf.append(text.charAt(i));
                } else {
                    // not escaped, just append it
                    resultBuf.append(text.charAt(i));
                }
            }

            return resultBuf.toString();
        } catch (Exception e) {
            Logger.getLogger(HTMLEntityConverter.class.getName()).log(Level.WARNING,
                    "converting html entity has failed for string " + text + ". The unconverted text is used instead.", e);
        }
        return text;
    }

    /**
     * Exchanges all chars in the given text which are not a member of seven or eight bit ascii or which are problematic for xml processing (<>="' etc.). <BR>
     * <BT> The exchange character is a numeric html entity character. For example for the euro char will be replaced by the numeric html entity &#8364;
     *
     * @param text
     *            The text to be encoded.
     * @param encodeCondition
     *            use {@link #ENCODE_SEVEN_BIT_ASCII} and {@link #ENCODE_EIGHT_BIT_ASCII} for telling to get a string which only contains seven or eight bit
     *            ascii chars.
     * @param useNamedEntities
     *            Tells if the encoding should be done with named entites or not. For example &euro will be used instead of &#8364;
     * @return The encoded text. If the given text is <code>null</code>, <code>null</code> will be returned.
     */
    public String encodeEntities(final boolean useNamedEntities) {
        if (text == null) {
            return null;
        }



        final StringBuilder resultBuf = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            final char textChar = text.charAt(i);

            if (Character.isWhitespace(textChar) || Character.isSpaceChar(textChar)) {
                resultBuf.append(" ");
            } else if (shouldEncodeCharacer(textChar, encodeCondition)) {
                //skip already encoded entities.
                if (textChar == '&' && !isReencodeEntities()) {
                    String entity = isEntity(text, i);
                    if (entity != null) {
                        i += entity.length() - 1;
                        resultBuf.append(entity);
                        continue;
                    }
                }
                String entity = "&#" + (int) textChar + ";";
                if (useNamedEntities) {
                    // take a look if there is a named entity available for the current character.
                    String namedEntity = reverseHtmlEntities.get(Character.valueOf(textChar));
                    if (namedEntity != null) {
                        entity = namedEntity;
                    }
                }
                resultBuf.append(entity);
            } else {
                resultBuf.append(textChar);
            }
        }

        return resultBuf.toString();
    }

    /**
     * Determines if there is a known entity int the given <code>text</code> at
     * the given <code>idx</code> (position).
     *
     * @param text The text to be tested
     * @param idx The index where the entity should be.
     * @return The identified entity or <code>null</code> if the entity isn't onw or
     *   could not be identified.
     */
    private static String isEntity(String text, int idx) {
        if (text.charAt(idx) == '&' && text.length() >= idx + 3) {
            if (text.charAt(idx + 1) == '#') {
                //test if we have a hex entity.
                int hexEntityEnd = -1;
                boolean digitFound = false;
                for (int i = idx + 2; i < text.length(); i++) {
                    if (Character.isDigit(text.charAt(i))) {
                        digitFound = true;
                    } else if (text.charAt(i) == ';' && digitFound) {
                        hexEntityEnd = i;
                        break;
                    } else {
                        break;
                    }
                }
                if (hexEntityEnd != -1) {
                    //return the hex entity.
                    return text.substring(idx, hexEntityEnd + 1);
                }
            } else {
                //test if we have a named entity
                for (Map.Entry<String, Character> entry : htmlEntities.entrySet()) {
                    final String namedEntity = entry.getKey();
                    if (text.startsWith(namedEntity, idx)) {
                        return namedEntity;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Exchanges all chars in the given text which are not a member of seven or eight bit ascii or which are problematic for xml processing (<>="' etc.). <BR>
     * <BT> The exchange character is a numeric html entity character. For example for the euro char will be replaced by the numeric html entity &#8364;
     *
     * @param text
     *            The text to be encoded.
     * @param encodeCondition
     *            use {@link #ENCODE_SEVEN_BIT_ASCII} and {@link #ENCODE_EIGHT_BIT_ASCII} for telling to get a string which only contains seven or eight bit
     *            ascii chars.
     * @return The encoded text. If the given text is <code>null</code>, <code>null</code> will be returned.
     */
    public String encodeEntities() {
        return encodeEntities(this.isUseNamedEntities());
    }

    /**
     * Tells if the given character should be encoded or not.
     *
     * @param c
     *            The character
     * @param encodeCondition
     *            use {@link #ENCODE_SEVEN_BIT_ASCII} and {@link #ENCODE_EIGHT_BIT_ASCII} for telling to get a string which only contains seven or eight bit
     *            ascii chars.
     * @return <code>true</code> if the char should be encoded and <code>false</code> otherwise.
     */
    private static boolean shouldEncodeCharacer(final char c, final int encodeCondition) {
        switch (encodeCondition) {
            case ENCODE_SEVEN_BIT_ASCII:
                return c > 127;
            case ENCODE_EIGHT_BIT_ASCII:
                return c > 255;
            case ENCODE_SEVEN_BIT_XML:
                if (c > 127) {
                    return true;
                } else if (invalidAscii7XMLCharacters.contains(Character.valueOf(c))) {
                    return true;
                } else if (Character.isISOControl(c)) {
                    return true;
                }
            default:
                return false;
        }
    }

    /**
     * @see #setEncodeCondition(int)
     */
    public int getEncodeCondition() {
        return encodeCondition;
    }

    /**
     * Sets the encode condition.
     * @param encodeCondition <code>ENCODE_SEVEN_BIT_ASCII, ENCODE_EIGHT_BIT_ASCII, ENCODE_SEVEN_BIT_XML</code>
     */
    public void setEncodeCondition(int encodeCondition) {
        this.encodeCondition = encodeCondition;
    }

    /**
     * @see #setReencodeEntities(boolean)
     */
    public boolean isReencodeEntities() {
        return reencodeEntities;
    }

    /**
     * If there already entities in the given html, reencode them or do not touch them.
     * @param reencodeEntities <code>true</code> reencode them and <code>false</code> do not touch them.
     */
    public void setReencodeEntities(boolean reencodeEntities) {
        this.reencodeEntities = reencodeEntities;
    }

    /**
     * @see #setText(java.lang.String)
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text to be encoded / decoded.
     * @param text The text to be encoded / decoded.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @see #setUseNamedEntities(boolean)
     */
    public boolean isUseNamedEntities() {
        return useNamedEntities;
    }

    /**
     * Use named entities like &amp; for the encoding process or not.
     * @param useNamedEntities <code>true</code> for using named entities and <code>false</code>
     *   for using the numeric ones like "&#23ac;".
     */
    public void setUseNamedEntities(boolean useNamedEntities) {
        this.useNamedEntities = useNamedEntities;
    }
}
