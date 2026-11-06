package com.example.summit_climb


/**
 * Person using app is identified by their device.
 * Users are stored in the Firestore `users` collection, with [userId] as the document ID.
 *
 * Example:
 * ```
 * val user = User(userId = "device123", name = "Alex", email = "alex@example.com")
 * user.hasValidProfile()   // true
 * user.roles               // [ENTRANT]
 * ```
 *
 * @property userId: the device based user ID. (Firestore document ID)
 * @property name: user's name.
 * @property email: user's email address
 * @property phoneNumber: user's phone number. (Optional)
 * @property profileImageUrl: download URL of user's profile image
 * @property notificationsOptedOut: `true` if user has opted out of notifications
 * @property roles: user's roles. (Defaults to only [UserRole.ENTRANT])
 * @property accountStatus: whether account is active or deactivated.
 * @property flagged: `true` if user has been flagged for an admin to review.
 * @constructor Creates a user. With no arguments, creates an empty active entrant.
 */
class User(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var profileImageUrl: String = "",
    var notificationsOptedOut: Boolean = false,
    var roles: List<UserRole> = listOf(UserRole.ENTRANT),
    var accountStatus: AccountStatus = AccountStatus.ACTIVE,
    var flagged: Boolean = false
) {
    /**
     * Checks if name is not empty.
     * @return `true` if name has atleast one non space character.
     */
    fun validName(): Boolean{
        return name.isNotBlank()
    }

    /**
     * Checks the email is valid, like `alex@example.com`
     * @return `true` if email has text, then `@`, then a domain with a dot.
     */
    fun validEmail(): Boolean{
        val trimEmail = email.trim()
        val EMAIL_PATTERN = Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""")
        return EMAIL_PATTERN.matches(trimEmail)
    }

    /**
     * Checks that phone number is valid, like `7805551234`.
     * The phone number is optional, so an empty phone number is valid.
     * @return `true` if phoneNumber is empty or valid(10 digits)
     */
    fun validPhone(): Boolean{
        val trimPhone = phoneNumber.trim()
        val PHONE_PATTERN = Regex("""^[0-9]{10}$""")
        if (trimPhone.isEmpty()) {
            return true
        }
        return PHONE_PATTERN.matches(trimPhone)
    }

    /**
     * Checks name, email and phone number together
     * (needs to be called before saving profile changes)
     * @return `true` only if hasValidName, hasValidEmail and hasValidPhone are all `true`
     */
    fun validProfile(): Boolean{
        return validName() && validEmail() && validPhone()
    }
}

/**
 * Roles a User can have.
 * Every new user starts as an ENTRANT (default). A user can hold multiple roles at once,
 * like admin who is also a campsite owner and entrant.
 */
enum class UserRole{
    ENTRANT, OWNER, ADMIN
}
/**
 * Whether a User's account can be used.
 */
enum class AccountStatus{
    ACTIVE, DEACTIVATED
}