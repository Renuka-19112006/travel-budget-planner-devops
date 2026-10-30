package com.college.travelbudget.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** The only things the user has to enter: From, To, days, members and total budget. */
public record PlanRequest(
        @NotBlank(message = "Please enter the city you are travelling from")
        @Size(max = 60, message = "The From city name is too long")
        String from,

        @NotBlank(message = "Please select a destination")
        String to,

        @NotNull(message = "Number of days is required")
        @Min(value = 1, message = "Days must be at least 1")
        @Max(value = 30, message = "Days cannot be more than 30")
        Integer days,

        @NotNull(message = "Number of members is required")
        @Min(value = 1, message = "Members must be at least 1")
        @Max(value = 50, message = "Members cannot be more than 50")
        Integer members,

        @NotNull(message = "Total budget is required")
        @Positive(message = "Budget must be more than 0")
        Double budget
) {
}
