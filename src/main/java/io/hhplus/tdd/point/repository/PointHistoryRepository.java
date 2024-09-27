package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.TransactionType;

import java.util.List;

public interface PointHistoryRepository {

    PointHistory insert(long userId, long amount, TransactionType type, long updateMillis);

    List<PointHistory> selectAllByUserId(long userId);

}
