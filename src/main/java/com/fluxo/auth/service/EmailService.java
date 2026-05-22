package com.fluxo.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String EMAIL_FROM = "pucrsages@gmail.com";

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Redefinição de senha";
        String body = "Clique no link abaixo para redefinir sua senha:\n\n"
                + "http://localhost:5173/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(EMAIL_FROM);

        mailSender.send(message);

        System.out.println("E-mail enviado com sucesso para: " + to);
    }

    public void sendHoursStartedEmail(String to, String studentName, Instant entryTime) {
        String subject = "Início de Registro de Horas";
        
        // Converte o horário para formato legível (DD/MM/YYYY HH:mm:ss)
        OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(entryTime, ZoneOffset.of("-03:00"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedTime = offsetDateTime.format(formatter);

        String body = "Olá, " + studentName + "!\n\n"
                + "Você começou a hora extra em " + formattedTime + "\n\n"
                + "Controle de Fluxo de Acesso da AGES.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(EMAIL_FROM);

        mailSender.send(message);

        System.out.println("E-mail de início de horas enviado com sucesso para: " + to);
    }

    public void sendHoursStoppedEmail(String to, String studentName, Instant entryTime, Instant exitTime, Integer totalTimeSeconds) {
        String subject = "Encerramento de Registro de Horas";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        OffsetDateTime startDateTime = OffsetDateTime.ofInstant(entryTime, ZoneOffset.of("-03:00"));
        OffsetDateTime endDateTime = OffsetDateTime.ofInstant(exitTime, ZoneOffset.of("-03:00"));
        String formattedStartTime = startDateTime.format(formatter);
        String formattedEndTime = endDateTime.format(formatter);

        long hours = totalTimeSeconds / 3600;
        long minutes = (totalTimeSeconds % 3600) / 60;
        long seconds = totalTimeSeconds % 60;
        String formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        String body = "Olá, " + studentName + "!\n\n"
                + "Seu registro de horas foi encerrado.\n\n"
                + "Horário de Início: " + formattedStartTime + "\n"
                + "Horário de Término: " + formattedEndTime + "\n"
                + "Duração Total: " + formattedDuration + "\n\n"
                + "Controle de Fluxo de Acesso da AGES.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(EMAIL_FROM);

        mailSender.send(message);

        System.out.println("E-mail de encerramento de horas enviado com sucesso para: " + to);
    }
}
