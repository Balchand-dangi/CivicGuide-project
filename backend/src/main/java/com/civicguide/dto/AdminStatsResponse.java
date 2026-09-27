package com.civicguide.dto;

public record AdminStatsResponse(long users, long mentors, long pendingMentors, long requests, long pendingRequests) {}
