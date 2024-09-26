package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointConstant;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    /**
     * 유저 포인트 조회
     * @param id
     * @return
     */
    public UserPoint getUserPoint(long id) {
        UserPoint userPoint = userPointRepository.selectById(id);

        if (userPoint == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        return userPoint;
    }

    /**
     * 유저 포인트 이력 조회
     * @param id
     * @return
     */
    public List<PointHistory> getUserPointHistoryList(long id) {
        getUserPoint(id);
        return pointHistoryRepository.selectAllByUserId(id);
    }

    /**
     * 포인트 충전
     * @param id
     * @param amount
     * @return
     */
    public synchronized UserPoint chargePoint(long id, long amount) {
        if (amount < PointConstant.MIN_CHARGE_POINT) {
            throw new RuntimeException("최소 충전 포인트는 " + PointConstant.MIN_CHARGE_POINT + "P 이상이어야 합니다.");
        }

        UserPoint userPoint = getUserPoint(id);

        long chargedPoint = userPoint.point() + amount;
        if (chargedPoint > PointConstant.MAX_POINT) {
            throw new RuntimeException("충전 후 보유 포인트는 " + PointConstant.MAX_POINT + "P를 초과할 수 없습니다.");
        }

        UserPoint updatedUserPoint = userPointRepository.insertOrUpdate(id, userPoint.point() + amount);
        if (updatedUserPoint == null) {
            throw new RuntimeException("포인트 충전 오류");
        }

        pointHistoryRepository.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return updatedUserPoint;
    }

    /**
     * 포인트 사용
     * @param id
     * @param amount
     * @return
     */
    public synchronized UserPoint usePoint(long id, long amount) {
        if (amount < PointConstant.MIN_USE_POINT) {
            throw new RuntimeException("최소 사용 포인트는 " + PointConstant.MIN_USE_POINT + "P 이상이어야 합니다.");
        }

        UserPoint userPoint = getUserPoint(id);

        if (!canUsePoint(userPoint.point(), amount)) {
            throw new RuntimeException("보유 포인트가 부족합니다.");
        }

        long usedPoint = userPoint.point() - amount;
        if (usedPoint < PointConstant.MIN_POINT) {
            throw new RuntimeException("사용 후 잔여 포인트는 " + PointConstant.MIN_POINT + "P 이상이어야 합니다.");
        }

        UserPoint updatedUserPoint = userPointRepository.insertOrUpdate(id, usedPoint);
        if (updatedUserPoint == null) {
            throw new RuntimeException("포인트 사용 오류");
        }

        pointHistoryRepository.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return updatedUserPoint;
    }

    /**
     * 포인트 사용 가능 여부
     * @param initPoint
     * @param amount
     * @return
     */
    private boolean canUsePoint(long initPoint, long amount) {
        return initPoint - amount >= 0;
    }

}
