package info.jejking.hamburg.nord.drucksachen.allris;

import java.net.URL;
import java.time.LocalDate;

import com.google.common.collect.ImmutableList;

public class RawDrucksache {

	private final String id;
	private final String title;
	private final String type;
	private final URL originalUrl;
	private final LocalDate creationDate;
	
	private final ImmutableList<String> extractedHtmlContentItems;

	public RawDrucksache(String id, String title, String type, URL originalUrl,
			LocalDate creationDate,
			ImmutableList<String> extractedHtmlContentItems) {
		super();
		this.id = id;
		this.title = title;
		this.type = type;
		this.originalUrl = originalUrl;
		this.creationDate = creationDate;
		this.extractedHtmlContentItems = extractedHtmlContentItems;
	}
	
	/*
	 * Omitted. Any attachments, links to actual meetings or agendas,
	 * decision status (if any), coordination.
	 */
	
	
	
}
