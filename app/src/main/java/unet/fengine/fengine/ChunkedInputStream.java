package unet.fengine.fengine;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
// ChunkedInputStream - an InputStream that implements HTTP/1.1 chunking
//
// Copyright (C) 1996,1998 by Jef Poskanzer <jef@acme.com>. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// Visit the ACME Labs Java page for up-to-date versions of this and other
// fine Java utilities: http://www.acme.com/java/

/**
 * Modifications done by Daniel Matuschek (daniel@matuschek.net)
 * - modified JavaDoc documentation
 * - adapted to Java 1.2, removed deprecated DataInputStream.readLine() method
 * - replaced DataInputStream by InputStream (there was no need for a
 *   DatainputStream, not idea why this was used in the original version)
 * - fixed a bug (there is an CRLF after every the data block)
 */


/**
 * An InputStream that implements HTTP/1.1 chunking.
 * <P>
 * This class lets a Servlet read its request data as an HTTP/1.1 chunked
 * stream.  Chunked streams are a way to send arbitrary-length data without
 * having to know beforehand how much you're going to send.  They are
 * introduced by a "Transfer-Encoding: chunked" header, so if such a header
 * appears in an HTTP request you should use this class to read any data.
 * <P>
 * Sample usage:
 * <BLOCKQUOTE><PRE><CODE>
 * InputStream in = req.getInputStream();
 * if ( "chunked".equals( req.getHeader( "Transfer-Encoding" ) ) )
 *     in = new ChunkedInputStream( in );
 * </CODE></PRE></BLOCKQUOTE>
 * <P>
 * Because it would be impolite to make the authors of every Servlet include
 * the above code, this is general done at the server level so that it
 * happens automatically.  Servlet authors will generally not create
 * ChunkedInputStreams.  This is in contrast with ChunkedOutputStream,
 * which Servlets have to call themselves if they want to use it.
 * <P>
 * <A HREF="/resources/classes/Acme/Serve/servlet/http/ChunkedInputStream.java">Fetch the software.</A><BR>
 * <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>
 *
 * @author Jef Poskanzer
 * @author Daniel Matuschek
 * @version $Id: ChunkedInputStream.java,v 1.6 2002/05/31 14:45:56 matuschd Exp $
 */
public class ChunkedInputStream extends FilterInputStream
{

    private int contentLength;
    private byte[] b1 = new byte[1];

    /** number of bytes available in the current chunk */
    private int chunkCount = 0;

    private Vector<String> footerNames = null;
    private Vector<String> footerValues = null;

    /**
     * Make a ChunkedInputStream.
     */
    public ChunkedInputStream( InputStream in )
    {
        super(in);
        contentLength = 0;
    }


    /**
     * The FilterInputStream implementation of the single-byte read()
     * method just reads directly from the underlying stream.  We want
     * to go through our own read-block method, so we have to override.
     * Seems like FilterInputStream really ought to do this itself.
     */
    public int read() throws IOException
    {
        if (read(b1,0,1) == -1 ) {
            return -1;
        }


        return b1[0];
    }


    /**
     * Reads into an array of bytes.
     * @param b the buffer into which the data is read
     * @param off the start offset of the data
     * @param len the maximum number of bytes read
     * @return the actual number of bytes read, or -1 on EOF
     * @exception IOException if an I/O error has occurred
     */
    public int read( byte[] b, int off, int len ) throws IOException
    {
        if (chunkCount == 0) {
            startChunk();
            if (chunkCount == 0) {
                return -1;
            }
        }
        int toRead = Math.min( chunkCount, len );
        int r = in.read( b, off, toRead );

        if ( r != -1 ) {
            chunkCount -= r;
        }
        return r;
    }


    /**
     * Reads the start of a chunk.
     */
    private void startChunk() throws IOException
    {
        String line = readLine();
        if (line.equals("")) {
            line=readLine();
        }

        try {
            chunkCount = Integer.parseInt(line.trim(),16);
        } catch (NumberFormatException e) {
            throw new IOException("malformed chunk ("+line+")");
        }
        contentLength += chunkCount;
        if ( chunkCount == 0 ) {
            readFooters();
        }

    }


    /**
     * Reads any footers.
     */
    private void readFooters() throws IOException
    {
        footerNames = new Vector<String>();
        footerValues = new Vector<String>();
        String line;
        while ( true ) {
            line = readLine();
            if ( line.length() == 0 )
                break;
            int colon = line.indexOf( ':' );
            if ( colon != -1 )
            {
                String name = line.substring( 0, colon ).toLowerCase();
                String value = line.substring( colon + 1 ).trim();
                footerNames.addElement( name.toLowerCase() );
                footerValues.addElement( value );
            }
        }
    }


    /**
     * Returns the value of a footer field, or null if not known.
     * Footers come at the end of a chunked stream, so trying to
     * retrieve them before the stream has given an EOF will return
     * only nulls.
     * @param name the footer field name
     */
    public String getFooter( String name )
    {
        if ( ! isDone() )
            return null;
        int i = footerNames.indexOf( name.toLowerCase() );
        if ( i == -1 )
            return null;
        return (String) footerValues.elementAt( i );
    }


    /**
     * Returns an Enumeration of the footer names.
     */
    public Enumeration getFooters()
    {
        if ( ! isDone() )
            return null;
        return footerNames.elements();
    }


    /**
     * Returns the size of the request entity data, or -1 if not known.
     */
    public int getContentLength()
    {
        if (! isDone()) {
            return -1;
        }
        return contentLength;
    }


    /**
     * Tells whether the stream has gotten to its end yet.  Remembering
     * whether you've gotten an EOF works fine too, but this is a convenient
     * predicate.  java.io.InputStream should probably have its own isEof()
     * predicate.
     */
    public boolean isDone()
    {
        return footerNames != null;
    }


    /**
     * ChunkedInputStream used DataInputStream.readLine() before. This method
     * is deprecated, therefore we will it replace by our own method.
     * Because the chunk lines only use 7bit ASCII, we can use the
     * system default encoding
     * The data lines itself will not be read using this readLine method
     * but by a block read
     */
    protected String readLine()
            throws IOException
    {
        final byte CR=13;
        final byte LF=10;

        ByteBuffer buff = new ByteBuffer();
        byte b=0;

        int i=0;
        do {
            b = (byte)this.in.read();
            if (b != LF) {
                buff.append(b);
            }
            i++;
        } while ((b != LF));

        // according to the RFC there must be a CR before the LF, but some
        // web servers don't do this :-(
        byte[] byteBuff = buff.getContent();

        if (byteBuff.length == 0) {
            return "";
        }

        if (byteBuff[byteBuff.length-1] != CR) {
            return new String(byteBuff);
        } else {
            return new String(byteBuff,0,byteBuff.length-1);
        }
    }

}


/*********************************************
 Copyright (c) 2001 by Daniel Matuschek
 *********************************************/

/**
 * A ByteBuffer implements a growable byte array. You can simple
 * add bytes like you do it using a Vector, but internally the buffer
 * is implemented as a real array of bytes. This increases memory usage.
 *
 * @author Daniel Matuschek
 * @version $Id $
 */
class ByteBuffer {

    protected final int INITIALSIZE=1024;

    protected int used = 0;
    protected int size = 0;
    protected byte[] buff =null;

    /**
     * Initializes a new ByteBuffer object and creates
     * a temporary buffer array of a predefined initial size.
     * If you want to set your own initial size, use the <code>setSize</code>
     * method after initializing the object.
     *
     */
    public ByteBuffer() {
        size=INITIALSIZE;
        buff=new byte[INITIALSIZE];
    }


    /**
     * Appends a byte to the end of the buffer
     *
     * If the currently reserved memory is used, the size of the
     * internal buffer will be doubled.
     * In this case the memory usage will temprary increase by factor 3
     * because it need a temporary storage for the old data.
     *
     * Be sure that you have enough heap memory !
     *
     * @param b byte to append
     */
    public void append(byte b) {
        if (used >= size) {
            doubleBuffer();
        }

        buff[used]=b;
        used++;
    }

    /**
     * @return the number of bytes stored in the buffer
     */
    public int length() {
        return used;
    }


    /**
     * @return the buffer contents as a byte array
     */
    public byte[] getContent() {
        byte[] b = new byte[used];
        for (int i=0; i<used; i++) {
            b[i]=buff[i];
        }
        return b;
    }

    /**
     * removes all contents in the buffer
     */
    public void clean() {
        used=0;
    }


    /**
     * Sets the size of the internal buffer to
     * the given value. This is useful, if the size of the
     * data that should be stored is known.
     * @param size size of the buffer in Bytes
     */
    public void setSize(int size) {

        // if we have already used more data, ignore it !
        if (size < used) {
            return;
        }

        this.size=size;

        // create a new (larger) array
        byte[] newBuff = new byte[size];

        // copy contents
        for (int i=0; i<used; i++) {
            newBuff[i]=buff[i];
        }

        buff=newBuff;
    }


    /**
     * Print the buffer content as a String (use it for debugging only !)
     * @return a String containing every byte in the buffer as a character
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(buff.length);
        for (int i=0; i<used; i++) {
            sb.append(buff[i]);
        }
        return sb.toString();
    }


    /**
     * doubles the size of the internal buffer
     */
    protected void doubleBuffer() {
        // increase size
        setSize(size*2);
    }
}
