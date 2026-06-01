package BuissnesManager.BuissnesManager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    



    @Value("${spring.mail.from:${spring.mail.username:no-reply@localhost}}")
    private String fromAddress;

    public void sendMailWithAttachment(String to, String subject, String text, MultipartFile file) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);

        
        helper.addAttachment(file.getOriginalFilename(), new ByteArrayResource(file.getBytes()));

        mailSender.send(message);
    }

    






    public void sendPasswordResetEmail(String to, String username, String resetLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject("Réinitialisation de votre mot de passe");

        String safeName = (username == null || username.isBlank()) ? "" : " " + HtmlUtils.htmlEscape(username);
        String safeLink = HtmlUtils.htmlEscape(resetLink);
        String html = ""
                + "<div style=\"font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#222;line-height:1.5;\">"
                + "<p>Bonjour" + safeName + ",</p>"
                + "<p>Nous avons reçu une demande de réinitialisation du mot de passe associé à votre compte.</p>"
                + "<p>Cliquez sur le bouton ci-dessous pour définir un nouveau mot de passe&nbsp;:</p>"
                + "<p style=\"margin:24px 0;\">"
                + "<a href=\"" + safeLink + "\" "
                + "style=\"background:#6366f1;color:#fff;padding:10px 18px;border-radius:6px;text-decoration:none;display:inline-block;\">"
                + "Réinitialiser mon mot de passe</a>"
                + "</p>"
                + "<p>Ou copiez/collez ce lien dans votre navigateur&nbsp;:<br/>"
                + "<a href=\"" + safeLink + "\">" + safeLink + "</a></p>"
                + "<p>Ce lien expirera dans 30 minutes. Si vous n'êtes pas à l'origine de cette demande, "
                + "vous pouvez ignorer cet e-mail.</p>"
                + "<p>— L'équipe SmartERP</p>"
                + "</div>";

        helper.setText(html, true);
        mailSender.send(message);
    }
}

