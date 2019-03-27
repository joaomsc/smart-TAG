import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Date;
import java.util.List;

public class DocumentDL {

    private String tag;
    private Date deadLine;

    public DocumentDL() {
    }

    public DocumentDL(String tag, Date deadLine) {

        this.tag = tag;
        this.deadLine = deadLine;
    }

//    public List<DocumentDL> getAllDeadLines(Document html) {
//        Elements deadLineElements = html.select("#tabela_Doc tr[class^=linha]");
//        for (Element line : deadLineElements) {
//            System.out.println(line.getAllElements().first().text());
//        }
//    }

    public void printAllDeadLines(Document html) {
        Elements deadLineElements = html.select("#tabela_Doc tr[class^=linha]");
        for (Element line : deadLineElements) {
            Elements columns = line.select("td");

            String tag = columns.first().text();
            String deadLineStr = columns.last().text();

            String[] dateSplit = deadLineStr.split("/");
            String year = dateSplit[2];
            String month = dateSplit[1];
            String day = dateSplit[0];
            String deadLineStrFormatted = year.concat("-").concat(month).concat("-").concat(day);

            DocumentDL doc = new DocumentDL(tag, Date.valueOf(deadLineStrFormatted));
            System.out.println(doc.getTag());
            System.out.println(doc.getDeadLine());
        }
    }

    public Date getDeadLine() {
        return deadLine;
    }

    public String getTag() {
        return tag;
    }
}
