package code.onlycode.coronavirustracker.dto;

import java.text.DecimalFormat;

public class RegionDto {
	private String country;
	private Integer latestTotalCases;
	private Integer differenceFromPreviousDay;
	private Integer totalDeath;
	private Integer totalRecovered;
	private Float totalDeathPercentage;
	private Float totalRecoveredPercentage;

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Integer getLatestTotalCases() {
		return latestTotalCases;
	}

	public void setLatestTotalCases(Integer latestTotalCases) {
		this.latestTotalCases = latestTotalCases;
	}

	@Override
	public String toString() {
		return "RegionDto [country=" + country + ", latestTotalCases=" + latestTotalCases
				+ ", differenceFromPreviousDay=" + differenceFromPreviousDay + ", totalDeath=" + totalDeath
				+ ", totalRecovered=" + totalRecovered + ", totalDeathPercentage=" + totalDeathPercentage
				+ ", totalRecoveredPercentage=" + totalRecoveredPercentage + "]";
	}

	public void setDifferenceFromPreviousDay(Integer differenceFromPreviousDay) {
		this.differenceFromPreviousDay = differenceFromPreviousDay;
	}

	public Integer getDifferenceFromPreviousDay() {
		return differenceFromPreviousDay;
	}

	public Integer getTotalDeath() {
		return totalDeath;
	}

	public void setTotalDeath(Integer totalDeath) {
		this.totalDeath = totalDeath;
	}

	public Integer getTotalRecovered() {
		return totalRecovered;
	}

	public void setTotalRecovered(Integer totalRecovered) {
		this.totalRecovered = totalRecovered;
	}

	public Float getTotalDeathPercentage() {
		if (totalDeath == 0)
			return 0F;
		else {
			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			float f = (totalDeath.floatValue() / latestTotalCases.floatValue()) * 100;
			return Float.parseFloat(decimalFormat.format(f));
		}
	}

	public Float getTotalRecoveredPercentage() {
		if (totalRecovered == 0)
			return 0F;
		else {
			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			float f = (totalRecovered.floatValue() / latestTotalCases.floatValue()) * 100;
			return Float.parseFloat(decimalFormat.format(f));
		}
	}

}
