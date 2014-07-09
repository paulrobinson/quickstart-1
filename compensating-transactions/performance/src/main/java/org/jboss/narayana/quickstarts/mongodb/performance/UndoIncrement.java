package org.jboss.narayana.quickstarts.mongodb.performance;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.jboss.narayana.compensations.cdi.api.CompensationHandler;

import javax.inject.Inject;

/**
 * This compensation handler is used to undo a debit operation.
 *
 * @author paul.robinson@redhat.com 09/01/2014
 */
public class UndoIncrement implements CompensationHandler {

    @Inject
    IncrementCounterData incrementCounterData;

    @Override
    public void compensate() {

        MongoClient mongoClient = CounterManager.getMongoClient();
        DB database = mongoClient.getDB("test");
        DBCollection accounts = database.getCollection("counters");

        System.out.println(incrementCounterData.getCounter());
        System.out.println(incrementCounterData.getAmount());

        accounts.update(new BasicDBObject("name", String.valueOf(incrementCounterData.getCounter())), new BasicDBObject("$inc", new BasicDBObject("value", -1 * incrementCounterData.getAmount())));
    }
}
