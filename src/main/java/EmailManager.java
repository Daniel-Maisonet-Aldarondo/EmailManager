import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;


public class EmailManager {
    private static final String APPLICATION_NAME = "Termail Email Manager";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";


    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
		final String user = "me";

//        List<Message> messages = retrieveMessageQuery(service, "", user);
////        List<Message> messages = list.getMessages();
//        displayMessageHeaders(service, messages, user);
        boolean running = true;
        while(running) {
            running = run(service, user);
        }



    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = EmailManager.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    //get list of messages based on gmail query ---- To: From: Subject: before: after: etc
    private static List<Message> retrieveMessageQuery(Gmail service, String query, String user) throws IOException {
        ListMessagesResponse response = service.users().messages().list(user).setQ(query).execute();
        List<Message> messages = new ArrayList<>();
        while(response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if(response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(user).setPageToken(pageToken).setQ(query).execute();
            } else {
                break;
            }
        }
        return messages;
    }

    private static void deleteMessages(Gmail service, List<Message> messages, String user) throws IOException {
        int numberOfMessages = messages.size();
        for(Message m : messages) {
            service.users().messages().delete(user, m.getId()).execute();
        }
        System.out.println("Messages deleted: " + numberOfMessages);
    }

    private static void trashMessages(Gmail service, List<Message> messages, String user) throws IOException {
        int numberOfMessages = messages.size();
        for(Message m : messages) {
            service.users().messages().trash(user, m.getId()).execute();
        }
        System.out.println("Messages trashed: " + numberOfMessages);
    }

    private static void displayMessageHeaders(Gmail service, List<Message> messages, String user) throws IOException {
        System.out.println("messages : " + messages.size());
        for(Message message : messages) {
            Message msg = service.users().messages().get(user,message.getId()).setFormat("full").execute();
            for(MessagePartHeader header : msg.getPayload().getHeaders()) {
                if(header.getName().contains("Date")
                        || header.getName().contains("From")
                        || header.getName().contains("To")
                        || header.getName().contains("Subject")) {
                    System.out.println(header.getName() + ":" + header.getValue());
                }
            }
            System.out.println("============================================");
        }
    }

    private static String getQuery() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter your search query: ");
        return sc.nextLine();
    }

    private static boolean run(Gmail service, String user) throws IOException{
        List<Message> messages;
        System.out.println("1. List messages with query\n" +
                "2.Delete messages with query\n" +
                "3. Trash messages with query\n" +
                "4.Close program");
        System.out.println("Please make a selection... ");
        java.util.Scanner sc = new java.util.Scanner(System.in);
        String selection = sc.nextLine();

        switch(selection) {
            case "1":
                String query = getQuery();
                messages = retrieveMessageQuery(service, query, user);
                displayMessageHeaders(service,messages,user);
                return true;
            case "2":
                //delete messages with quer
                return true;
            case "3":
                //trash messages with q
                return true;
            case "4":
                System.out.println("Bye Bye");
                return false;
            default:
                System.out.println("Invalid choice");
                return true;
        }
    }

    private static boolean conformation() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Are you sure?(Y/N)");
        String choice = sc.nextLine();
        if(choice.toLowerCase().equals("y")) {
            return true;
        }else if(choice.toLowerCase().equals("n")) {
            return false;
        }else {
            System.out.println("Invalid choice");
            return false;
        }
    }


}
