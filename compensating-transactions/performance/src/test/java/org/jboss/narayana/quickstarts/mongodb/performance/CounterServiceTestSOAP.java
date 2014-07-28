/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package org.jboss.narayana.quickstarts.mongodb.performance;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.quickstarts.mongodb.performance.jaxws.RunnerService;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

@RunWith(Arquillian.class)
public class CounterServiceTestSOAP {

    private DBCollection counters;

    private static final int COUNTERS = 5;
    private static final int THREADS = 10;
    private static final int ITERATIONS = 5000;
    private static final int COMPENSATE_PROB = 0;

    private List<Thread> threads = new LinkedList<Thread>();


    @Deployment
    public static WebArchive createTestArchive() {

        //Use 'Shrinkwrap Resolver' to include the mongodb java driver in the deployment
        File lib = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.mongodb:mongo-java-driver:2.10.1").withoutTransitivity().asSingleFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, CounterService.class.getPackage().getName())
                .addAsManifestResource("services/javax.enterprise.inject.spi.Extension")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(lib);


        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        final String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.narayana.txframework\n";
        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }


    /**
     * Setup the initial test data. Give both counters 'A' and 'B' £1000
     *
     * @throws Exception
     */
    @Before
    public void resetAccountData() throws Exception {

        MongoClient mongo = new MongoClient("localhost", 27017);
        DB database = mongo.getDB("test");

        database.getCollection("counters").drop();
        counters = database.getCollection("counters");

        for (int i = 1; i < COUNTERS + 1; i++) {
            counters.insert(new BasicDBObject("name", String.valueOf(i)).append("value", 0).append("tx", 0));
        }
    }

    @Test
    public void perf() throws Exception {

        double before = System.currentTimeMillis();

        LinkedList<Thread> threads = new LinkedList<Thread>();
        for (int i = 0; i < THREADS; i++) {
            Thread clientThread = new Thread(new ClientThread());
            threads.add(clientThread);
            clientThread.start();
        }
        for (int i = 0; i < THREADS; i++) {
            threads.get(i).join();
        }

        double after = System.currentTimeMillis();
        double durationSecs = (after - before) / 1000;


        double itersPerSec = ITERATIONS / durationSecs;
        System.out.println("\n\n");
        System.out.println("Duration: " + durationSecs);
        System.out.println("Iterations: " + ITERATIONS);
        System.out.println("THROUGHPUT: " + itersPerSec + "/sec");
        System.out.println("\n\n");

    }

    private static class ClientThread implements Runnable {


        @Override
        public void run() {

            RunnerService runnerService = createWebServiceClient();
            runnerService.run(ITERATIONS, COUNTERS, COMPENSATE_PROB);
        }

        private RunnerService createWebServiceClient() {

            try {
                URL wsdlLocation = new URL("http://localhost:8080/test/HotelServiceService?wsdl");
                QName serviceName = new QName("http://www.jboss.org/as/quickstarts/compensationsApi/travel/hotel",
                        "HotelServiceService");
                QName portName = new QName("http://www.jboss.org/as/quickstarts/compensationsApi/travel/hotel",
                        "HotelService");

                Service service = Service.create(wsdlLocation, serviceName);
                return service.getPort(portName, RunnerService.class);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Error creating Web Service client", e);
            }
        }
    }
}
