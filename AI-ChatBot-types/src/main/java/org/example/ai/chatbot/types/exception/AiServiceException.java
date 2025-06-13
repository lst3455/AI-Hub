package org.example.ai.chatbot.types.exception;


public class AiServiceException extends RuntimeException {

    private String code;

    public AiServiceException(String code) {
        super();
        this.code = code;
    }

    public AiServiceException(String code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public AiServiceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public AiServiceException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
