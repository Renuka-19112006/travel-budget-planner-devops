const $ = (id) => document.getElementById(id);

const inr = new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0
});

const COLORS = {
    "Transport": "#0f5f58",
    "Stay": "#ffd35c",
    "Food": "#a8b4ff",
    "Local Travel": "#58154a",
    "Activities": "#ff9f80",
    "Emergency Fund": "#7fc8a9",
    "Spare": "#d9d6c9"
};

const ICONS = {
    "Transport": "🚆",
    "Stay": "🏨",
    "Food": "🍽️",
    "Local Travel": "🛺",
    "Activities": "🎟️",
    "Emergency Fund": "🛟"
};

function esc(text) {
    return String(text).replace(/[&<>"']/g, (c) =>
        ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

function plural(n, word) {
    return n + " " + word + (n === 1 ? "" : "s");
}

// ---------------------------------------------------------------- load lists
async function loadLists() {
    try {
        const [dests, cities] = await Promise.all([
            fetch("/api/destinations").then((r) => r.json()),
            fetch("/api/cities").then((r) => r.json())
        ]);
        $("to").innerHTML = dests.map((d) => "<option value=\"" + esc(d) + "\">" + esc(d) + "</option>").join("");
        $("cityList").innerHTML = cities.map((c) => "<option value=\"" + esc(c) + "\"></option>").join("");
    } catch (e) {
        showErrors(["Could not load the destination list. Is the server running?"]);
    }
}

// ---------------------------------------------------------------- form
function readNumber(id) {
    const v = $(id).value;
    return v === "" ? null : Number(v);
}

function validate(data) {
    const errors = [];
    if (!data.from) errors.push("Please enter the city you are travelling from.");
    if (!data.to) errors.push("Please select a destination.");
    if (data.days === null || !Number.isInteger(data.days) || data.days < 1 || data.days > 30)
        errors.push("Days must be a whole number between 1 and 30.");
    if (data.members === null || !Number.isInteger(data.members) || data.members < 1 || data.members > 50)
        errors.push("Members must be a whole number between 1 and 50.");
    if (data.budget === null || Number.isNaN(data.budget) || data.budget <= 0)
        errors.push("Total budget must be more than 0.");
    return errors;
}

function showErrors(errors) {
    $("errorBox").innerHTML = "<ul>" + errors.map((e) => "<li>" + esc(e) + "</li>").join("") + "</ul>";
    $("errorBox").hidden = false;
}

function clearErrors() {
    $("errorBox").hidden = true;
    $("errorBox").innerHTML = "";
}

$("planForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    clearErrors();

    const data = {
        from: $("from").value.trim(),
        to: $("to").value,
        days: readNumber("days"),
        members: readNumber("members"),
        budget: readNumber("budget")
    };

    const errors = validate(data);
    if (errors.length > 0) {
        $("dashboard").hidden = true;
        showErrors(errors);
        return;
    }

    try {
        const response = await fetch("/api/plan", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });
        const body = await response.json();
        if (!response.ok) {
            $("dashboard").hidden = true;
            showErrors(body.errors || ["Something went wrong."]);
            return;
        }
        render(body);
    } catch (e) {
        showErrors(["Could not reach the server. Please try again."]);
    }
});

function resetAll() {
    $("planForm").reset();
    clearErrors();
    $("dashboard").hidden = true;
}

$("resetBtn").addEventListener("click", resetAll);

$("newTripBtn").addEventListener("click", () => {
    resetAll();
    $("planner").scrollIntoView({ behavior: "smooth" });
});

$("sampleBtn").addEventListener("click", () => {
    $("from").value = "Hyderabad";
    $("to").value = "Goa";
    $("days").value = 4;
    $("members").value = 2;
    $("budget").value = 25000;
    clearErrors();
});

// ---------------------------------------------------------------- render
function render(p) {
    // banner
    const banner = $("banner");
    banner.textContent = p.status + ": " + p.message;
    banner.classList.toggle("warn", !p.feasible);

    // hero
    $("heroTag").textContent = p.to;
    $("heroTitle").textContent = "Welcome to your " + p.to + " trip!";
    $("heroSub").textContent = p.tagline;
    $("heroMeta").textContent = p.from + " to " + p.to + "  |  " + plural(p.days, "day") + "  |  " + plural(p.members, "traveller")
        + "  |  " + p.tier + " style (" + p.priceLevelPercent + "% of standard prices)";

    renderBudget(p);
    renderExpenses(p);
    renderHealth(p);
    renderRoute(p);

    // weather
    $("wxPlace").textContent = "📍 " + p.to.toUpperCase();
    $("wxTemp").textContent = p.weatherRange;
    $("wxNote").textContent = p.weatherNote;

    // journey
    $("jOut").textContent = p.from + " → " + p.to;
    $("jBack").textContent = p.to + " → " + p.from;
    $("jMode").textContent = p.transportMode;
    $("jDist").textContent = p.distanceKm > 0 ? "about " + p.distanceKm + " km" : "none";
    $("jFare").textContent = inr.format(p.farePerPerson);
    $("jNote").textContent = p.routeNote || "Sample fare estimate for " + plural(p.members, "traveller") + ": " + inr.format(p.farePerPerson * p.members);

    renderItinerary(p);

    // packing list
    $("packList").innerHTML = p.packingList
        .map((item) => "<li><label><input type=\"checkbox\"><span>" + esc(item) + "</span></label></li>")
        .join("");

    renderOptions(p);

    // tips
    $("tips").innerHTML = p.tips.map((t) => "<li>" + esc(t) + "</li>").join("");

    $("dashboard").hidden = false;
    $("dashboard").scrollIntoView({ behavior: "smooth", block: "start" });
}

function renderBudget(p) {
    $("budgetChip").textContent = "TOTAL " + inr.format(p.budget);

    const parts = p.categories.map((c) => ({ name: c.name, amount: c.amount }));
    if (p.spareBudget > 0) parts.push({ name: "Spare", amount: p.spareBudget });

    const denominator = Math.max(p.budget, p.totalPlanned);
    let start = 0;
    const stops = parts.map((part) => {
        const end = start + (part.amount / denominator) * 100;
        const piece = COLORS[part.name] + " " + start.toFixed(2) + "% " + end.toFixed(2) + "%";
        start = end;
        return piece;
    });
    $("donut").style.background = "conic-gradient(" + stops.join(", ") + ")";
    $("donutPct").textContent = Math.round(p.percentUsed) + "%";

    $("legend").innerHTML = parts.map((part) => {
        const pct = (part.amount / denominator * 100).toFixed(1);
        return "<li><span class=\"dot\" style=\"background:" + COLORS[part.name] + "\"></span>"
            + esc(part.name) + "<span class=\"pct\">" + pct + "%</span></li>";
    }).join("");
}

function renderExpenses(p) {
    $("expList").innerHTML = p.categories.map((c) =>
        "<li><span class=\"ico\">" + ICONS[c.name] + "</span><span>" + esc(c.name) + "</span>"
        + "<span class=\"amt\">" + inr.format(c.amount) + "</span></li>").join("");
    $("expTotal").textContent = inr.format(p.totalPlanned);
}

function renderHealth(p) {
    const fill = Math.min(p.percentUsed, 100);
    const color = p.feasible ? "#0f5f58" : "#c0392b";
    $("ring").style.background = "conic-gradient(" + color + " 0% " + fill + "%, rgba(255,255,255,0.6) " + fill + "% 100%)";
    $("ringPct").textContent = Math.round(p.percentUsed) + "%";
    $("healthStatus").textContent = p.status;
    $("healthText").textContent = p.feasible
        ? "Planned " + inr.format(p.totalPlanned) + " of " + inr.format(p.budget) + ". Unplanned: " + inr.format(p.spareBudget) + "."
        : "Lowest-cost plan is " + inr.format(p.totalPlanned) + ". Short by " + inr.format(p.shortfall) + ".";
}

function renderRoute(p) {
    const from = esc(p.from.length > 16 ? p.from.slice(0, 15) + "…" : p.from);
    const to = esc(p.to);
    $("routeBox").innerHTML =
        "<svg viewBox=\"0 0 320 230\" xmlns=\"http://www.w3.org/2000/svg\">"
        + "<path d=\"M55 45 C 270 25, 30 125, 160 128 S 285 175, 250 192\" fill=\"none\" stroke=\"#0f5f58\" stroke-width=\"4\" stroke-linecap=\"round\"/>"
        + "<circle cx=\"55\" cy=\"45\" r=\"10\" fill=\"#ffd35c\" stroke=\"#0f5f58\" stroke-width=\"4\"/>"
        + "<circle cx=\"250\" cy=\"192\" r=\"10\" fill=\"#ffd35c\" stroke=\"#0f5f58\" stroke-width=\"4\"/>"
        + "<text x=\"72\" y=\"34\" font-size=\"14\" font-weight=\"700\" fill=\"#1f2a28\">" + from + "</text>"
        + "<text x=\"190\" y=\"222\" font-size=\"14\" font-weight=\"700\" fill=\"#1f2a28\">" + to + "</text>"
        + "</svg>";
    $("stops").innerHTML = p.topStops.map((s) =>
        "<li><span class=\"dot\" style=\"background:#0f5f58\"></span>" + esc(s) + "</li>").join("");
}

function renderItinerary(p) {
    $("itinerary").innerHTML = p.itinerary.map((d) =>
        "<div class=\"day\">"
        + "<div><span class=\"day-pill\">Day " + d.day + "</span></div>"
        + "<div><div class=\"day-title\">" + esc(d.title) + "</div><ul>"
        + d.activities.map((a) => "<li>" + esc(a) + "</li>").join("")
        + "</ul></div>"
        + "<div class=\"day-spend\">Daily spend<strong>" + inr.format(d.daySpend) + "</strong></div>"
        + "</div>").join("");
}

function renderOptions(p) {
    $("optionsIntro").textContent = "Your plan runs at " + p.priceLevelPercent + "% of standard prices ("
        + p.tier + " style). " + p.tierDescription + " Here is what each full style would cost:";
    $("options").innerHTML = p.options.map((o) => {
        const fitText = o.fits
            ? "Fits, " + inr.format(o.difference) + " left"
            : "Over by " + inr.format(-o.difference);
        return "<div class=\"opt\"><span class=\"name\">" + esc(o.name) + "</span>"
            + "<span class=\"cost\">" + inr.format(o.total) + "</span>"
            + "<span class=\"desc\">" + esc(o.description) + "</span>"
            + "<span class=\"fit " + (o.fits ? "ok" : "no") + "\">" + fitText + "</span></div>";
    }).join("");
}

loadLists();
