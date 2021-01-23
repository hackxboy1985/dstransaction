

package org.saga.server.command;

import java.util.List;

public interface CommandRepository {

  void saveCompensationCommands(String globalTxId);

  void markCommandAsDone(String globalTxId, String localTxId);

  List<Command> findUncompletedCommands(String globalTxId);

  List<Command> findFirstCommandToCompensate();
}
