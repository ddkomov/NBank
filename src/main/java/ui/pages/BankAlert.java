package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    DEPOSIT_SUCCESSFULLY("✅ Successfully deposited"),
    PLEASE_ENTER_A_VALID_AMOUNT("❌ Please enter a valid amount."),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!");

    private final String message;

    BankAlert(String message) {
        this.message=message;
    }
}
