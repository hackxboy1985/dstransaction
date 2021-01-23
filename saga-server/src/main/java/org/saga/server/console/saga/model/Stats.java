
package org.saga.server.console.saga.model;

import java.util.Date;

public class Stats {

  private int totalTransactions;

  private int pendingTransactions;

  private int committedTransactions;

  private int compensatingTransactions;

  private int rollbackTransactions;

  private Date updatedAt;

  private int failureRate;

  public Stats(int totalTransactions, int pendingTransactions, int committedTransactions, int compensatingTransactions,
      int rollbackTransactions) {
    setTotalTransactions(totalTransactions);
    setPendingTransactions(pendingTransactions);
    setCommittedTransactions(committedTransactions);
    setCompensatingTransactions(compensatingTransactions);
    setRollbackTransactions(rollbackTransactions);
    if (totalTransactions > 0 && rollbackTransactions + compensatingTransactions > 0) {
      setFailureRate((rollbackTransactions + compensatingTransactions) * 100 / totalTransactions);
    } else {
      setFailureRate(0);
    }

    setUpdatedAt(new Date());
  }

  public int getTotalTransactions() {
    return totalTransactions;
  }

  public void setTotalTransactions(int totalTransactions) {
    this.totalTransactions = totalTransactions;
  }

  public int getPendingTransactions() {
    return pendingTransactions;
  }

  public void setPendingTransactions(int pendingTransactions) {
    this.pendingTransactions = pendingTransactions;
  }

  public int getCommittedTransactions() {
    return committedTransactions;
  }

  public void setCommittedTransactions(int committedTransactions) {
    this.committedTransactions = committedTransactions;
  }

  public int getCompensatingTransactions() {
    return compensatingTransactions;
  }

  public void setCompensatingTransactions(int compensatingTransactions) {
    this.compensatingTransactions = compensatingTransactions;
  }

  public int getRollbackTransactions() {
    return rollbackTransactions;
  }

  public void setRollbackTransactions(int rollbackTransactions) {
    this.rollbackTransactions = rollbackTransactions;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public int getFailureRate() {
    return failureRate;
  }

  public void setFailureRate(int failureRate) {
    this.failureRate = failureRate;
  }
}
