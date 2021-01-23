

CREATE TABLE IF NOT EXISTS TxEvent (
  surrogateId BIGSERIAL PRIMARY KEY,
  serviceName varchar(36) NOT NULL,
  instanceId varchar(36) NOT NULL,
  creationTime timestamp(6) NOT NULL DEFAULT CURRENT_DATE,
  globalTxId varchar(36) NOT NULL,
  localTxId varchar(36) NOT NULL,
  parentTxId varchar(36) DEFAULT NULL,
  type varchar(50) NOT NULL,
  compensationMethod varchar(512) NOT NULL,
  expiryTime timestamp(6) NOT NULL,
  retryMethod varchar(512) NOT NULL,
  retries int NOT NULL DEFAULT 0,
  payloads bytea
);

CREATE INDEX IF NOT EXISTS saga_events_index ON TxEvent (surrogateId, globalTxId, localTxId, type, expiryTime);
CREATE INDEX IF NOT EXISTS saga_global_tx_index ON TxEvent (globalTxId);


CREATE TABLE IF NOT EXISTS Command (
  surrogateId BIGSERIAL PRIMARY KEY,
  eventId bigint NOT NULL UNIQUE,
  serviceName varchar(36) NOT NULL,
  instanceId varchar(36) NOT NULL,
  globalTxId varchar(36) NOT NULL,
  localTxId varchar(36) NOT NULL,
  parentTxId varchar(36) DEFAULT NULL,
  compensationMethod varchar(512) NOT NULL,
  payloads bytea,
  status varchar(12),
  lastModified timestamp(6) NOT NULL DEFAULT CURRENT_DATE,
  version bigint NOT NULL
);

CREATE INDEX IF NOT EXISTS saga_commands_index ON Command (surrogateId, eventId, globalTxId, localTxId, status);


CREATE TABLE IF NOT EXISTS TxTimeout (
  surrogateId BIGSERIAL PRIMARY KEY,
  eventId bigint NOT NULL UNIQUE,
  serviceName varchar(36) NOT NULL,
  instanceId varchar(36) NOT NULL,
  globalTxId varchar(36) NOT NULL,
  localTxId varchar(36) NOT NULL,
  parentTxId varchar(36) DEFAULT NULL,
  type varchar(50) NOT NULL,
  expiryTime TIMESTAMP NOT NULL,
  status varchar(12),
  version bigint NOT NULL
);

CREATE INDEX IF NOT EXISTS saga_timeouts_index ON TxTimeout (surrogateId, expiryTime, globalTxId, localTxId, status);

CREATE TABLE IF NOT EXISTS tcc_global_tx_event (
  surrogateId BIGSERIAL PRIMARY KEY,
  globalTxId varchar(36) NOT NULL,
  localTxId varchar(36) NOT NULL,
  parentTxId varchar(36) DEFAULT NULL,
  serviceName varchar(36) NOT NULL,
  instanceId varchar(36) NOT NULL,
  txType varchar(12),
  status varchar(12),
  creationTime timestamp(6) NOT NULL DEFAULT CURRENT_DATE,
  lastModified timestamp(6) NOT NULL DEFAULT CURRENT_DATE
);

CREATE UNIQUE INDEX IF NOT EXISTS tcc_global_tx_event_index ON tcc_global_tx_event (globalTxId, localTxId, parentTxId, txType);

CREATE TABLE IF NOT EXISTS tcc_participate_event (
  surrogateId BIGSERIAL PRIMARY KEY,
  serviceName varchar(36) NOT NULL,
  instanceId varchar(36) NOT NULL,
  globalTxId varchar(36) NOT NULL,
  localTxId varchar(36) NOT NULL,
  parentTxId varchar(36) DEFAULT NULL,
  confirmMethod varchar(512) NOT NULL,
  cancelMethod varchar(512) NOT NULL,
  status varchar(50) NOT NULL,
  creationTime timestamp(6) NOT NULL DEFAULT CURRENT_DATE,
  lastModified timestamp(6) NOT NULL DEFAULT CURRENT_DATE
);

CREATE UNIQUE INDEX IF NOT EXISTS tcc_participate_event_index ON tcc_participate_event (globalTxId, localTxId, parentTxId);

CREATE TABLE IF NOT EXISTS tcc_tx_event (
  surrogateId BIGSERIAL PRIMARY KEY,
  globalTxId varchar(36) NOT NULL,
  localTxId varchar(36) NOT NULL,
  parentTxId varchar(36) DEFAULT NULL,
  serviceName varchar(36) NOT NULL,
  instanceId varchar(36) NOT NULL,
  methodInfo varchar(512) NOT NULL,
  txType varchar(12),
  status varchar(12),
  creationTime timestamp(6) NOT NULL DEFAULT CURRENT_DATE,
  lastModified timestamp(6) NOT NULL DEFAULT CURRENT_DATE
);

CREATE UNIQUE INDEX IF NOT EXISTS tcc_tx_event_index ON tcc_tx_event (globalTxId, localTxId, parentTxId, txType);

CREATE TABLE IF NOT EXISTS master_lock (
  serviceName varchar(36) not NULL,
  expireTime timestamp(3) not NULL,
  lockedTime timestamp(3) not NULL,
  instanceId  varchar(255) not NULL,
  PRIMARY KEY (serviceName)
);

CREATE INDEX IF NOT EXISTS master_lock_index ON master_lock (serviceName);
