package com.rcc.csv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.LineNumberReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.List;
import java.text.ParseException;

public class DelimitedReader {
    private static final Log log = LogFactory.getLog( DelimitedReader.class );

    private LineNumberReader lineNumberReader;
    private CSV csv;
    private String curLine;
    private char delimiter;
    private char quote;
    private String raw;
    
    public DelimitedReader( char delimiter, char quote, boolean mapNulls, String filename )
        throws IOException
    {
        this( delimiter, quote, mapNulls, new InputStreamReader( new FileInputStream( filename ),
                System.getProperty( "file.encoding" ) ) );
    }

    public DelimitedReader( char delimiter, char quote, boolean mapNulls, InputStream is )
        throws IOException
    {
        this( delimiter, quote, mapNulls, new InputStreamReader( is,
                System.getProperty( "file.encoding" ) ) );
    }

    public DelimitedReader( char delimiter, char quote, boolean mapNulls, String encoding,
            String filename )
        throws IOException
    {
       	this ( delimiter, quote, mapNulls, new InputStreamReader( new FileInputStream( filename ),
        encoding ));
    }

    public DelimitedReader( char delimiter, char quote, String filename ) throws IOException {
        this( delimiter, quote, false, filename );
    }
    
    public DelimitedReader( char delimiter, char quote, boolean mapNulls, Reader reader )
        throws IOException
    {
        this( delimiter, quote, mapNulls, new LineNumberReader( reader ) );
    }

    public DelimitedReader( char delimiter, char quote, boolean mapNulls,
            LineNumberReader lineNumberReader )
        throws IOException
    {
        this.lineNumberReader = lineNumberReader;
        this.csv = new CSV( mapNulls, delimiter, quote );
        this.curLine = null;
        this.delimiter = delimiter;
        this.quote = quote;
    }

    public int getLineNumber() {
        return lineNumberReader.getLineNumber();
    }

    public char getDelimiter() {
        return this.delimiter;
    }

    public char getQuote() {
        return this.quote;
    }

    public boolean hasNext() throws IOException {
        if ( this.curLine == null ) {
            try {
                this.curLine = lineNumberReader.readLine();
            } catch ( IOException e ) {
                log.error( e );
                this.curLine = null;
            }
        }
        return this.curLine != null;
    }

    public String[] next() throws IOException, ParseException {
        if ( this.curLine == null ) {
            this.curLine = lineNumberReader.readLine();
        }
        
        if ( this.curLine == null ) {
            throw new NoSuchElementException( "There are no more lines to retrieve" );
        }

        List fieldList = null;
        try {
            fieldList = this.csv.decode( this.curLine );
        } catch ( ParseException e ) {
            log.error( "Caught ParseException parsing line: " + this.curLine );
            // set curLine to null if the client wants to continue
            this.raw = this.curLine;
            this.curLine = null;
            throw e;
        }

        this.raw = this.curLine;
        this.curLine = null;

        return ( String[] ) fieldList.toArray( new String[ 0 ] );
    }

    public String getLastRawLine() {
        return this.raw;
    }

    public void close() throws IOException {
        this.lineNumberReader.close();
    }
}
