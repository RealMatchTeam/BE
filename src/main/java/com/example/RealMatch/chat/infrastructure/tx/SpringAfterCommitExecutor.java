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

        // 트랜잭션이 없거나 동기화가 비활성화된 경우 즉시 실행 (운영 감지용 로그)
        LOG.warn("Executing task immediately without transaction synchronization. " +
                "hasTransaction={}, hasSynchronization={}. " +
                "This may indicate an unexpected call path.",
                hasTransaction, hasSynchronization);

        try {
            task.run();
        } catch (Exception ex) {
            // 비트랜잭션 케이스에서도 예외를 로깅하여 장애 추적 가능하도록 함
            LOG.error("Exception occurred in immediate task execution (no transaction synchronization).", ex);
            throw ex;
        }
    }
}
