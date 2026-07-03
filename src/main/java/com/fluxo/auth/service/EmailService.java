package com.fluxo.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String emailFrom;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Redefinição de senha";
        String body = "Clique no link abaixo para redefinir sua senha:\n\n"
                + buildResetPasswordUrl(token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(emailFrom);

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
        message.setFrom(emailFrom);

        mailSender.send(message);

        System.out.println("E-mail de início de horas enviado com sucesso para: " + to);
    }
    
    public void sendHoursStoppedEmail(String to, String studentName, Instant entryTime, Instant exitTime, Integer totalTimeSeconds, String activities) {
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
                + "Atividades realizadas: " + activities + "\n\n"
                + "Controle de Fluxo de Acesso da AGES.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(emailFrom);

        mailSender.send(message);

        System.out.println("E-mail de encerramento de horas enviado com sucesso para: " + to);
    }

    public void sendOpenHoursReminderEmail(String to, String studentName, Instant entryTime, Instant reminderTime) {
        String subject = "Lembrete: Suas horas ainda estao abertas";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        OffsetDateTime startDateTime = OffsetDateTime.ofInstant(entryTime, ZoneOffset.of("-03:00"));
        OffsetDateTime currentDateTime = OffsetDateTime.ofInstant(reminderTime, ZoneOffset.of("-03:00"));

        Duration duration = Duration.between(entryTime, reminderTime);
        long totalHours = duration.toHours();
        long minutes = duration.minusHours(totalHours).toMinutes();
        String formattedOpenDuration = String.format("%dh%02dmin", totalHours, minutes);

        String body = "Ola, " + studentName + "!\n\n"
                + "Seu registro de horas continua aberto.\n\n"
                + "Horario de inicio: " + startDateTime.format(formatter) + "\n"
                + "Horario atual: " + currentDateTime.format(formatter) + "\n"
                + "Tempo aberto: " + formattedOpenDuration + "\n\n"
                + "Se voce concluiu a atividade, volte ao Fluxo e encerre o registro.\n\n"
                + "Controle de Fluxo de Acesso da AGES.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(emailFrom);

        mailSender.send(message);

        System.out.println("E-mail de lembrete de horas abertas enviado com sucesso para: " + to);
    }
    private String buildResetPasswordUrl(String token) {
        return frontendBaseUrl.replaceAll("/$", "") + "/reset-password?token=" + token;
    }
}
