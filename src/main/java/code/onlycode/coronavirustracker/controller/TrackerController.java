package code.onlycode.coronavirustracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import code.onlycode.coronavirustracker.dto.RegionDto;
import code.onlycode.coronavirustracker.service.CoronaVirusTrackerService;

@Controller
public class TrackerController {
	
	@Autowired
	private CoronaVirusTrackerService caronaVirusTrackerService;

	@GetMapping("/")
	public String getTracker(Model model) {
		List<RegionDto> regionDtos = caronaVirusTrackerService.getRegionDtos();
		int sum = regionDtos.stream().mapToInt(region -> region.getLatestTotalCases()).sum();
		int totalNewCases = regionDtos.stream().mapToInt(region -> region.getDifferenceFromPreviousDay()).sum();
		model.addAttribute("regions",regionDtos);
		model.addAttribute("totalCountTillNow",sum);
		model.addAttribute("totalNewCases",totalNewCases);
		model.addAttribute("currentDate",caronaVirusTrackerService.getCurrentDate());
		model.addAttribute("previousDate",caronaVirusTrackerService.getPreviousDate());
		return "tracker";
	}
}
