package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.PointConstant;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private UserPointRepository userPointRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    @DisplayName("존재하는 유저인 경우 포인트 조회가 성공")
    void testUserPointsExist() {
        long id = 1L;
        long amount = 1000L;

        UserPoint userPoint = new UserPoint(id, amount, System.currentTimeMillis());
        when(userPointRepository.selectById(id)).thenReturn(userPoint);

        UserPoint result = pointService.getUserPoint(id);

        assertEquals(id, result.id());
        assertEquals(amount, result.point());
    }

    @Test
    @DisplayName("존재하지 않는 유저인 경우 예외 발생")
    void testUserPointsNotExist() {
        long id = 100L;

        when(userPointRepository.selectById(id)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            pointService.getUserPoint(id);
        });
    }

    @Test
    @DisplayName("존재하는 내역 조회")
    void testUserPointHistoryExist() {
        long id = 1L;

        UserPoint userPoint = new UserPoint(id, 1500L, System.currentTimeMillis());
        when(userPointRepository.selectById(id)).thenReturn(userPoint);

        PointHistory history1 = new PointHistory(1L, id, 1000L, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory history2 = new PointHistory(2L, id, 500L, TransactionType.USE, System.currentTimeMillis());
        List<PointHistory> historyList = List.of(history1, history2);

        when(pointHistoryRepository.selectAllByUserId(id)).thenReturn(historyList);

        List<PointHistory> result = pointService.getUserPointHistoryList(id);

        assertNotNull(result);
        assertEquals(historyList.size(), result.size());

        assertEquals(historyList.get(0).id(), result.get(0).id());
        assertEquals(historyList.get(1).id(), result.get(1).id());
        assertEquals(historyList.get(0).userId(), result.get(0).userId());
        assertEquals(historyList.get(1).userId(), result.get(1).userId());
        assertEquals(historyList.get(0).type(), result.get(0).type());
        assertEquals(historyList.get(1).type(), result.get(1).type());
    }

    @Test
    @DisplayName("존재하지 않는 유저의 내역을 조회하는 경우 예외 발생")
    void testUserPointHistoryNotExist() {
        long id = 100L;

        when(userPointRepository.selectById(id)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            pointService.getUserPointHistoryList(id);
        });
    }

    @Test
    @DisplayName("정상적인 포인트 충전")
    void testUserPointsCharge() {
        long id = 1L;
        long initPoint = 2000L;
        long chargePoint = 500L;

        UserPoint userPoint = new UserPoint(id, initPoint, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(id, initPoint + chargePoint, System.currentTimeMillis());

        when(userPointRepository.selectById(id)).thenReturn(userPoint);
        when(userPointRepository.insertOrUpdate(id, initPoint + chargePoint)).thenReturn(updatedUserPoint);

        UserPoint result = pointService.chargePoint(id, chargePoint);

        assertEquals(id, result.id());
        assertEquals(initPoint + chargePoint, result.point());
    }

    @Test
    @DisplayName("정상적인 포인트 사용")
    void testUserPointsUse() {
        long id = 1L;
        long initPoint = 2000L;
        long usePoint = 500L;

        UserPoint userPoint = new UserPoint(id, initPoint, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(id, initPoint - usePoint, System.currentTimeMillis());

        when(userPointRepository.selectById(id)).thenReturn(userPoint);
        when(userPointRepository.insertOrUpdate(id, initPoint - usePoint)).thenReturn(updatedUserPoint);

        UserPoint result = pointService.usePoint(id, usePoint);

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals(initPoint - usePoint, result.point());
    }

    @Test
    @DisplayName("보유 포인트가 부족한 경우 예외 발생")
    void testUserPointsUseInsufficient() {
        long id = 1L;
        long initPoint = 100L;
        long usePoint = 500L;

        UserPoint userPoint = new UserPoint(id, initPoint, System.currentTimeMillis());

        when(userPointRepository.selectById(id)).thenReturn(userPoint);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pointService.usePoint(id, usePoint);
        });

        assertEquals("보유 포인트가 부족합니다.", exception.getMessage());
    }


    @Test
    @DisplayName("충전 포인트가 최소 충전 포인트 미만인 경우 예외 발생")
    void testChargeBelowMinChargePoint() {
        long userId = 1L;
        long chargeAmount = PointConstant.MIN_CHARGE_POINT - 1;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pointService.chargePoint(userId, chargeAmount);
        });

        assertEquals("최소 충전 포인트는 " + PointConstant.MIN_CHARGE_POINT + "P 이상이어야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("충전 후 포인트가 최대 보유 포인트를 초과하는 경우 예외 발생")
    void testChargeOverMaxPoint() {
        long userId = 1L;
        long initPoint = PointConstant.MAX_POINT - 500L;
        long chargeAmount = 1000L;

        when(userPointRepository.selectById(userId)).thenReturn(new UserPoint(userId, initPoint, System.currentTimeMillis()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pointService.chargePoint(userId, chargeAmount);
        });

        assertEquals("충전 후 보유 포인트는 " + PointConstant.MAX_POINT + "P를 초과할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사용 포인트가 최소 사용 포인트 미만인 경우 예외 발생")
    void testUseBelowMinUsePoint() {
        long userId = 1L;
        long useAmount = PointConstant.MIN_USE_POINT - 1;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pointService.usePoint(userId, useAmount);
        });

        assertEquals("최소 사용 포인트는 " + PointConstant.MIN_USE_POINT + "P 이상이어야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사용 후 포인트가 최소 보유 포인트 미만인 경우 예외 발생")
    void testUseBelowMinRemainingPoint() {
        long userId = 1L;
        long initPoint = 1000L;
        long useAmount = 990L;

        when(userPointRepository.selectById(userId)).thenReturn(new UserPoint(userId, initPoint, System.currentTimeMillis()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pointService.usePoint(userId, useAmount);
        });

        assertEquals("사용 후 잔여 포인트는 " + PointConstant.MIN_POINT + "P 이상이어야 합니다.", exception.getMessage());
    }

}