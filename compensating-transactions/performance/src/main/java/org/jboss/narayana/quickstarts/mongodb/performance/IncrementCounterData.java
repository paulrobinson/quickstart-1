package org.jboss.narayana.quickstarts.mongodb.performance;

import org.jboss.narayana.compensations.cdi.api.CompensationScoped;

import java.io.Serializable;

/**
 * This is a CompensationScoped POJO that is used to store data against the current running compensating transaction.
 *
 * This scope is also available to the compensation handlers.
 *
 * @author paul.robinson@redhat.com 09/01/2014
 */
@CompensationScoped
public class IncrementCounterData implements Serializable {

    private int counter;
    private int amount;

    public IncrementCounterData() {}

    public void setCounter(int counter) {

        this.counter = counter;
    }

    public void setAmount(int amount) {

        this.amount = amount;
    }

    public int getCounter() {

        return counter;
    }

    public int getAmount() {

        return amount;
    }
}