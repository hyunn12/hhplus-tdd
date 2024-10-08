# 항해 플러스 백엔드
## 1주차 TDD

### `Default`

- `/point` 패키지 (디렉토리) 내에 `PointService` 기본 기능 작성
- `/database` 패키지의 구현체는 수정하지 않고, 이를 활용해 기능을 구현
- 각 기능에 대한 단위 테스트 작성

> 총 4가지 기본 기능 (포인트 조회, 포인트 충전/사용 내역 조회, 충전, 사용) 을 구현합니다.
>

### `Step 1`

- 포인트 충전, 사용에 대한 정책 추가 (잔고 부족, 최대 잔고 등)
- 동시에 여러 요청이 들어오더라도 순서대로 (혹은 한번에 하나의 요청씩만) 제어될 수 있도록 리팩토링
- 동시성 제어에 대한 통합 테스트 작성



### `Step 2`

- 동시성 제어 방식에 대한 분석 및 보고서 작성 ( **README.md** )

---

## 동시성 제어 방식

### 현재 적용한 방법
`PointService` 클래스 내에서 메서드에 `synchronized` 를 이용해 동시성 문제를 해결하고 있음
한 번에 하나의 스레드만 해당 메서드에 접근할 수 있도록 보장

### 단점
- 성능 저하
  - 여러 스레드가 동시에 처리되지 못해서 성능 저하가 발생할 수 있음
- 글로벌 락
  - 다른 유저의 작업까지 지연될 수 있음
- 한 프로세스 내에서만 동시성 제어 가능
  - 두 개 이상의 프로세스 이용 시 실질적으로 문제가 해결되지 않을 수 있음

### 개선 방안
> `ReentrantLock`, `ConcurrentHashMap` 을 이용한 동시성 제어 방식 고려

- 해당 방식은 코드레벨에서 동시성 제어를 하는 방식으로 직접 lock을 제어할 수 있어 더 세밀한 제어가 가능한 방법
- `ReentrantLock` 은 **뮤텍스 (Mutex)** 방식 중 하나라고 함
- 유저별로 동시성 제어가 가능해 글로벌 락 문제가 해결되고 성능 저하가 줄어듦

### 추가적으로 알아볼 것
- `ReentrantLock`, `ConcurrentHashMap` 을 이용한 동시성 제어 방식 고려
- `Mutex`와 `Semaphore`