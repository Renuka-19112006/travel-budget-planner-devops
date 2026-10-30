package com.college.travelbudget.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_JSON = """
            {"from":"Hyderabad","to":"Goa","days":4,"members":2,"budget":25000}
            """;

    private org.springframework.test.web.servlet.ResultActions postPlan(String json) throws Exception {
        return mockMvc.perform(post("/api/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    void destinationsEndpointListsGoa() throws Exception {
        mockMvc.perform(get("/api/destinations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("Goa")));
    }

    @Test
    void citiesEndpointReturnsCities() throws Exception {
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("Hyderabad")));
    }

    @Test
    void validRequestReturnsAPlan() throws Exception {
        postPlan(VALID_JSON)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.to").value("Goa"))
                .andExpect(jsonPath("$.feasible").value(true))
                .andExpect(jsonPath("$.tier").value("Standard"))
                .andExpect(jsonPath("$.categories.length()").value(6))
                .andExpect(jsonPath("$.itinerary.length()").value(4))
                .andExpect(jsonPath("$.totalPlanned", greaterThan(0)));
    }

    @Test
    void lowBudgetReturnsNotFeasiblePlanWithStatus200() throws Exception {
        postPlan(VALID_JSON.replace("25000", "3000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feasible").value(false))
                .andExpect(jsonPath("$.status").value("Budget too low"));
    }

    @Test
    void zeroMembersIsRejected() throws Exception {
        postPlan(VALID_JSON.replace("\"members\":2", "\"members\":0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativeBudgetIsRejected() throws Exception {
        postPlan(VALID_JSON.replace("25000", "-500"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void zeroBudgetIsRejected() throws Exception {
        postPlan(VALID_JSON.replace("25000", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void zeroDaysIsRejected() throws Exception {
        postPlan(VALID_JSON.replace("\"days\":4", "\"days\":0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tooManyDaysIsRejected() throws Exception {
        postPlan(VALID_JSON.replace("\"days\":4", "\"days\":31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankFromIsRejected() throws Exception {
        postPlan(VALID_JSON.replace("Hyderabad", " "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownDestinationIsRejected() throws Exception {
        postPlan(VALID_JSON.replace("Goa", "Mars"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Unknown destination: Mars"));
    }

    @Test
    void missingFieldsAreRejected() throws Exception {
        postPlan("{\"from\":\"Hyderabad\",\"to\":\"Goa\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void textInsteadOfNumberIsRejected() throws Exception {
        postPlan(VALID_JSON.replace("\"days\":4", "\"days\":\"abc\""))
                .andExpect(status().isBadRequest());
    }
}
