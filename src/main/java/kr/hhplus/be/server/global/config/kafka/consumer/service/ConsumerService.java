package kr.hhplus.be.server.global.config.kafka.consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConsumerService {
    private final ObjectMapper objectMapper;
    public ConsumerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /*
        - 메시지 송수신 기본 실습(하나의 컨슈머그룹에 하나의 컨슈머가 1개의 토픽을 listen)
        - 컨슈머 서버 down → 메시지 발행 → 재시작 이후 메시지 수신 여부 확인
        - 재시작 후 auto-offset-reset 설정(earliest, lastest)에 따라 메세지를 컨슘한다.
    */
    @KafkaListener(
            topics = "member-topic", // producer에서 토픽 설정 후 send
            groupId = "${spring.kafka.consumer.member-topic-log-group-id}", // yml에서 불러오기
            containerFactory = "kafkaListener" // config 설정 값
    )
    public void consumer1(String message) {
        System.out.println("member-topic-log-group 메시지 수신 : " + message);
    }

    /*
        - 한대의 서버에서 여러개의 토픽구독도 가능 -> consumer1과 consumer2에서 동시에 메세지를 컨슘
        - producer에서는 2개의 topic을 send 해야한다.
    */
    @KafkaListener(
            topics = "order-topic",
            groupId = "${spring.kafka.consumer.order-topic-log-group-id}",
            containerFactory = "kafkaListener"
    )
    public void consumer2(String message) {
        System.out.println("order-topic-log-group 메시지 수신 : " + message);
    }

    /*
        - 2대의 컨슈머서버가 같은 그룹ID를 가지고 같은 topic을 listen 경우
        - 2대의 서버 중 1대만 메세지를 컨슘한다.(파티션에 메세지가 저장되어 컨슈머 그룹 별로 독립적으로 메세지가 컨슘된다.)
    */
    @KafkaListener(
            topics = "member-topic",
            groupId = "${spring.kafka.consumer.member-topic-log-group-id}",
            containerFactory = "kafkaListener"
    )
    public void consumer3(String message) {
        System.out.println("member-topic 메시지 수신 : " + message);
    }

    /*
        - 키값을 통한 파티션별 메시지 순서 보장 확인 : 서버down -> 메시지 발행 -> 서버up 테스트 진행
        - 같은 key → 같은 파티션 저장 => 메세지 순서가 보장된다.
     */
    @KafkaListener(
            topics = "member-topic",
            groupId = "${spring.kafka.consumer.member-topic-log-group-id}",
            containerFactory = "kafkaListener"
    )
    public void consumer4(
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            String message
    ) {
        System.out.println("key 값 : " + key);
        System.out.println("컨슈머 메시지 수신 : " + message);
    }

    /*
        - 실패된 메시지 재처리
        - 기본 모드 : auto-commit
    */
    @KafkaListener(
            topics = "member-topic",
            groupId = "${spring.kafka.consumer.member-topic-log-group-id}",
            containerFactory = "kafkaListener"
    )
    public void consumer5(
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            String message
    ) {
        try {
            System.out.println("컨슈머 메시지 수신1 : " + message);
            // db 작업 및 실패가능성 있는 코드
            if(key.equals("1")){
                throw new IllegalArgumentException("예상치 못한 예외 발생");
            }
            System.out.println("처리 완료");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /*
        - 수동 커밋 모드로 변경
     */
    @KafkaListener(
            topics = "member-topic",
            groupId = "${spring.kafka.consumer.member-topic-log-group-id}",
            containerFactory = "kafkaListener"
    )
    public void consumer6(
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            String message, Acknowledgment ack
    ) {
        try {
            System.out.println("컨슈머 메시지 수신1 : " + message);
            // db 작업 및 실패가능성 있는 코드
            if(key.equals("1")){
                throw new IllegalArgumentException("예상치 못한 예외 발생");
            }
            System.out.println("처리 완료");
            ack.acknowledge(); // 수동 커밋
        }catch (Exception e){
            e.printStackTrace();
            // DLQ 예시: 실패 메시지를 별도 토픽으로 발행
            // kafkaTemplate.send("member-topic-dlq", key, message);
        }
    }
}
