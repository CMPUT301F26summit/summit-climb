package com.example.summit_climb

data class PolicyDetailContent (
    val code: String,
    val title: String,
    val details: String
)

val TermsOfService = PolicyDetailContent(
    code = "terms",
    title = "Terms of Service",
    details = "Placeholder message"
)

val PrivacyPolicy = PolicyDetailContent(
    code = "privacy",
    title = "Privacy Policy",
    details = "Placeholder message"
)

val DataUse = PolicyDetailContent(
    code = "data_use",
    title = "Data Use",
    details = "Placeholder message"
)

val CommunityGuidelines = PolicyDetailContent(
    code = "comm_guidelines",
    title = "Community Guidelines",
    details = "Placeholder message"
)

val SafetyDisclaimer = PolicyDetailContent(
    code = "safety_disclaimer",
    title = "Safety Disclaimer",
    details = "Placeholder message"
)

val currentPolicies = mutableListOf(TermsOfService, PrivacyPolicy, DataUse, CommunityGuidelines, SafetyDisclaimer)
