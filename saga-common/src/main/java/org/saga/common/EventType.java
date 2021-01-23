

package org.saga.common;

public enum EventType {
  SagaStartedEvent,
  TxStartedEvent,
  TxEndedEvent,
  TxAbortedEvent,
  TxCompensatedEvent,
  SagaEndedEvent,
  SagaAbortedEvent,
  SagaTimeoutEvent,
  TxCompensateEvent,
  TxCompensateAckFailedEvent,
  TxCompensateAckSucceedEvent,
  CompensateAckTimeoutEvent
}
