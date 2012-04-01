/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.blacktie.jatmibroker.ejb.connector.ejb;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.jab.JABException;
import org.jboss.narayana.blacktie.jatmibroker.jab.JABSession;
import org.jboss.narayana.blacktie.jatmibroker.jab.JABSessionAttributes;
import org.jboss.narayana.blacktie.jatmibroker.jab.JABTransaction;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionFactory;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

public class TxBlacktieServiceTestCase extends TestCase {
    private static final Logger log = LogManager.getLogger(TxBlacktieServiceTestCase.class);
    private Connection connection;
    private JABSession session;

    public void setUp() throws ConnectionException, ConfigurationException, JABException {
        log.info("TxBlacktieServiceTestCase::setUp");
        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();

        JABSessionAttributes attrs = new JABSessionAttributes();
        session = new JABSession(attrs);
    }

    public void tearDown() throws ConnectionException, ConfigurationException {
        log.info("TxBlacktieServiceTestCase::tearDown");
        connection.close();
    }

    public void test1() throws ConnectionException, JABException, ConfigurationException, NotFound, CannotProceed, InvalidName,
            org.omg.CORBA.ORBPackage.InvalidName, AdapterInactive {
        log.info("TxBlacktieServiceTestCase::test1");
        byte[] args = "test=test1,tx=true".getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null, args.length);
        buffer.setByteArray(args);

        JABTransaction transaction = new JABTransaction(session, 5000);
        Response response = connection.tpcall("TxEchoService", buffer, 0);
        String responseData = new String(((X_OCTET) response.getBuffer()).getByteArray());
        transaction.commit();
        assertEquals("test=test1,tx=true", responseData);
    }

    public void test2() throws ConnectionException, JABException, ConfigurationException {
        log.info("TxBlacktieServiceTestCase::test2");
        byte[] args = "test=test2,tx=true".getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null, args.length);
        buffer.setByteArray(args);

        Response response = connection.tpcall("TxEchoService", buffer, 0);
        String responseData = new String(((X_OCTET) response.getBuffer()).getByteArray());
        assertNotSame("test=test2,tx=true", responseData);
    }

    public void test3() throws ConnectionException, JABException, ConfigurationException {
        log.info("TxBlacktieServiceTestCase::test3");
        byte[] args = "test=test3,tx=false".getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null, args.length);
        buffer.setByteArray(args);

        Response response = connection.tpcall("TxEchoService", buffer, 0);
        String responseData = new String(((X_OCTET) response.getBuffer()).getByteArray());
        assertEquals("test=test3,tx=false", responseData);
    }

    public void test4() throws ConnectionException, JABException, ConfigurationException, NotFound, CannotProceed, InvalidName,
            org.omg.CORBA.ORBPackage.InvalidName, AdapterInactive {
        log.info("TxBlacktieServiceTestCase::test4");
        byte[] args = "test=test4,tx=false".getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null, args.length);
        buffer.setByteArray(args);

        JABTransaction transaction = new JABTransaction(session, 5000);
        Response response = connection.tpcall("TxEchoService", buffer, 0);
        String responseData = new String(((X_OCTET) response.getBuffer()).getByteArray());
        transaction.commit();
        assertNotSame("test=test4,tx=false", responseData);
    }

    /*
     * Test that the AS can create a transaction and propagate it too another blacktie service.
     */
    public void test5() throws ConnectionException, JABException, ConfigurationException {
        log.info("TxBlacktieServiceTestCase::test5");
        byte[] args = "test=test5,tx=create".getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null, args.length);
        buffer.setByteArray(args);

        Response response = connection.tpcall("TxEchoService", buffer, 0);
        String responseData = new String(((X_OCTET) response.getBuffer()).getByteArray());
        assertEquals("test=test5,tx=create", responseData);
    }
}