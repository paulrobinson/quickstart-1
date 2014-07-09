package org.jboss.narayana.quickstarts.mongodb.performance;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.jboss.narayana.compensations.api.CompensationHandler;

import javax.inject.Inject;

/**
 * This compensation handler is used to undo a credit operation.
 *
 * @author paul.robinson@redhat.com 09/01/2014
 */
public class UndoDecrement implements CompensationHandler {

    @Inject
    DecrementCounterData decrementCounterData;

    @Override
    public void compensate() {

        MongoClient mongoClient = CounterManager.getMongoClient();
        DB database = mongoClient.getDB("test");
        DBCollection accounts = database.getCollection("counters");

        accounts.update(new BasicDBObject("name", String.valueOf(decrementCounterData.getCounter())), new BasicDBObject("$inc", new BasicDBObject("value", decrementCounterData.getAmount())));
    }
}
