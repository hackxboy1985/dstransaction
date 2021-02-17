

package org.saga.server.callback;

import cn.ds.transaction.grpc.protocol.GrpcCompensateCommand;
import org.saga.server.txevent.TxEvent;

import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;

public class GrpcAgentCallback implements AgentCallback {

  private final StreamObserver<GrpcCompensateCommand> observer;

  public GrpcAgentCallback(StreamObserver<GrpcCompensateCommand> observer) {
    this.observer = observer;
  }

  @Override
  public void compensate(TxEvent event) {
    GrpcCompensateCommand command = GrpcCompensateCommand.newBuilder()
        .setGlobalTxId(event.globalTxId())
        .setLocalTxId(event.localTxId())
        .setParentTxId(event.parentTxId() == null ? "" : event.parentTxId())
        .setCompensationMethod(event.compensationMethod())
        .setPayloads(ByteString.copyFrom(event.payloads()))
        .build();
    observer.onNext(command);
  }

  @Override
  public void disconnect() {
    observer.onCompleted();
  }
}
