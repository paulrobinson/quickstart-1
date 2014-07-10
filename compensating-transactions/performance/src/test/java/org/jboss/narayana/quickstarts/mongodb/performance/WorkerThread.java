package org.jboss.narayana.quickstarts.mongodb.performance;

import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.narayana.compensations.impl.BeanManagerUtil;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author paul.robinson@redhat.com 28/06/2014
 */
public class WorkerThread implements Runnable {


    private static Random rand = new Random(System.currentTimeMillis());

    int loops;

    int counters;

    CounterService counterService;

    double compensateProbability;

    static AtomicInteger compensations = new AtomicInteger(0);


    public WorkerThread(int loops, int counters, double compensateProbability) {

        this.loops = loops;
        this.counters = counters;
        this.compensateProbability = compensateProbability;

        //BeanManager beanManager = BeanManagerUtil.getBeanManager();
        BeanManager beanManager = CDI.current().getBeanManager();
        counterService = BeanManagerUtil.createBeanInstance(CounterService.class, beanManager);
    }

    @Override
    public void run() {

        for (int i=0; i<loops; i++) {
            update();
        }
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
        int result = rand.nextInt(counters)+1;
        while (result == exclude) {
            result = rand.nextInt(counters)+1;
        }
        return result;
    }


    public static int getCompensations() {
        return compensations.get();
    }
}
