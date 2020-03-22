package code.onlycode.coronavirustracker.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import code.onlycode.coronavirustracker.dto.RegionDto;

@Service
public class CoronaVirusTrackerService {

	private static final String COUNTRY_REGION = "Country/Region";
	private static String VIRUS_DATA_CONFIRMED_CASE_URL = "https://raw.githubusercontent.com/CSSEGISandData/"
			+ "COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
	private static String VIRUS_DATA_DEATH_CASE_URL = "https://raw.githubusercontent.com/CSSEGISandData/"
			+ "COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Deaths.csv";
	private static String VIRUS_DATA_RECOVERED_CASE_URL = "https://raw.githubusercontent.com/CSSEGISandData/"
			+ "COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Recovered.csv";
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private List<RegionDto> regionDtos = new ArrayList<>();
	Map<String, Integer> mapofCountryVsTotalDeath = new HashMap<>();
	Map<String, Integer> mapofCountryVsTotalRecovered = new HashMap<>();
	private String currentDate;
	private String previousDate;
	private static final ThreadLocal<SimpleDateFormat> simpleDateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("mm/dd/yy");
		}
	};
	
	private static final ThreadLocal<SimpleDateFormat> actualDateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("dd/mm/yyyy");
		}
	};

	public List<RegionDto> getRegionDtos() {
		return regionDtos;
	}
	
	public String getCurrentDate() {
		return currentDate;
	}

	public String getPreviousDate() {
		return previousDate;
	}


	@PostConstruct
	@Scheduled(cron = "* * 1 * * *")
	public void fetchService() {
		List<RegionDto> newregionDtos = new ArrayList<>();
		try {
			getTotalNoOfDeath();

			getTotalNoOfRecovered();
			newregionDtos = getVirusDataForConfirmedCase(newregionDtos);
			this.regionDtos = newregionDtos;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<RegionDto> getVirusDataForConfirmedCase(List<RegionDto> newregionDtos)
			throws IOException, InterruptedException, ParseException {
		String currentDate = "";
		String previousDate = "";
		Iterable<CSVRecord> csvRecord = getHttpResponse(VIRUS_DATA_CONFIRMED_CASE_URL);
		for (CSVRecord record : csvRecord) {
			RegionDto region = new RegionDto();
			region.setCountry(record.get(COUNTRY_REGION));
			int latestCases = Integer.parseInt(record.get(record.size() - 1));
			int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
			region.setLatestTotalCases(latestCases);
			region.setDifferenceFromPreviousDay(latestCases - prevDayCases);
			logger.info(" test " + region);
			newregionDtos.add(region);
		}

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest build = HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_CONFIRMED_CASE_URL)).build();
		HttpResponse<String> httpResponse = client.send(build, HttpResponse.BodyHandlers.ofString());
		StringReader csvReader = new StringReader(httpResponse.body());
 
		csvReader.reset();
		Iterable<CSVRecord> recordsWithHeader = CSVFormat.DEFAULT.parse(csvReader);
		for (CSVRecord headerRecord : recordsWithHeader) {
			currentDate = actualDateFormat.get().format(simpleDateFormat.get().parse(headerRecord.get(headerRecord.size() - 1)));
			previousDate = actualDateFormat.get().format(simpleDateFormat.get().parse(headerRecord.get(headerRecord.size() - 2)));
			break;
		}
		Comparator<RegionDto> regionComparatorCountry = new Comparator<RegionDto>() {

			@Override
			public int compare(RegionDto o1, RegionDto o2) {
				return o1.getCountry().compareTo(o2.getCountry());
			}
		};

		newregionDtos.sort(regionComparatorCountry);
		Map<String, RegionDto> mapOfRegions = new LinkedHashMap<String, RegionDto>(); // newregionDtos.stream().collect(Collectors.toMap(RegionDto::getCountry,
																						// item -> item));

		for (RegionDto region : newregionDtos) {
			if (mapOfRegions.containsKey(region.getCountry())) {
				RegionDto regionfromMap = mapOfRegions.get(region.getCountry());
				regionfromMap.setLatestTotalCases(regionfromMap.getLatestTotalCases() + region.getLatestTotalCases());
				regionfromMap.setDifferenceFromPreviousDay(
						regionfromMap.getDifferenceFromPreviousDay() + region.getDifferenceFromPreviousDay());
			} else {
				mapOfRegions.put(region.getCountry(), region);
			}
			region.setTotalDeath(mapofCountryVsTotalDeath.get(region.getCountry()));
			region.setTotalRecovered(mapofCountryVsTotalRecovered.get(region.getCountry()));
		}
		newregionDtos = new ArrayList<>(mapOfRegions.values());

		Comparator<RegionDto> regionComparatorTotalCases = new Comparator<RegionDto>() {

			@Override
			public int compare(RegionDto o1, RegionDto o2) {
				return o2.getLatestTotalCases() - o1.getLatestTotalCases();
			}
		};
		newregionDtos.sort(regionComparatorTotalCases);
		this.currentDate = currentDate;
		this.previousDate = previousDate;
		newregionDtos.get(0).getTotalDeathPercentage();
		return newregionDtos;
	}

	private Map<String, Integer> getTotalNoOfDeath() throws IOException, InterruptedException {
		Iterable<CSVRecord> csvRecord = getHttpResponse(VIRUS_DATA_DEATH_CASE_URL);
		for (CSVRecord record : csvRecord) {
			int totalDeath = Integer.parseInt(record.get(record.size() - 1));
			String country = record.get(COUNTRY_REGION);
			if (mapofCountryVsTotalDeath.containsKey(country)) {
				Integer totalDeathFromMap = mapofCountryVsTotalDeath.get(country);
				totalDeathFromMap = totalDeathFromMap + totalDeath;
			} else {
				mapofCountryVsTotalDeath.put(country, totalDeath);
			}
		}
		return mapofCountryVsTotalDeath;
	}

	private Map<String, Integer> getTotalNoOfRecovered() throws IOException, InterruptedException {
		Iterable<CSVRecord> csvRecord = getHttpResponse(VIRUS_DATA_RECOVERED_CASE_URL);
		for (CSVRecord record : csvRecord) {
			int totalRecovered = Integer.parseInt(record.get(record.size() - 1));
			String country = record.get(COUNTRY_REGION);
			if (mapofCountryVsTotalRecovered.containsKey(country)) {
				Integer totalRecoveredFromMap = mapofCountryVsTotalDeath.get(country);
				totalRecoveredFromMap = totalRecoveredFromMap + totalRecovered;
			} else {
				mapofCountryVsTotalRecovered.put(country, totalRecovered);
			}
		}
		return mapofCountryVsTotalRecovered;
	}

	private Iterable<CSVRecord> getHttpResponse(String url) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest build = HttpRequest.newBuilder().uri(URI.create(url)).build();
		HttpResponse<String> httpResponse = client.send(build, HttpResponse.BodyHandlers.ofString());
		StringReader csvReader = new StringReader(httpResponse.body());
		Iterable<CSVRecord> csvRecord = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvReader);
		return csvRecord;
	}

}
