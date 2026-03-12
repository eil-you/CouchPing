# 🛋️ CouchPing (카우치핑)

[![couchping CI](https://github.com/f-lab-edu/couchping/actions/workflows/ci.yml/badge.svg)](https://github.com/f-lab-edu/couchping/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=f-lab-edu_couchping&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=f-lab-edu_couchping)

> 기존 **couchping** 프로젝트에서 '신뢰 기반의 C2C 숙박 공유 플랫폼'으로 진화 중인 프로젝트입니다.

💡 **Project Vision: Why Couchsurfing?**
단순한 숙박 업소를 넘어, 개인 간의 신뢰를 바탕으로 전 세계 여행자와 호스트를 연결합니다.

- **B2C → C2C**: 기업의 상품이 아닌 개인의 공간과 문화를 공유합니다.
- **Trust-First**: 자기소개, 구사 언어, 라이프스타일 기반의 매칭으로 안전한 공유 환경을 구축합니다.

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.2.2
- **Language**: Java 21
- **Database**: PostgreSQL (Core), Redis (Caching/Token/Distributed Lock)
- **MSA**: Gradle Multi-module
- **Security**: Spring Security, OAuth2, JWT
- **DevOps**: Docker, GitHub Actions, SonarCloud

## 📂 Project Structure

- `module-api`: API 서비스 모듈 (User, Reservation 등)
- `module-common`: 핵심 비즈니스 로직, 공용 예외 처리 및 공통 모델
- `module-infra`: 외부 인프라 연동 (Discovery, Gateway)

## 🚀 Key Features & Architectural Decisions

### 1. 신뢰 기반의 아키텍처 설계 (New)
- **Extensible User Profile**: 사용자의 언어 능력, 라이프스타일 태그 등 복잡한 개인 정보를 효율적으로 관리하기 위한 1:N 정규화 설계를 했습니다.
- **Matching Workflow**: 단순 결제가 아닌 '요청-수락-완료'로 이어지는 호스트 의사 결정 프로세스를 구현했습니다.

### 2. 분산 락(Distributed Lock)을 통한 예약 정합성 보장 및 성능 최적화
- **우수 게스트 즉시 예약 (Instant Book)**: 호스트가 허용한 방에 대해, 검증된 우수 게스트(평점 3.0 이상)가 수동 승인 절차 없이 예약과 동시에 확정(CONFIRMED)받는 비즈니스 모델을 추가했습니다. 
- **문제 상황**: 분산 환경에서 위와 같은 성수기 인기 숙소(즉시 예약 활성화)에 대한 예약 요청이 동시에 몰릴 경우, 선착순 트래픽 경쟁으로 인한 중복 예약(Overbooking) 발생 리스크가 존재했습니다.
- **해결 방안**: Redisson Distributed Lock을 도입하여 동일 숙소에 대한 예약 요청을 직렬화하고, `예약 가능 여부(날짜 중복) 확인 -> 예약 생성 및 상태 확정`까지 이어지는 하나의 예약 프로세스의 원자성(Atomicity)을 보장하는 커스텀 `@DistributedLock` AOP를 구현했습니다.
- **기술 선택 근거**: DB 비관적 락(Pessimistic Lock) 대비 커넥션 점유 시간을 줄이고, 시스템 부하가 심한 스핀 락(Spin Lock) 대신 Pub/Sub 방식을 통해 효율적인 락 대기가 가능한 Redisson을 선택하여 시스템 성능을 최적화했습니다.
- **SpEL 동적 키 기반 병목 해결**: `CustomSpringELParser` 헬퍼 클래스를 자체 구현하여, 메서드 인자로 넘어온 숙소 객체(`request.roomId()`)를 파싱해 고유한 락 키(`room:lock:{id}`)를 동적으로 생성함으로써, 다른 숙소의 대기열까지 블로킹해버리는 전역 병목 현상을 완벽하게 제거했습니다.

### 3. MSA (Microservice Architecture)
- **Service Discovery (Eureka)**: 각 서비스의 위치를 동적으로 관리하여 확장성을 확보했습니다.
- **API Gateway**: 단일 진입점을 통해 라우팅 및 횡단 관심사(인증/인가)를 처리합니다.

## 📦 Project Structure (Multi-Module)
- **module-api**: 사용자 프로필 관리, 매칭 요청, 리뷰 시스템 API
- **module-common**: 엔티티 유틸리티 및 공통 예외 처리
- **module-infra**: Eureka Discovery, API Gateway 설정 및 외부 연동

## 🏁 Getting Started

```bash
# 리포지토리 클론
git clone https://github.com/f-lab-edu/couchping.git

# 빌드 및 실행
./gradlew bootRun
```

## 🤝 Contributing

본 프로젝트의 기여 가이드는 [CONTRIBUTING.md](./CONTRIBUTING.md)를 확인해주세요.
커밋 메시지 규칙 및 브랜치 전략(Git-flow)에 대한 상세한 가이드가 포함되어 있습니다.
