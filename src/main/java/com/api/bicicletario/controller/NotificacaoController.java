package com.api.bicicletario.controller;

import com.api.bicicletario.service.NotificacaoService;
import com.api.bicicletario.model.NotificacaoRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notificacao")
public class NotificacaoController {
    private final NotificacaoService notificacaoService;

    @Autowired
    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @PostMapping("/enviar-email")
    public ResponseEntity<String> enviarEmail(@RequestBody NotificacaoRequest request) {
        String destinatario = request.getDestinatario();
        String assunto = request.getAssunto();
        String mensagem = request.getMensagem();

        notificacaoService.enviarEmail(destinatario, assunto, mensagem);

        return ResponseEntity.ok("Notificação enviada com sucesso.");
    }
}