package com.college.travelbudget.data;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Predefined SAMPLE data. These are rough estimates for learning and demonstration only.
 * They are NOT live prices. No travel API or database is used.
 */
@Component
public class DestinationCatalog {

    public record Destination(String name, double lat, double lon, String tagline,
                              long hotelPerRoomNight, long foodPerPersonDay,
                              long localPerPersonDay, long activitiesPerPersonDay,
                              String weatherRange, String weatherNote,
                              List<String> highlights, List<String> packing, List<String> tips) {
    }

    public record City(String name, double lat, double lon) {
    }

    private final Map<String, Destination> destinations = new LinkedHashMap<>();
    private final Map<String, City> cities = new TreeMap<>();

    public DestinationCatalog() {
        addDestination(new Destination("Goa", 15.499, 73.828,
                "Beaches, sunsets, old churches and seafood.",
                2500, 700, 350, 500,
                "24-33 C", "Warm and humid with a sea breeze",
                List.of("North Goa beaches (Calangute and Baga)", "Fort Aguada and sunset point",
                        "Old Goa churches and Panjim Fontainhas walk", "Palolem beach in South Goa",
                        "Spice plantation visit", "Dudhsagar waterfalls day trip"),
                List.of("Sunscreen and sunglasses", "Light cotton clothes and swimwear", "Flip-flops",
                        "Power bank and phone charger", "Valid ID for scooter rental", "Small daypack"),
                List.of("Rent a scooter for the day instead of using taxis for every trip.",
                        "Eat at local fish-thali places away from the busiest beach shacks.")));

        addDestination(new Destination("Hyderabad", 17.385, 78.487,
                "Historic forts, bazaars and the famous biryani.",
                2200, 600, 300, 400,
                "22-34 C", "Warm days, pleasant evenings",
                List.of("Charminar and Laad Bazaar", "Golconda Fort and light show",
                        "Hussain Sagar lake and Buddha statue", "Salar Jung Museum",
                        "Ramoji Film City", "Hyderabadi biryani and Irani chai trail"),
                List.of("Comfortable walking shoes", "Light cotton clothes", "Sunscreen and cap",
                        "Water bottle", "Power bank and phone charger", "Small daypack"),
                List.of("Use the metro and app cabs instead of private taxis for the whole day.",
                        "Big attractions like film city are usually the largest single cost, so plan them once.")));

        addDestination(new Destination("Bengaluru", 12.972, 77.594,
                "Gardens, cafes, breweries and pleasant weather.",
                2300, 600, 300, 400,
                "18-30 C", "Mild and pleasant, light rain possible",
                List.of("Lalbagh Botanical Garden", "Cubbon Park and Vidhana Soudha walk",
                        "Bangalore Palace", "Nandi Hills sunrise", "VV Puram food street",
                        "Cafe and brewery hopping in Indiranagar"),
                List.of("Light jacket or umbrella", "Comfortable walking shoes", "Casual clothes",
                        "Power bank and phone charger", "Water bottle", "Small daypack"),
                List.of("Traffic is heavy, so use the metro for long distances when you can.",
                        "Have breakfast at local darshini restaurants. It is tasty and cheap.")));

        addDestination(new Destination("Visakhapatnam", 17.687, 83.219,
                "Beaches, hills and the green Araku Valley.",
                2000, 500, 300, 350,
                "23-33 C", "Warm and breezy near the coast",
                List.of("RK Beach and the beach road", "Kailasagiri hill park and ropeway",
                        "Submarine museum", "Araku Valley day trip", "Borra Caves",
                        "Rushikonda beach and Simhachalam temple"),
                List.of("Sunscreen and cap", "Light cotton clothes", "Comfortable sandals",
                        "Rain jacket (monsoon months)", "Power bank and phone charger", "Small daypack"),
                List.of("Do the Araku Valley trip by bus or train. It costs much less than a private taxi.",
                        "Beach road food stalls are good value for evening snacks.")));

        addDestination(new Destination("Jaipur", 26.912, 75.787,
                "Forts, palaces and colourful markets.",
                2400, 600, 400, 800,
                "10-35 C", "Hot summers, pleasant October to March",
                List.of("Amber Fort", "City Palace and Jantar Mantar", "Hawa Mahal photo stop",
                        "Nahargarh Fort sunset", "Bapu Bazaar shopping", "Rajasthani thali dinner"),
                List.of("Hat and sunglasses", "Light cotton clothes (jacket for winter nights)",
                        "Comfortable walking shoes", "Sunscreen", "Power bank and phone charger",
                        "Cash for small shops"),
                List.of("Ask about a combined ticket if you plan to visit several monuments.",
                        "Bargain politely in markets and compare two or three shops first.")));

        addDestination(new Destination("Manali", 32.240, 77.189,
                "Mountains, snow, rivers and cosy cafes.",
                2800, 650, 700, 900,
                "5-20 C", "Cool to cold, snow in winter",
                List.of("Hadimba Temple and Mall Road", "Solang Valley activities",
                        "Old Manali cafes", "Vashisht hot springs", "Jogini waterfall walk",
                        "Rohtang Pass (permit needed, seasonal)"),
                List.of("Warm jacket and thermals", "Waterproof shoes", "Woollen cap and gloves",
                        "Moisturiser and lip balm", "Power bank and phone charger", "Basic medicines"),
                List.of("Share a taxi with other travellers for sightseeing days to cut local travel cost.",
                        "Some mountain roads close in bad weather, so keep one spare day if you can.")));

        // More cities that can be used in the "From" box (coordinates are approximate).
        addCity("Ahmedabad", 23.023, 72.571);
        addCity("Bengaluru", 12.972, 77.594);
        addCity("Chennai", 13.083, 80.270);
        addCity("Coimbatore", 11.017, 76.956);
        addCity("Delhi", 28.614, 77.209);
        addCity("Guntur", 16.307, 80.437);
        addCity("Kakinada", 16.989, 82.247);
        addCity("Kochi", 9.931, 76.267);
        addCity("Kolkata", 22.573, 88.364);
        addCity("Mumbai", 19.076, 72.878);
        addCity("Nagpur", 21.146, 79.088);
        addCity("Pune", 18.520, 73.857);
        addCity("Rajahmundry", 17.000, 81.804);
        addCity("Tirupati", 13.629, 79.419);
        addCity("Vijayawada", 16.506, 80.648);
    }

    private void addDestination(Destination d) {
        destinations.put(key(d.name()), d);
        addCity(d.name(), d.lat(), d.lon());
    }

    private void addCity(String name, double lat, double lon) {
        cities.put(key(name), new City(name, lat, lon));
    }

    private static String key(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }

    public List<String> getDestinationNames() {
        return new ArrayList<>(destinations.values().stream().map(Destination::name).toList());
    }

    public List<String> getCityNames() {
        return new ArrayList<>(cities.values().stream().map(City::name).toList());
    }

    public Optional<Destination> findDestination(String name) {
        return Optional.ofNullable(destinations.get(key(name)));
    }

    public Optional<City> findCity(String name) {
        return Optional.ofNullable(cities.get(key(name)));
    }
}
