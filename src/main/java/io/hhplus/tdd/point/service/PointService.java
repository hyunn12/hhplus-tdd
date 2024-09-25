package io.hhplus.tdd.point.service;

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
    public UserPoint point(long id) {
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
    public List<PointHistory> history(long id) {
        point(id);
        return pointHistoryRepository.selectAllByUserId(id);
    }

    /**
     * 포인트 충전
     * @param id
     * @param amount
     * @return
     */
    public UserPoint charge(long id, long amount) {
        UserPoint userPoint = point(id);

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
    public UserPoint use(long id, long amount) {
        UserPoint userPoint = point(id);

        if (!canUsePoint(userPoint.point(), amount)) {
            throw new RuntimeException("보유 포인트가 부족합니다.");
        }

        UserPoint updatedUserPoint = userPointRepository.insertOrUpdate(id, userPoint.point() - amount);
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
