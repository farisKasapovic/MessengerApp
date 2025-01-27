package praksa.unravel.talksy.ui.register


object InputFieldValidator {

    private val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    private val phonePattern = "^\\+?[0-9]{10,15}$".toRegex()

    fun validateFields(email: String, password: String, phoneNumber: String, username: String): String? {
        if (email.isBlank() || password.isBlank() || phoneNumber.isBlank() || username.isBlank()) {
            return "All fields are required."
        }
        return null
    }

    fun validateEmail(email: String): String? {
        if (!email.matches(emailPattern)) {
            return "Invalid email format."
        }
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.length < 6) {
            return "Password must be at least 6 characters long."
        }
        if (!password.any { it.isUpperCase() }) {
            return "Password must contain at least one uppercase letter."
        }
        if (!password.any { "!@#$%^&*()-_=+<>?".contains(it) }) {
            return "Password must contain at least one special character."
        }
        if (!password.any { it.isDigit() }) {
            return "Password must contain at least one digit."
        }
        return null
    }


    fun validatePhoneNumber(phoneNumber: String): String? {
        if (!phoneNumber.matches(phonePattern)) {
            return "Invalid phone number format."
        }
        return null
    }

    fun validateInputs(email: String, password: String, username: String, phoneNumber: String): String? {
        validateFields(email, password, phoneNumber, username)?.let { return it }
        validateEmail(email)?.let { return it }
        validatePassword(password)?.let { return it }
        validatePhoneNumber(phoneNumber)?.let { return it }
        return null
    }
}



