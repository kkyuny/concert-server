package kr.hhplus.be.server.global.config.kafka.producer.controller;

import kr.hhplus.be.server.global.config.kafka.producer.dto.MemberDto;
import kr.hhplus.be.server.global.config.kafka.producer.service.ProducerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/producer")
public class ProducerController {
    private final ProducerService producerService;

    public ProducerController(ProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping("/message/create")
    public void messageCreate(@RequestBody MemberDto dto){
        producerService.kafkaMessageCreate(dto);
    }
    @PostMapping("/message/create/key/{id}")
    public void messageCreateKey(@RequestBody MemberDto dto, @PathVariable Long id){
        producerService.kafkaMessageKeyCreate(dto, id);
    }

}
