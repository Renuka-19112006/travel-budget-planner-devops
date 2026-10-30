package com.college.travelbudget.model;

import java.util.List;

/** The full trip plan sent to the dashboard. All money values are whole rupees. */
public record PlanResponse(
        String from,
        String to,
        String tagline,
        int days,
        int members,
        long budget,
        int hotelNights,
        int rooms,
        String tier,
        String tierDescription,
        int priceLevelPercent,
        boolean feasible,
        String status,
        String message,
        long distanceKm,
        String transportMode,
        long farePerPerson,
        String routeNote,
        List<CategoryShare> categories,
        long totalPlanned,
        long spareBudget,
        long shortfall,
        double percentUsed,
        long perPersonTotal,
        long dailyAllowancePerPerson,
        String weatherRange,
        String weatherNote,
        List<String> topStops,
        List<DayPlan> itinerary,
        List<String> packingList,
        List<String> tips,
        List<TierOption> options
) {
    public record CategoryShare(String name, long amount, double percent) {
    }

    public record DayPlan(int day, String title, List<String> activities, long daySpend) {
    }

    public record TierOption(String name, String description, long total, boolean fits, long difference) {
    }
}
