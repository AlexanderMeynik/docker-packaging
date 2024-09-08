package ru.edu.spbstu.mail;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.edu.spbstu.model.Language;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    private static GreenMail testSmtp;

    @BeforeAll
    public static void smtpInit() {
        testSmtp = new GreenMail(ServerSetupTest.SMTP);
        testSmtp.setUser("hellotest", "hellotest");
        testSmtp.start();
    }

    @Test
    void send() throws MessagingException {
        String email = "test@email.com";
        String text = "email text";
        String subject = "Reset password";
        emailService.send(email, text, Language.ENGLISH);

        MimeMessage[] messages = testSmtp.getReceivedMessages();
        Assertions.assertEquals(1, messages.length);
        Assertions.assertEquals(subject, messages[0].getSubject());
        String body = GreenMailUtil.getBody(messages[0]).replaceAll("=\r?\n", "");
        Assertions.assertEquals(text, body);
    }

    @AfterAll
    public static void cleanup() {
        testSmtp.stop();
    }
}