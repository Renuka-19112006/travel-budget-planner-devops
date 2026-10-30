package com.college.travelbudget.service;

import com.college.travelbudget.data.DestinationCatalog;
import com.college.travelbudget.model.PlanRequest;
import com.college.travelbudget.model.PlanResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlannerServiceTest {

    private final PlannerService service = new PlannerService(new DestinationCatalog());

    private PlanResponse plan(String from, String to, int days, int members, double budget) {
        return service.plan(new PlanRequest(from, to, days, members, budget));
    }

    @Test
    void sampleTripFitsTheBudgetAndUsesIt() {
        PlanResponse r = plan("Hyderabad", "Goa", 4, 2, 25000);
        assertTrue(r.feasible());
        assertEquals("Plan fits your budget", r.status());
        assertEquals("Standard", r.tier());
        assertTrue(r.totalPlanned() <= r.budget(), "plan must not exceed the budget");
        assertTrue(r.spareBudget() >= 0 && r.spareBudget() < 10, "plan should use almost the whole budget");
        assertEquals(0, r.shortfall());
    }

    @Test
    void categoriesAddUpToTheTotal() {
        PlanResponse r = plan("Hyderabad", "Goa", 4, 2, 25000);
        long sum = r.categories().stream().mapToLong(c -> c.amount()).sum();
        assertEquals(r.totalPlanned(), sum);
        assertEquals(6, r.categories().size());
    }

    @Test
    void transportCostIsFareTimesMembers() {
        PlanResponse r = plan("Hyderabad", "Goa", 4, 2, 25000);
        long transport = r.categories().get(0).amount();
        assertEquals("Transport", r.categories().get(0).name());
        assertEquals(r.farePerPerson() * 2, transport);
        assertTrue(r.distanceKm() > 400 && r.distanceKm() < 1000);
    }

    @Test
    void sameCityDayTripHasNoTravelAndNoHotel() {
        PlanResponse r = plan("Hyderabad", "Hyderabad", 1, 2, 10000);
        assertEquals(0, r.hotelNights());
        assertEquals(0, r.farePerPerson());
        assertEquals(0, r.distanceKm());
        // 1 day, 2 people, Hyderabad base costs, Comfort level (160 percent)
        assertEquals("Comfort", r.tier());
        assertEquals(4368, r.totalPlanned());
        assertEquals(5632, r.spareBudget());
        assertEquals(1, r.itinerary().size());
    }

    @Test
    void hotelNightsAreDaysMinusOneAndRoomsAreHalfOfMembersRoundedUp() {
        assertEquals(3, plan("Pune", "Goa", 4, 3, 60000).hotelNights());
        assertEquals(2, plan("Pune", "Goa", 4, 3, 60000).rooms());
        assertEquals(1, plan("Pune", "Goa", 4, 2, 60000).rooms());
        assertEquals(3, plan("Pune", "Goa", 4, 5, 90000).rooms());
        assertEquals(0, plan("Pune", "Goa", 1, 2, 60000).hotelNights());
    }

    @Test
    void veryLowBudgetIsNotFeasibleAndShowsShortfall() {
        PlanResponse r = plan("Hyderabad", "Goa", 4, 2, 3000);
        assertFalse(r.feasible());
        assertEquals("Budget too low", r.status());
        assertEquals("Economy", r.tier());
        assertTrue(r.shortfall() > 0);
        assertEquals(r.totalPlanned() - r.budget(), r.shortfall());
        assertTrue(r.percentUsed() > 100);
    }

    @Test
    void bigBudgetGivesComfortWithSpareMoney() {
        PlanResponse r = plan("Hyderabad", "Goa", 3, 2, 200000);
        assertEquals("Comfort", r.tier());
        assertEquals(160, r.priceLevelPercent());
        assertTrue(r.spareBudget() > 0);
    }

    @Test
    void moreBudgetNeverMeansLessSpending() {
        long low = plan("Chennai", "Goa", 5, 2, 30000).totalPlanned();
        long mid = plan("Chennai", "Goa", 5, 2, 45000).totalPlanned();
        long high = plan("Chennai", "Goa", 5, 2, 70000).totalPlanned();
        assertTrue(low <= mid && mid <= high);
    }

    @Test
    void planOptionsGoUpFromEconomyToComfort() {
        PlanResponse r = plan("Hyderabad", "Goa", 4, 2, 25000);
        assertEquals(3, r.options().size());
        assertEquals("Economy", r.options().get(0).name());
        assertTrue(r.options().get(0).total() < r.options().get(1).total());
        assertTrue(r.options().get(1).total() < r.options().get(2).total());
        assertTrue(r.options().get(0).fits());
    }

    @Test
    void unknownFromCityUsesDefaultDistanceAndGivesANote() {
        PlanResponse r = plan("Atlantis", "Goa", 3, 3, 40000);
        assertEquals(600, r.distanceKm());
        assertFalse(r.routeNote().isEmpty());
        assertEquals("Atlantis", r.from());
    }

    @Test
    void fromCityIsMatchedIgnoringCaseAndSpaces() {
        PlanResponse r = plan("  hyderabad ", "Goa", 3, 2, 40000);
        assertEquals("Hyderabad", r.from());
        assertTrue(r.routeNote().isEmpty());
    }

    @Test
    void unknownDestinationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> plan("Hyderabad", "Mars", 2, 2, 5000));
    }

    @Test
    void itineraryHasOneEntryPerDay() {
        for (int days = 1; days <= 8; days++) {
            assertEquals(days, plan("Pune", "Goa", days, 1, 50000).itinerary().size());
        }
        PlanResponse r = plan("Hyderabad", "Goa", 4, 2, 25000);
        assertTrue(r.itinerary().get(0).title().startsWith("Arrival"));
        assertEquals("Last look and return", r.itinerary().get(3).title());
    }

    @Test
    void packingListTipsAndWeatherAreFilled() {
        PlanResponse r = plan("Hyderabad", "Manali", 5, 2, 60000);
        assertFalse(r.packingList().isEmpty());
        assertFalse(r.tips().isEmpty());
        assertFalse(r.weatherRange().isEmpty());
        assertEquals(4, r.topStops().size());
    }

    @Test
    void destinationAndCityListsAreAvailable() {
        assertTrue(service.getDestinations().contains("Goa"));
        assertTrue(service.getDestinations().contains("Visakhapatnam"));
        assertTrue(service.getCities().contains("Kolkata"));
    }
}
