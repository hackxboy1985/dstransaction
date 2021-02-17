

package org.saga.server.callback;

import org.saga.server.exception.AlphaException;
import org.saga.server.exception.CompensateAckFailedException;
import org.saga.server.exception.CompensateConnectException;
import org.saga.server.txevent.TxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class CompositeAgentCallback implements AgentCallback {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final Map<String, Map<String, AgentCallback>> callbacks;

  public CompositeAgentCallback(Map<String, Map<String, AgentCallback>> callbacks) {
    this.callbacks = callbacks;
  }

  @Override
  public void compensate(TxEvent event) {
    Map<String, AgentCallback> serviceCallbacks = callbacks.getOrDefault(event.serviceName(), emptyMap());

    AgentCallback agentCallback = serviceCallbacks.get(event.instanceId());
    if (agentCallback == null) {
      LOG.info("Cannot find the service with the instanceId {}, call the other instance.", event.instanceId());
      // TODO extract an Interface to let user define the serviceCallback instance pick strategy
      Iterator<AgentCallback> iterator = new ArrayList<>(serviceCallbacks.values()).iterator();
      if(iterator.hasNext()) {
        agentCallback = iterator.next();
      }
    }
    if(agentCallback==null){
      throw new AlphaException("Compensate error, No such omega callback found for service " + event.serviceName());
    }

    try {
      agentCallback.compensate(event);
    } catch (CompensateConnectException e) {
      serviceCallbacks.values().remove(agentCallback);
      throw e;
    } catch (CompensateAckFailedException e) {
      throw e;
    } catch (Exception e) {
      serviceCallbacks.values().remove(agentCallback);
      throw e;
    }
  }
}
