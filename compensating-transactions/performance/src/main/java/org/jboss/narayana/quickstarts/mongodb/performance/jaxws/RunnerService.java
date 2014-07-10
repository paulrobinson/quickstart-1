package org.jboss.narayana.quickstarts.mongodb.performance.jaxws;

import javax.jws.WebService;

/**
 * @author paul.robinson@redhat.com 10/07/2014
 */
@WebService(name = "HotelService", targetNamespace = "http://www.jboss.org/as/quickstarts/compensationsApi/travel/hotel")
public interface RunnerService {

    public int run(int loops, int counters, double compensateProbability);

}
