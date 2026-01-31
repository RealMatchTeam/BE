package com.example.RealMatch.chat.infrastructure.tx;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;

@Component
public class SpringAfterCommitExecutor implements AfterCommitExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(SpringAfterCommitExecutor.class);

    @Override
    public void execute(Runnable task) {
        Objects.requireNonNull(task, "task must not be null");

        boolean hasTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        boolean hasSynchronization = TransactionSynchronizationManager.isSynchronizationActive();

        if (hasTransaction && hasSynchronization) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                task.run();
                            } catch (Exception ex) {
                                // afterCommit 내부 예외는 트랜잭션에 영향을 주지 않도록 로깅만 수행
                                LOG.error("Exception occurred in afterCommit task execution.", ex);
                            }
                        }
                    }
            );
            return;
        }

        String message = "AfterCommitExecutor must be used within an active transaction. " +
                "hasTransaction=%s, hasSynchronization=%s".formatted(hasTransaction, hasSynchronization);
        LOG.error(message);
        throw new IllegalStateException(message);
    }
}
