/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2008-2009 The eXist Project
 * http://exist-db.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *  
 *  $Id$
 */
package org.exist.xquery.modules.file;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;
import org.exist.dom.QName;
import org.exist.memtree.MemTreeBuilder;
import org.exist.util.DirectoryScanner;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.DateTimeValue;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;


/**
 * eXist File Module Extension DirectoryListFunction 
 * 
 * Enumerate a list of files, including their size and modification time, found in a specified directory, using a pattern
 * 
 * @author Andrzej Taramina <andrzej@chaeron.com>
 * @author ljo
 * @serial 2009-08-09
 * @version 1.2
 *
 * @see org.exist.xquery.BasicFunction#BasicFunction(org.exist.xquery.XQueryContext, org.exist.xquery.FunctionSignature)
 */

public class DirectoryListFunction extends BasicFunction {	
	
	private final static Logger logger = Logger.getLogger(DirectoryListFunction.class);
	
	final static String NAMESPACE_URI = FileModule.NAMESPACE_URI;
	final static String PREFIX = FileModule.PREFIX;
	
	
	public final static FunctionSignature[] signatures =
	{
		new FunctionSignature(
			new QName("directory-list", NAMESPACE_URI, PREFIX),
			"List all files, including their file size and modification time, found in or below a directory, $directory. Files are located in the server's " +
			"file system, using filename patterns, $pattern.  File pattern matching is based " +
			"on code from Apache's Ant, thus following the same conventions. For example:\n\n" +
			"'*.xml' matches any file ending with .xml in the current directory,\n- '**/*.xml' matches files " +
			"in any directory below the specified directory. ",
			new SequenceType[]
			{
			    new FunctionParameterSequenceType("directory", Type.STRING, Cardinality.EXACTLY_ONE, "The base directory path in the file system where the files are located."),
			    new FunctionParameterSequenceType("pattern", Type.STRING, Cardinality.EXACTLY_ONE, "The file name pattern")
				},
			new FunctionReturnSequenceType( Type.NODE, Cardinality.ZERO_OR_ONE, "a node fragment that shows all matching filenames, including their file size and modification time, and the subdirectory they were found in" )
			)
		};
	
	
	/**
	 * DirectoryListFunction Constructor
	 * 
	 * @param context	The Context of the calling XQuery
	 */
	
	public DirectoryListFunction( XQueryContext context, FunctionSignature signature )
	{
		super( context, signature );
	}
	
	
	/**
	 * evaluate the call to the XQuery execute() function,
	 * it is really the main entry point of this class
	 * 
	 * @param args		arguments from the execute() function call
	 * @param contextSequence	the Context Sequence to operate on (not used here internally!)
	 * @return		A node representing the SQL result set
	 * 
	 * @see org.exist.xquery.BasicFunction#eval(org.exist.xquery.value.Sequence[], org.exist.xquery.value.Sequence)
	 */
	public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException
	{
		File baseDir = new File( args[0].getStringValue() );
		Sequence patterns = args[1];
		
		logger.info("Listing matching files in directory: " + baseDir);
		
		Sequence xmlResponse = null;
		
		MemTreeBuilder builder = context.getDocumentBuilder();
		
		builder.startDocument();
		builder.startElement( new QName( "list", NAMESPACE_URI, PREFIX ), null );
		builder.addAttribute( new QName( "directory", null, null ), baseDir.toString() );
		
		for( SequenceIterator i = patterns.iterate(); i.hasNext(); ) {
			String pattern 	= i.nextItem().getStringValue();
			File[] files 	= DirectoryScanner.scanDir( baseDir, pattern );
			String relDir 	= null;
			
			logger.info("Found: " + files.length);
			
			for (int j = 0; j < files.length; j++) {
				logger.info("Found: " + files[j].getAbsolutePath());
				
				String relPath = files[j].toString().substring(baseDir.toString().length() + 1);
				
				int p = relPath.lastIndexOf(File.separatorChar);
				
				if (p >= 0) {
					relDir = relPath.substring(0, p);
					relDir = relDir.replace(File.separatorChar, '/');
				}
				
				builder.startElement(new QName("file", NAMESPACE_URI, PREFIX), null);
				
				builder.addAttribute(new QName("name", null, null), files[j].getName());

                Long sizeLong = files[j].length();
                String sizeString = Long.toString(sizeLong);
                String humanSize = getHumanSize(sizeLong, sizeString);
                
				builder.addAttribute(new QName("size", null, null), sizeString);
                builder.addAttribute(new QName("human-size", null, null), humanSize);
                builder.addAttribute(new QName("modified", null, null), new DateTimeValue(new Date(files[j].lastModified())).getStringValue());

				if (relDir != null && relDir.length() > 0) {
					builder.addAttribute(new QName("subdir", null, null), relDir);
				}
				
				builder.endElement();
				
			}
		}
		
		builder.endElement();
		
		xmlResponse = (NodeValue) builder.getDocument().getDocumentElement();
		
		return(xmlResponse);
	}

    private String getHumanSize(final Long sizeLong, final String sizeString) {
        String humanSize = "n/a";
        int sizeDigits = sizeString.length();
        if (sizeDigits < 4) {
            humanSize = Long.toString(Math.abs(sizeLong));
        } else if (sizeDigits >= 4 && sizeDigits <= 5) {
            if (sizeLong < 1024) {
                // We don't want 0KB för e.g. 1006 Bytes.
                humanSize = Long.toString(Math.abs(sizeLong));
            } else {
                humanSize = Math.abs((sizeLong / 1024)) + "KB";
            }
        } else if(sizeDigits >= 6 && sizeDigits <= 8) {
            humanSize = Math.abs(sizeLong / (1024 * 1024)) + "MB";
        } else if (sizeDigits >= 9) {
            humanSize = Math.abs((sizeLong / (1024 * 1024 * 1024))) +"GB";
        }
        return humanSize;
    }
	
}