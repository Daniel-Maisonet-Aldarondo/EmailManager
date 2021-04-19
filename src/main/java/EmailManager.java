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
    private static final String APPLICATION_NAME = "Terminal Email Manager";
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
        if(conformation(numberOfMessages)) {
            for(Message m : messages) {
                service.users().messages().delete(user, m.getId()).execute();
            }
            System.out.println("Messages deleted: " + numberOfMessages);
        }
    }
    //create a que of specific messages to delete or trash or untrash
    private static List<Message> queMessage(List<Message> messages) {
        List<Message> que = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter the number(s) of the email you would like to que(ex 1 2 3 12): ");
        String output = sc.nextLine();
        String[] indexes = output.split(" ");
        for(String index : indexes) {
            que.add(messages.get(Integer.valueOf(index) - 1));
        }

        return que;
    }
    //deleting messages one by one
    private static void deleteMessages(Gmail service, String user) throws IOException {
        String query = getQuery();
        List<Message> messages = retrieveMessageQuery(service, query, user);
        displayMessageHeaders(service, messages, user);
        List<Message> messagesToDelete = queMessage(messages);
        deleteMessages(service,messagesToDelete, user);
    }

    private static void trashMessages(Gmail service, List<Message> messages, String user) throws IOException {
        int numberOfMessages = messages.size();
        for(Message m : messages) {
            service.users().messages().trash(user, m.getId()).execute();
        }
        System.out.println("Messages trashed: " + numberOfMessages);
    }
    private static void untrashMessages(Gmail service, String user) throws IOException {
        List<String> labels = new ArrayList<>();
        labels.add("TRASH");

        //default size for query is 100 messages
        ListMessagesResponse response = service.users().messages().list(user).setLabelIds(labels).execute();
        List<Message> messages = response.getMessages();
        displayMessageHeaders(service,messages,user);
        List<Message> messagesToUntrash = queMessage(messages);
        untrash(service,messagesToUntrash,user);
    }

    private static void untrash(Gmail service, List<Message> messages, String user) throws IOException{
        for(Message message: messages) {
            service.users().messages().untrash(user, message.getId()).execute();
        }
        System.out.println("You've untrashed " + messages.size() + " message(s)!");
    }

    private static void deleteTrashMessages(Gmail service, String user) throws IOException {
        //we only want the messages withing these folders
        List<String> labels = new ArrayList<>();
        labels.add("TRASH");

        //default size for query is 100 messages
        ListMessagesResponse response = service.users().messages().list(user).setLabelIds(labels).execute();
        List<Message> messages = response.getMessages();
        //delete messages
        displayMessageHeaders(service,messages,user);
        deleteMessages(service,messages,user);
    }

    private static void displayMessageHeaders(Gmail service, List<Message> messages, String user) throws IOException {
        int count = 0;
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
            count++;
            System.out.println("====================" + count + "========================");
        }
    }

    private static String getQuery() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter your search query: ");
        return sc.nextLine();
    }

    private static boolean run(Gmail service, String user) throws IOException{
        List<Message> messages;
        String selection;
        String query;
        System.out.println("1.List messages with query\n" +
                "2.Delete all messages with query\n" +
                "3.Delete specific messages with query\n" +
                "4.Trash all messages with query\n" +
                "5.Delete 100 messages from trash/spam\n" +
                "6.Untrash specific messages\n" +
                "7.Close program");
        Scanner sc = new Scanner(System.in);
        System.out.print("Please make a selection: ");
        selection = sc.nextLine();

        switch(selection) {
            case "1":
                query = getQuery();
                messages = retrieveMessageQuery(service, query, user);
                displayMessageHeaders(service,messages,user);
                return true;
            case "2":
                query = getQuery();
                messages = retrieveMessageQuery(service, query, user);
                deleteMessages(service,messages, user);
                return true;
            case "3":
                deleteMessages(service,user);
                return true;
            case "4":
                query = getQuery();
                messages = retrieveMessageQuery(service, query, user);
                trashMessages(service,messages,user);
                return true;
            case "5" :
                deleteTrashMessages(service, user);
                return true;
            case "6":
                untrashMessages(service,user);
                return true;
            case "7":
                sc.close();
                System.out.println("Bye Bye");
                return false;
            default:
                System.out.println("Invalid choice");
                return true;
        }
    }

    private static boolean conformation(int size) {
        Scanner sc = new Scanner(System.in);
        System.out.print("You are about to delete " + size + " message(s)\n" +
                "Are you sure?(Y/N)");
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
