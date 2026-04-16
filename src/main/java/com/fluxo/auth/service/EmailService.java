package com.fluxo.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Redefinição de senha";
        String body = "Clique no link abaixo para redefinir sua senha:\n\n"
                + "https://fluxo.ages.pucrs.br/reset-password?token=" + token; //exemplo de url

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("ages@pucrs.br"); // exemplo de email

        mailSender.send(message);

        System.out.println("E-mail enviado com sucesso para: " + to);
    }
}
