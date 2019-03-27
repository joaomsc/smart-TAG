import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class Calendars {

    private Calendar service;
    private JsonObject calendarsIds;

    public Calendars(Calendar service, JsonObject calendarsIds) {
        this.service = service;
        this.calendarsIds = calendarsIds;
    }

    public String getCalendarIdByName(String calendarName) {
        return this.calendarsIds.get(calendarName).getAsString();
    }

    public void clearWeekEventsFromCalendar(String calendarName) throws IOException {

        String calendarId = getCalendarIdByName(calendarName);

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00.000'Z'");
        ZonedDateTime now  = ZonedDateTime.now();
        DateTime startTime = new DateTime(now.format(f));
        DateTime endTime = new DateTime(now.plusDays(7).format(f));

        Events events = this.service.events().list(calendarId)
                .setTimeMin(startTime)
                .setTimeMax(endTime)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        for (Event event : items) {
            this.service.events().delete(calendarId, event.getId()).execute();
            System.out.println(event.getId());
        }
    }

    public void printLast10Events(String calendarName) throws IOException {
        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = this.service.events().list(getCalendarIdByName(calendarName))
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }
    }

    public void printAll(Calendar service) throws IOException {
        String pageToken = null;
        do {
            CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                System.out.printf("\"%s\": \"%s\"\n", calendarListEntry.getSummary(), calendarListEntry.getId());
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
    }

    public void addEvent(Calendar service, String calendarName) throws IOException {
        Event event = new Event()
                .setSummary("Google I/O 2015")
                .setLocation("800 Howard St., San Francisco, CA 94103")
                .setDescription("A chance to hear more about Google's developer products.");

        DateTime startDateTime = new DateTime("2015-05-28T09:00:00-07:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setStart(start);

        DateTime endDateTime = new DateTime("2015-05-28T17:00:00-07:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setEnd(end);

        String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=2"};
        event.setRecurrence(Arrays.asList(recurrence));

        EventAttendee[] attendees = new EventAttendee[]{
                new EventAttendee().setEmail("lpage@example.com"),
                new EventAttendee().setEmail("sbrin@example.com"),
        };
        event.setAttendees(Arrays.asList(attendees));

        EventReminder[] reminderOverrides = new EventReminder[]{
                new EventReminder().setMethod("email").setMinutes(24 * 60),
                new EventReminder().setMethod("popup").setMinutes(10),
        };
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        event = service.events().insert(getCalendarIdByName(calendarName), event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());
    }

    public void addTask(Tasks service) throws IOException {

        DateTime now = new DateTime(System.currentTimeMillis());
        Task task = new Task()
                .setTitle("NEW TASK")
                .setNotes("DESCRIPTION")
                .setDue(now);

        task = service.tasks().insert("@default", task).execute();
        System.out.printf("Task created: %s\n", task.getSelfLink());
    }

}
