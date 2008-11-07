package com.rcc.csv;

import java.text.ParseException;
import java.util.*;

public class CSV {
    public static final String NULL_VALUE = "NULL";

    public static final boolean DEFAULT_MAP_NULLS = false;
    public static final char DEFAULT_SEPARATOR = ',';
    public static final char DEFAULT_QUOTE = '"';

    static private final int EOL = -1;

    private boolean mapNulls;
    private char separator;
    private char quote;

    public CSV() {
        this( DEFAULT_MAP_NULLS );
    }

    public CSV( char separator ) {
        this( DEFAULT_MAP_NULLS, separator );
    }

    public CSV( boolean mapNulls ) {
        this( mapNulls, DEFAULT_SEPARATOR );
    }

    public CSV(boolean mapNulls, char separator) {
        this( mapNulls, separator, DEFAULT_QUOTE );
    }

    public CSV( boolean mapNulls, char separator, char quote ) {
        if ( separator == quote ) {
            throw new IllegalArgumentException( "separator can not equal quote" );
        }

        this.mapNulls = mapNulls;
        this.separator = separator;
        this.quote = quote;
    }

    public List decode(String csvStr) throws ParseException {
        ArrayList list = new ArrayList();
        decode(csvStr, list);
        list.trimToSize();
        return list;
    }

    public void decode( String csvStr, List list ) throws ParseException {
        //States
        final int NEXT_TOKEN = 0;
        final int UNQUOTED_TOKEN = 1;
        final int QUOTED_TOKEN = 2;
        final int GOT_TOKEN = 3;
        final int POSSIBLE_END_OF_QUOTED_TOKEN = 4;

        int state = NEXT_TOKEN;
        int index = 0;
        StringBuffer token = null;
        int c = EOL;
        boolean done = false;

        while ( ! done ) {
            switch( state ) {
                case NEXT_TOKEN:
                    c = getNextChar( csvStr, index++ );
                    token = new StringBuffer();
                    if ( c == quote ) {
                        state = QUOTED_TOKEN;
                    } else if ( ( c == separator ) || ( c == EOL ) ) {
                        state = GOT_TOKEN;
                    } else {
                        state = UNQUOTED_TOKEN;
                    }
                    break;

                case UNQUOTED_TOKEN:
                    token.append( ( char ) c );
                    c = getNextChar( csvStr, index++ );
                    if ( ( c == EOL ) || ( c == separator ) ) {
                        if ( mapNulls && NULL_VALUE.equals(token.toString() ) ) {
                            token = null;
                        }
                        state = GOT_TOKEN;
                    }
                    break;

                case GOT_TOKEN:
                    if ( token == null ) {
                        list.add( null );
                    } else {
                        list.add( token.toString() );
                    }

                    state = NEXT_TOKEN;
                    if ( c == EOL ) {
                        done = true;
                    }
                    break;

                case QUOTED_TOKEN:
                    c = getNextChar( csvStr, index++ );
                    if ( c == EOL ) {
                        throw new ParseException("Was expecting ending '" + quote + "'", index - 1);
                    } else if ( c == quote ) {
                        state = POSSIBLE_END_OF_QUOTED_TOKEN;
                    } else {
                        token.append((char)c);
                    }
                    break;

                case POSSIBLE_END_OF_QUOTED_TOKEN:
                    int savChar = c;
                    c = getNextChar( csvStr, index++ );
                    if ( ( c == EOL ) || ( c == separator ) ) {
                        state = GOT_TOKEN;
                    }
                    if ( c == quote ) {
                        token.append( ( char ) savChar );
                        state = QUOTED_TOKEN;
                    }
                    break;
            }
        }
    }

    public String encode(Iterator values) {
        StringBuffer sb = new StringBuffer();
        encode( values, sb );
        return sb.toString();
    }

    public void encode(Iterator values, StringBuffer sb) {
        if ( ( values == null ) || ! values.hasNext() ) {
            return;
        }

        encodeElement( values.next(), sb );

        while ( values.hasNext() ) {
            sb.append( separator );
            encodeElement( values.next(), sb );
        }
    }

    public String encodeElement( Object o ) {
        StringBuffer sb = new StringBuffer();
        encodeElement( o, sb );
        return sb.toString();
    }

    public void encodeElement( Object o, StringBuffer encodeBuf ) {
        if ( o == null ) {
            if ( mapNulls ) {
                encodeBuf.append( NULL_VALUE );
            }

            return;
        }

        String s = o.toString();
        int end = s.length();
        if ( end < 1 ) {
            return;
        }

        boolean needsToBeQuoted = false;
        int bufStart = encodeBuf.length();

        for (int i = 0; i < end; i++) {
            char c = s.charAt(i);
            if (c == separator)
                needsToBeQuoted = true;
            else if (c == quote) {
                needsToBeQuoted = true;
                encodeBuf.append(c);
            }

            encodeBuf.append(c);
        }

        // 'NULL' also needs to be quoted if mapNulls is true
        if (needsToBeQuoted || (mapNulls && NULL_VALUE.equals(s))) {
            encodeBuf.insert(bufStart, quote);
            encodeBuf.append(quote);
        }
    }

    private int getNextChar(String csvStr, int index) {
        if (index >= csvStr.length())
           return EOL;

        return csvStr.charAt(index);
    }

    public char getQuoteChar() { return quote; }

    public char getSeparatorChar() { return separator; }

    public String toString() {
        final char SEP = ',';
        return "[" + mapNulls + SEP + separator + SEP + quote + "]";
    }
}
