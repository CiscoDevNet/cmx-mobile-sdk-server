package com.cisco.cmxmobile.services.mail;

import java.util.Date;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.cmxmobile.utils.EmailProperties;

public class SendEmail {
    
    private final String toAddress;
   
    private final String fromAddress;
    
    private static SendEmailService emailService = new SendEmailService();
    
    public SendEmail() {
        toAddress = null;
        fromAddress = null;
    }

    public SendEmail(String toAdd, String fromAdd) {
        toAddress = toAdd;
        fromAddress = fromAdd;
    }

    public final void sendMail(String subject, String body) {
        SendEmailThread emailThread = new SendEmailThread(toAddress, fromAddress, subject, body);
        emailService.runTask(emailThread);
    }
    
    private class SendEmailThread implements Runnable {
        
        private final String toAddress;
        
        private final String fromAddress;
        
        private final String mailSubject;
        
        private final String mailBody;
        
        public SendEmailThread(String toAddr, String fromAddr, String subject, String body) {
            toAddress = toAddr;
            fromAddress = fromAddr;
            mailSubject = subject;
            mailBody = body;
        }
        
        public void run() {
            try {
                Session mailSession = null;
                if (EmailProperties.getInstance().getEmailUsername() != null) {
                    mailSession = Session.getInstance(EmailProperties.getInstance().getProperties(), new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(EmailProperties.getInstance().getEmailUsername(), EmailProperties.getInstance().getEmailPassword());
                        }
                    });
                } else {
                    mailSession = Session.getInstance(EmailProperties.getInstance().getProperties());
                }
                mailSession.setDebug(true);
                Message msg = new MimeMessage(mailSession);
                msg.setFrom(new InternetAddress(fromAddress));
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
                msg.setSentDate(new Date());
                msg.setSubject(mailSubject);
                msg.setText(mailBody);
                Transport.send(msg);
            }
            catch (Exception smtpServerException) {
                Logger logger = LoggerFactory.getLogger(SendEmail.class);
                logger.error("Unable to send mail");
            }
        }
    }
}
