package com.fluxo.schedule.dto;

import java.util.List;

public class ScheduleEventDto {
	private Integer id;
	private String title;
	private String description;
	private String date;
	private String time;
	private Integer sprint;
	private List<String> categories;

	public ScheduleEventDto() {}

	public ScheduleEventDto(Integer id, String title, String description, String date, String time, Integer sprint, List<String> categories) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.date = date;
		this.time = time;
		this.sprint = sprint;
		this.categories = categories;
	}

	public Integer getId() { return id; }
	public String getTitle() { return title; }
	public String getDescription() { return description; }
	public String getDate() { return date; }
	public String getTime() { return time; }
	public Integer getSprint() { return sprint; }
	public List<String> getCategories() { return categories; }

	public void setId(Integer id) { this.id = id; }
	public void setTitle(String title) { this.title = title; }
	public void setDescription(String description) { this.description = description; }
	public void setDate(String date) { this.date = date; }
	public void setTime(String time) { this.time = time; }
	public void setSprint(Integer sprint) { this.sprint = sprint; }
	public void setCategories(List<String> categories) { this.categories = categories; }
}


