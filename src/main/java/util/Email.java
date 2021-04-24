package util;

public class Email {

    private String subject;
    private String to;
    private String from;
    private String body;


    public Email(String subject, String to, String from, String body) {
        this.subject = subject;
        this.to = to;
        this.from = from;
        this.body = body;

    }

    public String getSubject() {
        return subject;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Email{" +
                "Subject='" + subject + '\'' +
                ", To='" + to + '\'' +
                ", From='" + from + '\'' +
                ", Body='" + body + '\'' +
                '}';
    }
}
