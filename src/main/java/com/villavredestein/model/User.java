package com.villavredestein.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_username", columnNames = "username")
        }
)
public class User {

    public enum Role {
        ADMIN,
        STUDENT,
        CLEANER
    }

    public enum SocialPreference {
        OPEN_FOR_CONTACT,
        LEUK,
        AF_EN_TOE,
        LIEVER_OP_MEZELF
    }

    public enum MealPreference {
        JA,
        SOMS,
        NEE
    }

    public enum AvailabilityStatus {
        TENTAMENPERIODE,
        DRUK,
        OPEN_VOOR_CHILLEN
    }

    // =====================================================================
    // # FIELDS
    // =====================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username may not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String username;

    @Size(max = 100, message = "Name may not exceed 100 characters")
    @Column(length = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email may not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String email;

    @Size(max = 30, message = "Phone number may not exceed 30 characters")
    @Column(length = 30)
    private String phoneNumber;

    @Size(max = 30, message = "Emergency phone number may not exceed 30 characters")
    @Column(length = 30)
    private String emergencyPhoneNumber;

    @Size(max = 100, message = "Study/work may not exceed 100 characters")
    @Column(length = 100)
    private String studyOrWork;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SocialPreference socialPreference;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private MealPreference mealPreference;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private AvailabilityStatus availabilityStatus;

    @Column(nullable = false)
    private boolean statusToggle = true;

    @Size(max = 255, message = "Profile image path may not exceed 255 characters")
    @Column(length = 255)
    private String profileImagePath;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    private String password;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // =====================================================================
    // # CONSTRUCTORS
    // =====================================================================

    public User() {
    }

    public User(String username, String email, String password, Role role) {
        this.username = require(username, "username");
        this.email = require(email, "email");
        this.password = require(password, "password");
        this.role = require(role, "role");
        normalize();
    }

    // =====================================================================
    // # LIFECYCLE
    // =====================================================================

    @PrePersist
    @PreUpdate
    private void normalize() {
        username = clean(username);
        email = clean(email);
        if (email != null) {
            email = email.toLowerCase();
        }
        fullName = clean(fullName);
        phoneNumber = clean(phoneNumber);
        emergencyPhoneNumber = clean(emergencyPhoneNumber);
        studyOrWork = clean(studyOrWork);
        profileImagePath = clean(profileImagePath);
    }

    // =====================================================================
    // # GETTERS
    // =====================================================================

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmergencyPhoneNumber() {
        return emergencyPhoneNumber;
    }

    public String getStudyOrWork() {
        return studyOrWork;
    }

    public SocialPreference getSocialPreference() {
        return socialPreference;
    }

    public MealPreference getMealPreference() {
        return mealPreference;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public boolean isStatusToggle() {
        return statusToggle;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    // =====================================================================
    // # SETTERS
    // =====================================================================

    public void setUsername(String username) {
        this.username = require(username, "username");
        normalize();
    }

    public void setFullName(String fullName) {
        this.fullName = clean(fullName);
        normalize();
    }

    public void setEmail(String email) {
        this.email = require(email, "email");
        normalize();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = clean(phoneNumber);
        normalize();
    }

    public void setEmergencyPhoneNumber(String emergencyPhoneNumber) {
        this.emergencyPhoneNumber = clean(emergencyPhoneNumber);
        normalize();
    }

    public void setStudyOrWork(String studyOrWork) {
        this.studyOrWork = clean(studyOrWork);
        normalize();
    }

    public void setSocialPreference(SocialPreference socialPreference) {
        this.socialPreference = socialPreference;
    }

    public void setMealPreference(MealPreference mealPreference) {
        this.mealPreference = mealPreference;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public void setStatusToggle(boolean statusToggle) {
        this.statusToggle = statusToggle;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = clean(profileImagePath);
        normalize();
    }

    public void setPassword(String password) {
        this.password = require(password, "password");
    }

    public void setRole(Role role) {
        this.role = require(role, "role");
    }

    // =====================================================================
    // # HELPERS
    // =====================================================================

    private <T> T require(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        if (value instanceof String s && s.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " may not be blank");
        }
        return value;
    }

    private String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public boolean hasRole(Role expectedRole) {
        return this.role == expectedRole;
    }

    // =====================================================================
    // # OBJECT CONTRACT
    // =====================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", emergencyPhoneNumber='" + emergencyPhoneNumber + '\'' +
                ", studyOrWork='" + studyOrWork + '\'' +
                ", socialPreference=" + socialPreference +
                ", mealPreference=" + mealPreference +
                ", availabilityStatus=" + availabilityStatus +
                ", statusToggle=" + statusToggle +
                ", profileImagePath='" + profileImagePath + '\'' +
                '}';
    }
}