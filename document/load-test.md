# 부하테스트 환경 설정 정리

## 전체 구성

```text
k6 → Server(부하발생) → InfluxDB → Grafana
```

* k6 : 부하테스트 실행(요청 수 / 응답시간 / 실패율 측정)
* Server
    - Actuator: 서버 상태를 외부에서 볼 수 있게 endpoint api 제공(/actuator/health, /metrics 등)
    - Micrometer: Actuator 내부에서 JVM, DB, HTTP 요청 등의 실제 메트릭 수집 담당 -> 결과가 InfluxDB에 export
* InfluxDB : 시계열(Time Series) 데이터 저장소(k6 결과 + 서버 메트릭 저장)
* Grafana : InfluxDB 데이터 시각화

---

# 1. Docker 실행 상태 확인

## 실행 컨테이너 확인

```bash
docker ps
```

정상 예시:

```text
grafana/grafana
influxdb:1.8
mysql:8.0
redis:latest
apache/kafka:3.7.0
```

---

# 2. InfluxDB 설정

## gradle
```groovy
implementation("io.micrometer:micrometer-registry-influx")
```

## 컨테이너 접속

```bash
docker exec -it influxdb influx
```

## 데이터베이스 생성

```sql
CREATE DATABASE k6;
SHOW DATABASES;
```

정상적으로 `k6` DB가 보이면 완료.

## 종료

```sql
exit
```

---

# 3. Grafana 접속

```text
http://localhost:3000
```

기본 계정:

```text
id: admin
pw: admin
```

---

# 4. Grafana DataSource 설정

## 이동 경로

```text
Connections → Data Sources → Add data source
```

## InfluxDB 선택

설정값:

### Query Language

```text
InfluxQL
```

### URL

Mac Docker 기준:

```text
http://host.docker.internal:8086
```

### Database

```text
k6
```

### User / Password

비워둠

---

## 저장

```text
Save & Test
```

정상 메시지:

```text
datasource is working
```

---

# 5. Spring Boot actuator 설정

## gradle

```groovy
	implementation ("org.springframework.boot:spring-boot-starter-actuator")
```
## application.yml

```yml
management:
  endpoints:
    web:
      exposure:
        include: "*"

  endpoint:
    health:
      show-details: always

  metrics:
    tags:
      application: hhplus-server

    export:
      influx:
        enabled: true
        uri: http://localhost:8086
        db: metrics
        step: 10s
```
각의미
- exposure.include
    - → /actuator/** 전체 공개
- health.show-details
- → DB/Redis 상태 상세 출력
    - metrics.tags.application
    - → Grafana에서 앱 이름 기준 필터 가능
- metrics.export.influx
    - → Micrometer 메트릭을 InfluxDB로 주기 전송
---

# 6. actuator 상태 확인

## health 확인

```text
http://localhost:8080/actuator/health
```

정상 예시:

```json
{
  "status": "UP"
}
```

---

## metrics 확인

```text
http://localhost:8080/actuator/metrics
```

정상적으로 metric 목록이 나오면 성공.

예시

```text
http.server.requests
jvm.memory.used
hikaricp.connections
process.cpu.usage
```

---

# 7. k6 실행 방법

## 일반 실행 (Grafana 저장 안됨)

```bash
k6 run reservation-test.js
```

→ 콘솔 출력만 됨.

---

## InfluxDB 연동 실행 (중요)

```bash
K6_OUT=influxdb=http://localhost:8086/k6 k6 run reservation-test.js
```

이렇게 실행해야:

```text
k6
 → InfluxDB 저장
 → Grafana 시각화 가능
```

---

# 8. Grafana Dashboard 생성

## 생성 경로

```text
Dashboards → New Dashboard → Add Visualization
```

## DataSource 선택

```text
InfluxDB
```
## 실행화면

![img.png](img.png)

- k6를 실행한 콘솔에서의 결과가 시각화되어 나타난다.
---

# 9. Grafana 기본 대쉬보드 구성

## 평균 응답속도

### Measurement

```text
http_req_duration
```

### Query

```text
mean(value)
```

### Group By

```text
time($__interval)
```

---

## p95 응답속도

### Measurement

```text
http_req_duration
```

### Query

```text
percentile(value,95)
```

---

## TPS

### Measurement

```text
http_reqs
```

### Query

```text
sum(value)
```

### Group By

```text
time(1s)
```

---

## 실패율

### Measurement

```text
http_req_failed
```

### Query

```text
mean(value)
```