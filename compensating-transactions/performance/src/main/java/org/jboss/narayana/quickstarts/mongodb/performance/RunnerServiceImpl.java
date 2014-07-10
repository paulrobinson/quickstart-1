package org.jboss.narayana.quickstarts.mongodb.performance;

import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.narayana.quickstarts.mongodb.performance.jaxws.RunnerService;

import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author paul.robinson@redhat.com 10/07/2014
 */
@WebService(serviceName = "HotelServiceService", portName = "HotelService", name = "HotelService", targetNamespace = "http://www.jboss.org/as/quickstarts/compensationsApi/travel/hotel")
public class RunnerServiceImpl implements RunnerService {


    private Random rand = new Random(System.currentTimeMillis());

    private int counters;

    @Inject
    private CounterService counterService;

    private double compensateProbability;

    private AtomicInteger compensations = new AtomicInteger(0);


    @WebMethod
    public int run(int loops, int counters, double compensateProbability) {

        this.counters = counters;
        this.compensateProbability = compensateProbability;


        for (int i = 0; i < loops; i++) {
            update();
        }

        return compensations.get();
    }

    private void update() {

        try {
            int first = getCounter(-1);
            int second = getCounter(first);
            counterService.updateCounters(first, second, 1, compensateProbability);
        } catch (TransactionCompensatedException e) {
            compensations.incrementAndGet();
        }
    }

    private int getCounter(int exclude) {

        int result = rand.nextInt(counters) + 1;
        while (result == exclude) {
            result = rand.nextInt(counters) + 1;
        }
        return result;
    }
}
