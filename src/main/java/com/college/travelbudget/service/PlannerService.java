package com.college.travelbudget.service;

import com.college.travelbudget.data.DestinationCatalog;
import com.college.travelbudget.data.DestinationCatalog.City;
import com.college.travelbudget.data.DestinationCatalog.Destination;
import com.college.travelbudget.model.PlanRequest;
import com.college.travelbudget.model.PlanResponse;
import com.college.travelbudget.model.PlanResponse.CategoryShare;
import com.college.travelbudget.model.PlanResponse.DayPlan;
import com.college.travelbudget.model.PlanResponse.TierOption;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Turns (From, To, days, members, budget) into a spending plan.
 *
 * How it works (simple rule-based logic, no AI and no live prices):
 * 1. Estimate the round-trip travel cost from the distance between the two cities.
 * 2. Take the destination's "standard" daily costs (stay, food, local travel, activities).
 * 3. Keep 5 percent of the plan as an emergency fund.
 * 4. Scale the standard costs up or down (65 percent to 160 percent) so that the plan uses the budget.
 */
@Service
public class PlannerService {

    static final double CONTINGENCY = 0.05;
    static final double MIN_FACTOR = 0.65;
    static final double MAX_FACTOR = 1.60;
    static final double ROAD_FACTOR = 1.3;
    static final double DEFAULT_ROAD_KM = 600;

    private final DestinationCatalog catalog;

    public PlannerService(DestinationCatalog catalog) {
        this.catalog = catalog;
    }

    public List<String> getDestinations() {
        return catalog.getDestinationNames();
    }

    public List<String> getCities() {
        return catalog.getCityNames();
    }

    // ---------------------------------------------------------------- main method

    public PlanResponse plan(PlanRequest request) {
        Destination dest = catalog.findDestination(request.to())
                .orElseThrow(() -> new IllegalArgumentException("Unknown destination: " + request.to().trim()));

        int days = request.days();
        int members = request.members();
        long budget = Math.max(1, Math.round(request.budget()));

        // ---- route and travel cost
        String fromInput = request.from().trim();
        String fromName;
        double roadKm;
        String routeNote = "";
        if (fromInput.equalsIgnoreCase(dest.name())) {
            fromName = dest.name();
            roadKm = 0;
        } else {
            Optional<City> city = catalog.findCity(fromInput);
            if (city.isPresent()) {
                fromName = city.get().name();
                roadKm = haversineKm(city.get().lat(), city.get().lon(), dest.lat(), dest.lon()) * ROAD_FACTOR;
            } else {
                fromName = fromInput;
                roadKm = DEFAULT_ROAD_KM;
                routeNote = "We do not have distance data for \"" + fromInput
                        + "\", so we assumed about 600 km. Treat the travel cost as a rough guess.";
            }
        }
        Fare fare = fareFor(roadKm);
        long transport = fare.perPerson() * members;

        // ---- standard (100 percent) costs of the stay
        int nights = Math.max(0, days - 1);
        int rooms = (members + 1) / 2;
        Base base = new Base(
                (double) dest.hotelPerRoomNight() * rooms * nights,
                (double) dest.foodPerPersonDay() * members * days,
                (double) dest.localPerPersonDay() * members * days,
                (double) dest.activitiesPerPersonDay() * members * days);

        // ---- choose the price level that uses the budget
        double factor = (budget / (1 + CONTINGENCY) - transport) / base.variable();
        boolean feasible = factor >= MIN_FACTOR;
        factor = Math.max(MIN_FACTOR, Math.min(MAX_FACTOR, factor));

        Amounts a = amounts(transport, base, factor);
        long total = a.total();
        long spare = budget - total;
        long shortfall = Math.max(0, total - budget);
        String tier = tierFor(factor);

        // ---- category list
        List<CategoryShare> categories = new ArrayList<>();
        categories.add(share("Transport", a.transport(), budget));
        categories.add(share("Stay", a.stay(), budget));
        categories.add(share("Food", a.food(), budget));
        categories.add(share("Local Travel", a.local(), budget));
        categories.add(share("Activities", a.activities(), budget));
        categories.add(share("Emergency Fund", a.contingency(), budget));

        long dailySpend = (a.food() + a.local() + a.activities()) / days;
        long dailyAllowancePerPerson = dailySpend / members;

        String status;
        String message;
        if (feasible) {
            status = "Plan fits your budget";
            message = "Your budget gives you a " + tier + " trip to " + dest.name() + ". You can spend about "
                    + rupees(dailyAllowancePerPerson) + " per person per day on food, local travel and activities.";
        } else {
            status = "Budget too low";
            message = "Your budget is about " + rupees(shortfall) + " short of even a basic trip to "
                    + dest.name() + ". The plan below shows the lowest-cost version.";
        }

        return new PlanResponse(
                fromName, dest.name(), dest.tagline(), days, members, budget, nights, rooms,
                tier, tierDescription(tier), (int) Math.round(factor * 100), feasible, status, message,
                Math.round(roadKm), fare.mode(), fare.perPerson(), routeNote,
                categories, total, spare, shortfall, round1((double) total / budget * 100.0),
                total / members, dailyAllowancePerPerson,
                dest.weatherRange(), dest.weatherNote(),
                new ArrayList<>(dest.highlights().subList(0, 4)),
                buildItinerary(dest, fromName, fare, days, dailySpend, roadKm),
                dest.packing(),
                buildTips(dest, tier, feasible, shortfall, spare, budget, members, days),
                buildOptions(transport, base, budget));
    }

    // ---------------------------------------------------------------- helpers

    private record Fare(String mode, long perPerson) {
    }

    private record Base(double stay, double food, double local, double activities) {
        double variable() {
            return stay + food + local + activities;
        }
    }

    private record Amounts(long transport, long stay, long food, long local, long activities, long contingency) {
        long total() {
            return transport + stay + food + local + activities + contingency;
        }
    }

    private static Amounts amounts(long transport, Base base, double factor) {
        long stay = floor(base.stay() * factor);
        long food = floor(base.food() * factor);
        long local = floor(base.local() * factor);
        long act = floor(base.activities() * factor);
        long contingency = floor((transport + stay + food + local + act) * CONTINGENCY);
        return new Amounts(transport, stay, food, local, act, contingency);
    }

    /** Round-trip fare per person, estimated from the road distance. Sample rates only. */
    static Fare fareFor(double roadKm) {
        if (roadKm <= 0) {
            return new Fare("Local trip (no intercity travel)", 0);
        }
        double rate;
        String mode;
        if (roadKm <= 300) {
            rate = 1.8;
            mode = "Bus";
        } else if (roadKm <= 1000) {
            rate = 1.6;
            mode = "Train (sleeper or 3AC)";
        } else {
            rate = 2.2;
            mode = "Train (3AC) or budget flight";
        }
        double oneWay = Math.max(150, roadKm * rate);
        long roundTrip = Math.round(oneWay * 2 / 50.0) * 50;
        return new Fare(mode, roundTrip);
    }

    static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * r * Math.asin(Math.sqrt(h));
    }

    static String tierFor(double factor) {
        if (factor < 0.85) {
            return "Economy";
        }
        if (factor < 1.25) {
            return "Standard";
        }
        return "Comfort";
    }

    private static String tierDescription(String tier) {
        return switch (tier) {
            case "Economy" -> "Budget stays or hostels, local food, buses and shared rides.";
            case "Standard" -> "Mid-range hotels, a mix of restaurants and local transport, a few paid activities.";
            default -> "Comfortable hotels, cabs when needed, good restaurants and paid experiences.";
        };
    }

    private static CategoryShare share(String name, long amount, long budget) {
        return new CategoryShare(name, amount, round1((double) amount / budget * 100.0));
    }

    private List<TierOption> buildOptions(long transport, Base base, long budget) {
        double[] factors = {MIN_FACTOR, 1.0, MAX_FACTOR};
        String[] names = {"Economy", "Standard", "Comfort"};
        List<TierOption> options = new ArrayList<>();
        for (int i = 0; i < factors.length; i++) {
            long total = amounts(transport, base, factors[i]).total();
            options.add(new TierOption(names[i], tierDescription(names[i]), total, total <= budget, budget - total));
        }
        return options;
    }

    private List<DayPlan> buildItinerary(Destination d, String fromName, Fare fare, int days,
                                         long dailySpend, double roadKm) {
        List<String> h = d.highlights();
        int[] idx = {0};
        java.util.function.Supplier<String> next = () -> h.get(idx[0]++ % h.size());

        String travel = roadKm <= 0
                ? "No intercity travel needed"
                : "Travel from " + fromName + " to " + d.name() + " by " + fare.mode()
                + " (about " + Math.round(roadKm) + " km by road)";
        String back = roadKm <= 0 ? "Head home" : "Check out and travel back to " + fromName;

        List<DayPlan> plan = new ArrayList<>();
        if (days == 1) {
            plan.add(new DayPlan(1, "Day trip to " + d.name(), List.of(
                    travel,
                    "Morning: " + next.get(),
                    "Afternoon: " + next.get(),
                    "Evening: Local food, then " + (roadKm <= 0 ? "head home" : "return to " + fromName)),
                    dailySpend));
            return plan;
        }
        plan.add(new DayPlan(1, "Arrival in " + d.name(), List.of(
                travel, "Check in and rest", "Evening: " + next.get()), dailySpend));
        for (int day = 2; day < days; day++) {
            plan.add(new DayPlan(day, "Sightseeing in " + d.name(), List.of(
                    "Morning: " + next.get(),
                    "Afternoon: " + next.get(),
                    "Evening: Local food and a relaxed walk"), dailySpend));
        }
        plan.add(new DayPlan(days, "Last look and return", List.of(
                "Morning: " + next.get(), back), dailySpend));
        return plan;
    }

    private List<String> buildTips(Destination d, String tier, boolean feasible, long shortfall,
                                   long spare, long budget, int members, int days) {
        List<String> tips = new ArrayList<>();
        if (!feasible) {
            tips.add("You need about " + rupees(shortfall) + " more. Try fewer days, fewer travellers, "
                    + "a nearer destination, or a higher budget.");
        }
        tips.addAll(d.tips());
        switch (tier) {
            case "Economy" -> {
                tips.add("Choose hostels, homestays or budget hotels, and share rooms.");
                tips.add("Book trains or buses 30 to 60 days early for lower fares.");
                tips.add("Eat local thalis and street food for most meals.");
            }
            case "Standard" -> tips.add("Mix paid sightseeing with free walks, beaches and markets to stay on track.");
            default -> tips.add("You have room for upgrades. Spend extra on one special experience instead of everything.");
        }
        if (members > 2 && members % 2 == 1) {
            tips.add("With " + members + " travellers, ask the hotel for an extra bed instead of a new room.");
        }
        if (spare > budget / 10 && feasible) {
            tips.add("About " + rupees(spare) + " is not planned. Keep it as savings or for shopping.");
        }
        tips.add("Do not touch the emergency fund unless something unexpected happens.");
        return tips;
    }

    private static long floor(double value) {
        return (long) Math.floor(value + 1e-9);
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static String rupees(long amount) {
        return "\u20B9" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("en-IN")).format(amount);
    }
}
