package com.example.summit_climb

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
    fun validName(): Boolean{
        return name.isNotBlank()
    }

    fun validEmail(): Boolean{
        val trimEmail = email.trim()
        val EMAIL_PATTERN = Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""")
        return EMAIL_PATTERN.matches(trimEmail)
    }

    fun validPhone(): Boolean{
        val trimPhone = phoneNumber.trim()
        val PHONE_PATTERN = Regex("""^[0-9]{10}$""")
        if (trimPhone.isEmpty()) {
            return true
        }
        return PHONE_PATTERN.matches(trimPhone)
    }

    fun validProfile(): Boolean{
        return validName() && validEmail() && validPhone()
    }
}

enum class UserRole{
    ENTRANT, OWNER, ADMIN
}

enum class AccountStatus{
    ACTIVE, DEACTIVATED
}