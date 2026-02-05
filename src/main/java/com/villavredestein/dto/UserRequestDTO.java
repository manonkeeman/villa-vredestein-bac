package com.villavredestein.dto;

import com.villavredestein.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserRequestDTO {

    @Size(min = 2, max = 50, message = "Gebruikersnaam moet tussen 2 en 50 tekens zijn")
    private String username;

    @Size(max = 100, message = "Naam mag maximaal 100 tekens zijn")
    private String fullName;

    @Email(message = "E-mailadres moet een geldig e-mailadres zijn")
    @Size(max = 100, message = "E-mailadres mag maximaal 100 tekens zijn")
    private String email;

    @Size(max = 30, message = "Telefoonnummer mag maximaal 30 tekens zijn")
    private String phoneNumber;

    @Size(max = 30, message = "Noodnummer mag maximaal 30 tekens zijn")
    private String emergencyPhoneNumber;

    @Size(max = 100, message = "Studie/werk mag maximaal 100 tekens zijn")
    private String studyOrWork;

    private User.SocialPreference socialPreference;
    private User.MealPreference mealPreference;
    private User.AvailabilityStatus availabilityStatus;

    private Boolean statusToggle;

    public UserRequestDTO() {}

    // getters/setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmergencyPhoneNumber() { return emergencyPhoneNumber; }
    public void setEmergencyPhoneNumber(String emergencyPhoneNumber) { this.emergencyPhoneNumber = emergencyPhoneNumber; }

    public String getStudyOrWork() { return studyOrWork; }
    public void setStudyOrWork(String studyOrWork) { this.studyOrWork = studyOrWork; }

    public User.SocialPreference getSocialPreference() { return socialPreference; }
    public void setSocialPreference(User.SocialPreference socialPreference) { this.socialPreference = socialPreference; }

    public User.MealPreference getMealPreference() { return mealPreference; }
    public void setMealPreference(User.MealPreference mealPreference) { this.mealPreference = mealPreference; }

    public User.AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(User.AvailabilityStatus availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public Boolean getStatusToggle() { return statusToggle; }
    public void setStatusToggle(Boolean statusToggle) { this.statusToggle = statusToggle; }
}