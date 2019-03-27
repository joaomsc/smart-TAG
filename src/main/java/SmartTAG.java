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
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

public class SmartTAG {

    private static final String APPLICATION_NAME = "SmartTAG";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = new ArrayList<String>() {{
        add(CalendarScopes.CALENDAR);
        add(CalendarScopes.CALENDAR_EVENTS);
        add(TasksScopes.TASKS);
    }};
    private static final String CONFIG_FILE_PATH = "/configs.json";
    private static final String GCREDENTIALS_FILE_PATH = "/googlecredentials.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    public static void main(String... args) throws IOException, GeneralSecurityException {

//        final File staticHtmlFile = new File("src/main/resources/agenda.html");
//        Document doc = Jsoup.parse(staticHtmlFile, "UTF-8", "http://example.com/");
//
//        DocumentDL docDL = new DocumentDL();
//        docDL.printAllDeadLines(doc);
        //;

        //Get Config Params
        String configJson = IOUtils.toString(SmartTAG.class.getResourceAsStream(CONFIG_FILE_PATH), "UTF-8");
        JsonObject configObj = (JsonObject) new com.google.gson.JsonParser().parse(configJson);

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final Credential googleCredential = getGoogleCredentials(HTTP_TRANSPORT);

        Tasks taskService = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleCredential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Calendar calendarService = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleCredential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Calendars calendarsManager = new Calendars(calendarService, configObj.get("calendars").getAsJsonObject());

        calendarsManager.clearWeekEventsFromCalendar("SÃO SEBASTIÃO - Audiências JF");
        //calendarsManager.printLast10Events(calendarService, "SÃO SEBASTIÃO - Audiências JF");
        //calendarsManager.addTask(taskService);
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getGoogleCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        // Load config json.
        InputStream in = SmartTAG.class.getResourceAsStream(GCREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));


        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

}
