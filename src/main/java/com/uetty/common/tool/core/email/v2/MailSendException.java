package com.uetty.common.tool.core.email.v2;

/**
 * @author : Vince
 */
@SuppressWarnings("unused")
public class MailSendException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MailSendException(Throwable cause) {
        super(cause);
    }

    public MailSendException(String message) {
        super(message);
    }
}
