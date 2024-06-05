package com.booking.app.services.impl;

import com.booking.app.entity.ConfirmationCode;
import com.booking.app.entity.User;
import com.booking.app.exception.exception.UserNotFoundException;
import com.booking.app.services.ConfirmationCodeService;
import com.booking.app.services.MailSenderService;
import com.booking.app.services.UserService;
import com.booking.app.util.HtmlTemplateUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.booking.app.constant.MailConstants.EMAIL_CONFIRMATION_SUBJECT;
import static com.booking.app.constant.MailConstants.RESET_PASSWORD_SUBJECT;

/**
 * Service implementation for sending emails related to user registration and confirmation.
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
@Log4j2
public class MailSenderServiceImpl implements MailSenderService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UserService userService;
    private final ConfirmationCodeService confirmationCodeService;

    @Override
    public void sendVerificationCode(String email, String language) {
        userService.findByEmail(email)
                .ifPresentOrElse(user -> {
                    ConfirmationCode newCode = ConfirmationCode.createCode();
                    ConfirmationCode existingCode = user.getConfirmationCode();
                    user.setConfirmationCode(newCode);

                    confirmationCodeService.updateConfirmationCode(newCode, existingCode);

                    String templateName = HtmlTemplateUtils.getConfirmationTemplate(language);
                    sendEmail(templateName, EMAIL_CONFIRMATION_SUBJECT, newCode.getCode(), user);
                }, () -> {
                    throw new UserNotFoundException();
                });
    }

    @Override
    public void sendResetCode(String email, String language) {
        userService.findByEmail(email)
                .ifPresentOrElse(user -> {
                            if (userService.isEnabled(user)) {
                                ConfirmationCode newCode = ConfirmationCode.createCode();
                                user.setConfirmationCode(newCode);

                                String templateName = HtmlTemplateUtils.getResetPasswordTemplate(language);
                                sendEmail(templateName, RESET_PASSWORD_SUBJECT, newCode.getCode(), user);
                                confirmationCodeService.save(newCode);
                            }
                        },
                        () -> {
                            throw new UserNotFoundException();
                        });
    }

    /**
     * Sends an email with a confirmation token to the user.
     *
     * @param templateName the HTML template name
     * @param subject      the subject of the email
     * @param token        the confirmation token to be included in the email
     * @param user         the user credentials containing the recipient's email and username
     */
    private void sendEmail(String templateName, String subject, String token, User user) {
        Context context = new Context();
        context.setVariable("token", token);
        context.setVariable("nickname", user.getUsername());

        String process = templateEngine.process(templateName, context);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setSubject(subject);
            helper.setText(process, true);
            helper.setTo(user.getEmail());
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Failed to send confirmation email to {}, reason: {}", user.getEmail(), e.getCause());
        }
    }

}
