/*
 * eXist-db Open Source Native XML Database
 * Copyright (C) 2001 The eXist-db Authors
 *
 * info@exist-db.org
 * http://www.exist-db.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.xquery;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.exist.EXistException;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.test.ExistEmbeddedServer;
import org.exist.xquery.parser.XQueryAST;
import org.exist.xquery.parser.XQueryLexer;
import org.exist.xquery.parser.XQueryParser;
import org.exist.xquery.parser.XQueryTreeParser;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * @author <a href="gabriele@strumenta.com">Gabriele Tomassetti</a>
 */
public class WindowClauseTest {

    @ClassRule
    public static final ExistEmbeddedServer existEmbeddedServer = new ExistEmbeddedServer(true, true);

    @Test
    public void simpleWindowConditions() throws EXistException, RecognitionException, XPathException, TokenStreamException {
        final String query = "xquery version \"3.1\";\n" +
                "for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "    start at $s when fn:true()\n" +
                "    only end at $e when $e - $s eq 2\n" +
                "return <window>{ $w }</window>";

        final BrokerPool pool = existEmbeddedServer.getBrokerPool();
        try (final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            final XQueryContext context = new XQueryContext(broker.getBrokerPool());
            final XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            final XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            final XQueryAST ast = (XQueryAST) xparser.getAST();

            final XQueryTreeParser treeParser = new XQueryTreeParser(context);
            final PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
        }
    }

    @Test
    public void complexWindowCondition() throws EXistException, RecognitionException, XPathException, TokenStreamException {
        final String query = "xquery version \"3.1\";\n" +
                "for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "   start $first next $second when $first/price < $second/price\n" +
                "   end $last next $beyond when $last/price > $beyond/price\n" +
                "return <window>{ $w }</window>";

        final BrokerPool pool = existEmbeddedServer.getBrokerPool();
        try (final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            final XQueryContext context = new XQueryContext(broker.getBrokerPool());
            final XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            final XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            final XQueryAST ast = (XQueryAST) xparser.getAST();

            final XQueryTreeParser treeParser = new XQueryTreeParser(context);
            final PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
        }
    }

    @Test
    public void noEndWindowCondition() throws EXistException, RecognitionException, XPathException, TokenStreamException {
        final String query = "xquery version \"3.1\";\n" +
                "for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "   start $first next $second when $first/price < $second/price\n" +
                "return <window>{ $w }</window>";

        final BrokerPool pool = existEmbeddedServer.getBrokerPool();
        try (final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            final XQueryContext context = new XQueryContext(broker.getBrokerPool());
            final XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            final XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            final XQueryAST ast = (XQueryAST) xparser.getAST();

            final XQueryTreeParser treeParser = new XQueryTreeParser(context);
            final PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
        }
    }

    @Test
    public void slidingWindowClause() throws EXistException, RecognitionException, XPathException, TokenStreamException {
        final String query = "xquery version \"3.1\";\n" +
                "for sliding window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "    start at $s when fn:true()\n" +
                "    only end at $e when $e - $s eq 2\n" +
                "return <window>{ $w }</window>";

        final BrokerPool pool = existEmbeddedServer.getBrokerPool();
        try (final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            final XQueryContext context = new XQueryContext(broker.getBrokerPool());
            final XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            final XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            final XQueryAST ast = (XQueryAST) xparser.getAST();

            final XQueryTreeParser treeParser = new XQueryTreeParser(context);
            final PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
            assertEquals(WindowExpr.WindowType.SLIDING_WINDOW, ((WindowExpr) expr.getFirst()).getWindowType());
        }
    }
}
