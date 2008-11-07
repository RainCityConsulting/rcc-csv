package com.rcc.csv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Iterator;

/**
 * This class is used to read a delimited file with a header. The header is used as the
 * keys in the Maps returned by next().
 */
public class MappedDelimitedReader implements Iterator {
    private static final Log log = LogFactory.getLog(MappedDelimitedReader.class);

    private DelimitedReader delimitedReader;
    private String[] header;

    public MappedDelimitedReader(DelimitedReader delimitedReader)
        throws IOException, ParseException
    {
        this.delimitedReader = delimitedReader;

        if (!delimitedReader.hasNext()) {
            throw new IllegalStateException("No header");
        }

        this.header = delimitedReader.next();
    }

    public String[] getHeader() {
        return this.header;
    }

    public Set getKeys() {
        return new HashSet(Arrays.asList(this.header));
    }

    public int getLineNumber() {
        return delimitedReader.getLineNumber();
    }

    public char getDelimiter() {
        return delimitedReader.getDelimiter();
    }

    public boolean hasNext() {
        try {
            return delimitedReader.hasNext();
        } catch (IOException e) {
            throw new RuntimeException(
                    "IOException caught at line " + this.getLineNumber(), e);
        }
    }

    public Object next() {
        try {
            return createMap(delimitedReader.next());
        } catch (IOException e) {
            throw new RuntimeException(
                    "IOException caught at line " + this.getLineNumber(), e);
        } catch (ParseException e) {
            throw new RuntimeException(
                    "ParseException caught at line " + this.getLineNumber(), e);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private Map createMap(String[] splitLine) throws ParseException {
        if (splitLine.length != this.header.length) {
            throw new ParseException("Line does not have the same number of columns ("
                    + splitLine.length + ") as the header (" + this.header.length + ")", 0);
        }
        
        Map map = new HashMap();
        for (int i = 0; i < this.header.length; i++) {
            map.put(this.header[i], splitLine[i]);
        }

        return map;
    }

    public void close() throws IOException {
        this.delimitedReader.close();
    }
}
