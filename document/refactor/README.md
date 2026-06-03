# Refactor Documents

리팩토링 단계별 산출물을 정리하는 문서 디렉터리이다.

각 STEP은 코드 변경만으로 끝내지 않고, 리팩토링 내용, 관련 개념, 테스트 결과를 함께 남긴다. ERD나 인프라 흐름이 바뀌는 경우 Mermaid diagram을 사용해 기존 문서도 갱신한다.

## 문서 목록

| Step | 문서 | 주제 |
| --- | --- | --- |
| REFACTOR-1 | `refactor-01-structure.md` | 프로젝트 전체 구조 진단 |
| REFACTOR-2 | `refactor-02-server-structure.md` | 서버 구조 재정리 |
| REFACTOR-3 | `refactor-03-clean-architecture.md` | 클린 아키텍처 구조 개선 |
| REFACTOR-4 | `refactor-04-db-domain.md` | DB 정합성 및 도메인 모델 개선 |
| REFACTOR-5 | `refactor-05-concurrency.md` | 동시성 제어 개선 |
| REFACTOR-6 | `refactor-06-redis-lock.md` | Redis 분산락 개선 |
| REFACTOR-7 | `refactor-07-cache.md` | Redis 캐싱 전략 개선 |
| REFACTOR-8 | `refactor-08-event.md` | 이벤트 기반 구조 개선 |
| REFACTOR-9 | `refactor-09-kafka.md` | Kafka 확장 설계 개선 |
| REFACTOR-10 | `refactor-10-operation.md` | 장애 대응/운영 문서 개선 |

## 작성 기준

- 변경 전 구조와 변경 후 구조를 함께 기록한다.
- 리팩토링으로 바뀐 책임 경계와 의존 방향을 설명한다.
- 적용한 개념은 과제 맥락에 맞춰 짧게 정리한다.
- 테스트 명령과 검증 결과를 남긴다.
- DB 구조 변경은 `document/erd.md`에도 반영한다.
- 인프라 흐름 변경은 `document/infra.md`에도 반영한다.
- Kafka 관련 변경은 `document/kafka.md`에도 반영한다.
- 부하 테스트 또는 k6 시나리오 변경은 `document/load-test.md`에도 반영한다.

## 권장 템플릿

```markdown
# [REFACTOR-N] 제목

## 목표

## 변경 전

## 변경 내용

## 변경 후

## 관련 개념

## 테스트

## 남은 작업
```
