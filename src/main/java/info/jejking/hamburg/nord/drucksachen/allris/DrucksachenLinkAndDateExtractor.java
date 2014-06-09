package info.jejking.hamburg.nord.drucksachen.allris;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;


public class DrucksachenLinkAndDateExtractor implements Callable<ImmutableMap<URL, Optional<LocalDate>>> {

    private final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd.MM.yyyy");
    private final InputStream input;
    
    public DrucksachenLinkAndDateExtractor(InputStream input) {
        this.input = checkNotNull(input);
    }
    
    
    @Override
    public ImmutableMap<URL, Optional<LocalDate>> call() throws Exception {
        
        Document htmlDoc = Jsoup.parse(input, null, "http://ratsinformation.web.hamburg.de:85/bi/vo040.asp?showall=true");
        ImmutableMap.Builder<URL, Optional<LocalDate>> mapBuilder = ImmutableMap.builder();
        
        Elements tableRowElements = htmlDoc.select("#rismain > table > tbody > tr");
        
        for (Element tableRowElement : tableRowElements) {
            Elements linkElements = tableRowElement.select("td > a");
            if (!linkElements.isEmpty()) {
                String hrefText = linkElements.first().attr("href");
                String dateText = tableRowElement.select("td:eq(3)").first().text(); // fourth cell...
                mapBuilder.put(new URL(hrefText), makeOptionalDate(dateText));
            }
        }
        
        
        return mapBuilder.build();
    }


    private Optional<LocalDate> makeOptionalDate(String dateText) {
        if (dateText.trim().isEmpty()) {
            return Optional.absent();
        } else {
            try {
                LocalDate date = this.dateFormat.parseLocalDate(dateText);
                return Optional.of(date);
            } catch (Exception e) {
                e.printStackTrace();
                return Optional.absent();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        URL copy = new URL(args[0]);
        DrucksachenLinkAndDateExtractor extractor = new DrucksachenLinkAndDateExtractor(copy.openStream());
        ImmutableMap<URL, Optional<LocalDate>> urls = extractor.call();
        
        for (Map.Entry<URL, Optional<LocalDate>> entry : urls.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
        
    }
    

    

}
