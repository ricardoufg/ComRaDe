/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.dbcp2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Rodney Waldhoff
 * @author Dirk Verbeeck
 * @version $Id: TestDelegatingStatement.java 1649424 2015-01-04 21:15:33Z tn $
 */
public class TestDelegatingStatement {

    private DelegatingConnection<Connection> conn = null;
    private Connection delegateConn = null;
    private DelegatingStatement stmt = null;
    private Statement delegateStmt = null;

    @Before
    public void setUp() throws Exception {
        delegateConn = new TesterConnection("test", "test");
        delegateStmt = new TesterStatement(delegateConn);
        conn = new DelegatingConnection<>(delegateConn);
        stmt = new DelegatingStatement(conn,delegateStmt);
    }

    @Test
    public void testExecuteQueryReturnsNull() throws Exception {
        assertNull(stmt.executeQuery("null"));
    }

    @Test
    public void testGetDelegate() throws Exception {
        assertEquals(delegateStmt,stmt.getDelegate());
    }

    @Test
    public void testCheckOpen() throws Exception {
        stmt.checkOpen();
        stmt.close();
        try {
            stmt.checkOpen();
            fail("Expecting SQLException");
        } catch (SQLException ex) {
            // expected
        }
    }

    @Test
    public void testIsWrapperFor() throws Exception {
        TesterConnection tstConn = new TesterConnection("test", "test");
        TesterStatement tstStmt = new TesterStatementNonWrapping(tstConn);
        DelegatingConnection<TesterConnection> dconn = new DelegatingConnection<>(tstConn);
        DelegatingStatement stamt = new DelegatingStatement(dconn, tstStmt);

        Class<?> stmtProxyClass = Proxy.getProxyClass(
                this.getClass().getClassLoader(),
                Statement.class);

        assertTrue(stamt.isWrapperFor(DelegatingStatement.class));
        assertTrue(stamt.isWrapperFor(TesterStatement.class));
        assertFalse(stamt.isWrapperFor(stmtProxyClass));

        stamt.close();
    }

    private static class TesterStatementNonWrapping extends TesterStatement {

        public TesterStatementNonWrapping(Connection conn) {
            super(conn);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}
